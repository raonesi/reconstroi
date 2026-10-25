using System.Diagnostics;
using System.Net.Sockets;
using System.Text;
using System.Text.RegularExpressions;

namespace ReConstroi.Launcher;

/// <summary>Starts the bundled PostgreSQL only for the default local database.</summary>
internal sealed class PortableDatabase
{
    private const string DefaultUrl = "jdbc:postgresql://localhost:5432/reconstroi";
    private readonly string postgresRoot;
    private readonly string dataDirectory;
    private readonly string logsRoot;
    private readonly SemaphoreSlim lifecycle = new(1, 1);
    private ServerIdentity? ownedServer;
    private string password = "";

    public PortableDatabase(string runtimeRoot, string appRoot, string logsRoot)
    {
        postgresRoot = Path.Combine(runtimeRoot, "postgres");
        dataDirectory = Path.GetFullPath(Path.Combine(appRoot, "postgres-data"));
        this.logsRoot = logsRoot;
    }

    public async Task EnsureStartedAsync(Dictionary<string, string> environment)
    {
        await lifecycle.WaitAsync().ConfigureAwait(false);
        try
        {
            var url = GetOrDefault(environment, "DATABASE_URL", DefaultUrl);
            // Custom JDBC options, hosts, ports and database names belong to the user's server.
            // Let JDBC validate them; do not initialize or reconfigure an unrelated database.
            if (!TryGetManagedHost(url, out var host)) return;

            var user = GetOrDefault(environment, "DATABASE_USERNAME", "postgres");
            password = GetOrDefault(environment, "DATABASE_PASSWORD", "123");
            Directory.CreateDirectory(logsRoot);
            EnsureBundledTools();

            if (await IsPortOpenAsync(host).ConfigureAwait(false))
            {
                await EnsureDatabaseAsync(host, user).ConfigureAwait(false);
                Log("PostgreSQL já disponível. A instância existente foi preservada.");
                return;
            }

            await EnsureClusterAsync(user).ConfigureAwait(false);
            var status = await RunAsync("pg_ctl", ["status", "-D", dataDirectory], TimeSpan.FromSeconds(10)).ConfigureAwait(false);
            if (status.ExitCode == 0)
                throw new InvalidOperationException("O banco local já está iniciado, mas não responde em localhost:5432. Confira a porta na configuração e o log do PostgreSQL.");
            if (status.ExitCode != 3)
                throw Failure("Não foi possível verificar o estado do banco local", status);

            var serverLog = Path.Combine(logsRoot, $"postgres-{DateTime.Now:yyyyMMdd}.log");
            var start = await RunAsync("pg_ctl",
                ["start", "-D", dataDirectory, "-l", serverLog, "-o", "-h localhost -p 5432", "-w", "-t", "45"],
                TimeSpan.FromSeconds(55)).ConfigureAwait(false);
            if (start.ExitCode != 0)
                throw Failure($"O PostgreSQL não iniciou. Consulte {serverLog}", start);

            // Keep the identity, not just the PID, so a later, independently restarted server
            // is never stopped by this launcher.
            ownedServer = ReadServerIdentity()
                ?? throw new InvalidOperationException("O PostgreSQL iniciou, mas não foi possível identificar sua instância. Consulte o log do banco.");
            Log("PostgreSQL local iniciado pelo aplicativo.");
            await EnsureDatabaseAsync(host, user).ConfigureAwait(false);
        }
        finally
        {
            lifecycle.Release();
        }
    }

    public async Task StopOwnedAsync()
    {
        await lifecycle.WaitAsync().ConfigureAwait(false);
        try
        {
            if (ownedServer == null) return;
            if (ReadServerIdentity() != ownedServer)
            {
                ownedServer = null;
                return;
            }

            var stop = await RunAsync("pg_ctl", ["stop", "-D", dataDirectory, "-m", "fast", "-w", "-t", "30"],
                TimeSpan.FromSeconds(40)).ConfigureAwait(false);
            if (stop.ExitCode != 0)
            {
                Log("Não foi possível concluir o encerramento do PostgreSQL: " + stop.Error);
                return;
            }
            ownedServer = null;
            Log("PostgreSQL local encerrado com segurança.");
        }
        catch (Exception exception)
        {
            // Never forcibly terminate PostgreSQL; preserve the cluster on shutdown failures.
            Log("Falha ao encerrar o banco local: " + exception.Message);
        }
        finally
        {
            lifecycle.Release();
        }
    }

    private async Task EnsureClusterAsync(string user)
    {
        var version = await RunAsync("pg_ctl", ["--version"], TimeSpan.FromSeconds(10)).ConfigureAwait(false);
        var match = Regex.Match(version.Output, @"\(PostgreSQL\)\s+(\d+)");
        if (version.ExitCode != 0 || !match.Success)
            throw Failure("Não foi possível verificar a versão do PostgreSQL incluído", version);
        var major = match.Groups[1].Value;
        var versionPath = Path.Combine(dataDirectory, "PG_VERSION");
        if (File.Exists(versionPath))
        {
            var dataMajor = (await File.ReadAllTextAsync(versionPath).ConfigureAwait(false)).Trim();
            if (dataMajor != major)
                throw new InvalidOperationException($"Os dados existentes usam PostgreSQL {dataMajor}, mas este aplicativo inclui a versão {major}. Os dados foram preservados; é necessário migrar o banco antes de abri-lo nesta versão.");
            return;
        }

        if (Directory.Exists(dataDirectory) && Directory.EnumerateFileSystemEntries(dataDirectory).Any())
            throw new InvalidOperationException($"A pasta {dataDirectory} contém arquivos, mas não é um banco PostgreSQL válido. Os arquivos foram preservados; confira essa pasta antes de inicializar o banco.");
        if (password.IndexOfAny(['\r', '\n', '\0']) >= 0)
            throw new InvalidOperationException("A senha do banco local não pode conter quebras de linha.");

        Directory.CreateDirectory(Path.GetDirectoryName(dataDirectory)!);
        var passwordFile = Path.Combine(Path.GetDirectoryName(dataDirectory)!, $".postgres-password-{Guid.NewGuid():N}.tmp");
        try
        {
            await File.WriteAllTextAsync(passwordFile, password + "\n", new UTF8Encoding(false)).ConfigureAwait(false);
            var init = await RunAsync("initdb",
                ["-D", dataDirectory, "-U", user, "--encoding=UTF8", "--locale=C", "--auth-host=scram-sha-256",
                    "--auth-local=scram-sha-256", "--pwfile", passwordFile, "--no-instructions"],
                TimeSpan.FromSeconds(90)).ConfigureAwait(false);
            if (init.ExitCode != 0)
                throw Failure("Não foi possível preparar o banco local", init);
            Log("Banco local inicializado com codificação UTF-8 e autenticação por senha.");
        }
        finally
        {
            File.Delete(passwordFile);
        }
    }

    private async Task EnsureDatabaseAsync(string host, string user)
    {
        var connection = new Dictionary<string, string>
        {
            ["PGPASSWORD"] = password,
            ["PGCONNECT_TIMEOUT"] = "5",
            ["PGCLIENTENCODING"] = "UTF8"
        };
        // Connecting directly also supports accounts that can use reconstroi but cannot
        // access the maintenance database or create additional databases.
        var check = await QueryAsync(host, user, "reconstroi", "SELECT 1", connection).ConfigureAwait(false);
        if (check.ExitCode == 0) return;

        var exists = await QueryAsync(host, user, "postgres",
            "SELECT 1 FROM pg_database WHERE datname = 'reconstroi'", connection).ConfigureAwait(false);
        if (exists.ExitCode != 0)
            throw Failure("O PostgreSQL está ativo, mas não foi possível conectar. Confira DATABASE_USERNAME e DATABASE_PASSWORD na configuração do aplicativo", exists);
        if (exists.Output.Trim() == "1")
            throw Failure("O banco reconstroi existe, mas o usuário configurado não consegue acessá-lo", check);

        var create = await RunAsync("createdb",
            ["--host", host, "--port", "5432", "--username", user, "--no-password", "--maintenance-db=postgres", "--", "reconstroi"],
            TimeSpan.FromSeconds(30), connection).ConfigureAwait(false);
        // Recheck even when createdb fails: another client may have created it concurrently.
        var verify = await QueryAsync(host, user, "reconstroi", "SELECT 1", connection).ConfigureAwait(false);
        if (verify.ExitCode != 0)
            throw Failure("Não foi possível criar ou acessar o banco reconstroi", create.ExitCode != 0 ? create : verify);
        Log("Banco reconstroi criado e conexão verificada.");
    }

    private Task<CommandResult> QueryAsync(string host, string user, string database, string query,
        Dictionary<string, string> connection) => RunAsync("psql",
        ["-X", "--no-password", "--host", host, "--port", "5432", "--username", user, "--dbname", database,
            "--tuples-only", "--no-align", "--set=ON_ERROR_STOP=1", "--command", query], TimeSpan.FromSeconds(15), connection);

    private async Task<CommandResult> RunAsync(string tool, string[] arguments, TimeSpan timeout,
        Dictionary<string, string>? environment = null)
    {
        // O servidor Windows pode manter handles herdados de pg_ctl abertos.
        // Para start, os logs vao ao arquivo indicado por -l, sem pipes herdados.
        var capture = !(tool == "pg_ctl" && arguments.FirstOrDefault() == "start");
        var info = new ProcessStartInfo(Path.Combine(postgresRoot, "bin", tool + ".exe"))
        {
            WorkingDirectory = postgresRoot,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = capture,
            RedirectStandardError = capture
        };
        if (capture) { info.StandardOutputEncoding = Encoding.UTF8; info.StandardErrorEncoding = Encoding.UTF8; }
        // A user's psql shell configuration must not redirect this connection elsewhere.
        foreach (var key in info.Environment.Keys.Where(key => key.StartsWith("PG", StringComparison.OrdinalIgnoreCase)).ToArray())
            info.Environment.Remove(key);
        info.Environment["LC_ALL"] = "C";
        info.Environment["PATH"] = Path.Combine(postgresRoot, "bin") + Path.PathSeparator + Environment.GetEnvironmentVariable("PATH");
        if (environment != null)
            foreach (var (key, value) in environment) info.Environment[key] = value;
        foreach (var argument in arguments) info.ArgumentList.Add(argument);

        using var process = new Process { StartInfo = info };
        using var cancellation = new CancellationTokenSource(timeout);
        if (!process.Start()) throw new InvalidOperationException($"Não foi possível executar {tool}.");
        var output = capture ? process.StandardOutput.ReadToEndAsync(cancellation.Token) : Task.FromResult("");
        var error = capture ? process.StandardError.ReadToEndAsync(cancellation.Token) : Task.FromResult("");
        try
        {
            // pg_ctl start uses -l so postgres does not inherit these output pipes.
            // Wait only for this process; waiting for its descendants would wait forever.
            await process.WaitForExitAsync(cancellation.Token).ConfigureAwait(false);
            return new CommandResult(process.ExitCode, await output.ConfigureAwait(false), await error.ConfigureAwait(false));
        }
        catch (OperationCanceledException)
        {
            try { if (!process.HasExited) process.Kill(entireProcessTree: false); }
            catch (InvalidOperationException) { }
            throw new InvalidOperationException($"O comando {tool} excedeu o tempo de espera. Consulte os logs do PostgreSQL; os dados existentes foram preservados.");
        }
    }

    private void EnsureBundledTools()
    {
        foreach (var name in new[] { "pg_ctl", "initdb", "psql", "createdb", "postgres" })
            if (!File.Exists(Path.Combine(postgresRoot, "bin", name + ".exe")))
                throw new InvalidOperationException("O PostgreSQL incluído no aplicativo está incompleto. Extraia novamente o executável distribuído.");
    }

    private ServerIdentity? ReadServerIdentity()
    {
        try
        {
            var lines = File.ReadAllLines(Path.Combine(dataDirectory, "postmaster.pid"));
            if (lines.Length < 3 || !int.TryParse(lines[0], out var pid) || pid <= 0
                || !long.TryParse(lines[2], out var startedAt)
                || !string.Equals(Path.GetFullPath(lines[1]), dataDirectory, StringComparison.OrdinalIgnoreCase))
                return null;
            return new ServerIdentity(pid, startedAt);
        }
        catch (IOException) { return null; }
        catch (UnauthorizedAccessException) { return null; }
    }

    private static bool TryGetManagedHost(string url, out string host)
    {
        host = "localhost";
        if (!url.StartsWith("jdbc:postgresql://", StringComparison.OrdinalIgnoreCase)
            || !Uri.TryCreate(url[5..], UriKind.Absolute, out var uri)
            || uri.Port != 5432 || uri.AbsolutePath != "/reconstroi"
            || uri.Query.Length != 0 || uri.Fragment.Length != 0 || uri.UserInfo.Length != 0)
            return false;
        if (uri.Host != "localhost" && uri.Host != "127.0.0.1" && uri.Host != "[::1]") return false;
        host = uri.Host.Trim('[', ']');
        return true;
    }

    private static async Task<bool> IsPortOpenAsync(string host)
    {
        try
        {
            using var client = new TcpClient();
            await client.ConnectAsync(host, 5432).WaitAsync(TimeSpan.FromSeconds(3)).ConfigureAwait(false);
            return true;
        }
        catch (SocketException) { return false; }
        catch (TimeoutException) { return false; }
    }

    private static string GetOrDefault(Dictionary<string, string> environment, string key, string fallback)
    {
        if (environment.TryGetValue(key, out var value) && !string.IsNullOrWhiteSpace(value)) return value;
        environment[key] = fallback;
        return fallback;
    }

    private InvalidOperationException Failure(string description, CommandResult result)
    {
        var detail = Redact(string.IsNullOrWhiteSpace(result.Error) ? result.Output : result.Error).Trim();
        if (detail.Length > 2000) detail = detail[..2000];
        var message = description + (detail.Length == 0 ? "." : ":\n" + detail);
        Log(message);
        return new InvalidOperationException(message);
    }

    private string Redact(string value) => password.Length == 0 ? value : value.Replace(password, "[senha ocultada]", StringComparison.Ordinal);

    private void Log(string message)
    {
        try
        {
            Directory.CreateDirectory(logsRoot);
            File.AppendAllText(Path.Combine(logsRoot, $"database-launcher-{DateTime.Now:yyyyMMdd}.log"),
                $"{DateTimeOffset.Now:O} {Redact(message)}{Environment.NewLine}", Encoding.UTF8);
        }
        catch (IOException) { }
        catch (UnauthorizedAccessException) { }
    }

    private sealed record ServerIdentity(int Pid, long StartedAt);
    private sealed record CommandResult(int ExitCode, string Output, string Error);
}
