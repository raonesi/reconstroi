using System.Diagnostics;
using System.IO.Compression;
using System.Net;
using System.Net.Sockets;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

namespace ReConstroi.Launcher;

internal static class Program
{
    [STAThread]
    private static void Main(string[] args)
    {
        var root = Path.GetFullPath(Environment.GetEnvironmentVariable("RECONSTROI_APP_ROOT")
            ?? Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".reconstroi"));
        var key = Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(root.ToUpperInvariant())))[..16];
        using var open = new EventWaitHandle(false, EventResetMode.AutoReset, $"ReConstroi.Open.{key}");
        using var stop = new EventWaitHandle(false, EventResetMode.AutoReset, $"ReConstroi.Stop.{key}");
        if (args.Contains("--stop")) { stop.Set(); return; }
        using var mutex = new Mutex(true, $"ReConstroi.Launcher.{key}", out var first);
        if (!first) { open.Set(); return; }
        ApplicationConfiguration.Initialize();
        Application.Run(new LauncherContext(root, open, stop, args.Contains("--no-open")));
    }
}

internal sealed class LauncherContext : ApplicationContext
{
    private const string ApiUrl = "http://127.0.0.1:8081";
    private readonly NotifyIcon tray;
    private readonly System.Windows.Forms.Timer signals;
    private readonly string appRoot, logsRoot, configPath;
    private string runtimeRoot;
    private readonly bool noOpen;
    private readonly List<StreamWriter> logs = [];
    private PortableDatabase? database;
    private Process? backend, desktop;
    private bool ready, starting, stopping, shutdownRequested;
    private Form? progress;

    public LauncherContext(string root, EventWaitHandle open, EventWaitHandle stop, bool noOpen)
    {
        appRoot = root;
        runtimeRoot = Path.Combine(root, "runtime-v3");
        logsRoot = Path.Combine(root, "logs");
        configPath = Path.Combine(root, "reconstroi.env");
        this.noOpen = noOpen;
        var menu = new ContextMenuStrip();
        menu.Items.Add("Abrir ReConstrói", null, (_, _) => OpenApplication());
        menu.Items.Add("Reiniciar serviços", null, async (_, _) => await RestartAsync());
        menu.Items.Add("Editar configuração", null, (_, _) => OpenFile(configPath));
        menu.Items.Add("Abrir logs", null, (_, _) => OpenFolder(logsRoot));
        menu.Items.Add(new ToolStripSeparator());
        menu.Items.Add("Sair", null, async (_, _) => await ShutdownAsync());
        tray = new NotifyIcon {
            Icon = System.Drawing.Icon.ExtractAssociatedIcon(Environment.ProcessPath!) ?? System.Drawing.SystemIcons.Application,
            Text = "ReConstrói — iniciando", Visible = true, ContextMenuStrip = menu
        };
        tray.DoubleClick += (_, _) => OpenApplication();
        signals = new System.Windows.Forms.Timer { Interval = 500 };
        signals.Tick += async (_, _) => {
            if (stop.WaitOne(0)) await ShutdownAsync();
            if (open.WaitOne(0)) OpenApplication();
            if (ready && (backend?.HasExited ?? false)) {
                ready = false;
                tray.Text = "ReConstrói — serviço interrompido";
                tray.ShowBalloonTip(5000, "ReConstrói", "Um serviço parou. Use Reiniciar serviços no menu.", ToolTipIcon.Warning);
                WriteStatus("interrupted");
            }
        };
        signals.Start();
        Application.Idle += StartOnce;
    }

    private async void StartOnce(object? sender, EventArgs args)
    {
        Application.Idle -= StartOnce;
        await InitializeAsync();
    }

    private async Task InitializeAsync()
    {
        if (starting || stopping) return;
        starting = true;
        ready = false;
        tray.Text = "ReConstrói — iniciando";
        if (!noOpen) {
            progress = new Form { Text = "ReConstrói", ClientSize = new(460, 115), StartPosition = FormStartPosition.CenterScreen,
                FormBorderStyle = FormBorderStyle.FixedDialog, MaximizeBox = false, MinimizeBox = false, ControlBox = false };
            progress.Controls.Add(new Label { Text = "Preparando o sistema e o banco de dados…\nA primeira abertura pode levar alguns minutos.", Dock = DockStyle.Fill, Padding = new Padding(20) });
            progress.Controls.Add(new ProgressBar { Style = ProgressBarStyle.Marquee, Dock = DockStyle.Bottom, Height = 12 });
            progress.Show();
        }
        try {
            Directory.CreateDirectory(appRoot);
            Directory.CreateDirectory(logsRoot);
            Directory.CreateDirectory(Path.Combine(appRoot, "data", "uploads"));
            WriteStatus("starting");
            runtimeRoot = await Task.Run(EnsureRuntimeExtracted);
            database = new PortableDatabase(runtimeRoot, appRoot, logsRoot);
            EnsureConfiguration();
            var environment = ReadConfiguration();
            await database.EnsureStartedAsync(environment);
            if (!await IsBackendReadyAsync()) {
                if (await IsPortOpenAsync(8081)) throw new InvalidOperationException("A porta 8081 está sendo usada por outro processo. Feche a API antiga e tente novamente.");
                backend = StartService(Path.Combine(runtimeRoot, "java", "bin", "javaw.exe"),
                    Path.Combine(runtimeRoot, "backend"), environment, "backend",
                    "-Dfile.encoding=UTF-8", "-jar", Path.Combine(runtimeRoot, "backend", "backend.jar"));
                await WaitUntilReadyAsync(backend, IsBackendReadyAsync, "API", 90);
            }
            ready = true;
            tray.Text = "ReConstrói — em execução";
            WriteStatus("ready");
            if (!noOpen && !shutdownRequested) OpenApplication();
        }
        catch (Exception exception) {
            try { await StopOwnedProcessesAsync(); } catch { }
            tray.Text = "ReConstrói — erro ao iniciar";
            File.AppendAllText(Path.Combine(logsRoot, "launcher.log"), $"{DateTimeOffset.Now:O} {exception}\n");
            WriteStatus("error", exception.Message);
            if (!noOpen) MessageBox.Show(exception.Message + "\n\nLogs: " + logsRoot,
                "Não foi possível iniciar o ReConstrói", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
        finally {
            progress?.Close(); progress?.Dispose(); progress = null;
            starting = false;
            if (shutdownRequested) await ShutdownAsync();
        }
    }

    private string EnsureRuntimeExtracted()
    {
        using var payload = Assembly.GetExecutingAssembly().GetManifestResourceStream("ReConstroi.payload.zip")
            ?? throw new InvalidOperationException("Pacote interno não encontrado.");
        var hash = Convert.ToHexString(SHA256.HashData(payload));
        // Cada pacote tem sua pasta: uma atualização nunca apaga binários ainda em uso.
        var extractionRoot = Path.Combine(appRoot, "runtime-v3", hash[..16]);
        var marker = Path.Combine(extractionRoot, ".ready");
        if (File.Exists(marker) && File.ReadAllText(marker) == hash) return extractionRoot;
        Directory.CreateDirectory(extractionRoot);
        payload.Position = 0;
        using var archive = new ZipArchive(payload, ZipArchiveMode.Read);
        foreach (var entry in archive.Entries) {
            var destination = Path.GetFullPath(Path.Combine(extractionRoot, entry.FullName));
            if (!destination.StartsWith(extractionRoot + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase))
                throw new InvalidOperationException("Caminho inválido no pacote interno.");
            if (string.IsNullOrEmpty(entry.Name)) { Directory.CreateDirectory(destination); continue; }
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!);
            entry.ExtractToFile(destination, true);
        }
        File.WriteAllText(marker, hash);
        return extractionRoot;
    }

    private void EnsureConfiguration()
    {
        if (File.Exists(configPath)) return;
        var secret = Convert.ToBase64String(RandomNumberGenerator.GetBytes(48));
        File.WriteAllText(configPath, $"""
            # ReConstrói Desktop — banco e API iniciados automaticamente
            DATABASE_URL=jdbc:postgresql://localhost:5432/reconstroi
            DATABASE_USERNAME=postgres
            DATABASE_PASSWORD=123
            JWT_SECRET={secret}
            JWT_EXPIRATION_HOURS=24
            ADMIN_EMAIL=
            ADMIN_PASSWORD=
            MAIL_ENABLED=false
            """);
    }

    private Dictionary<string, string> ReadConfiguration()
    {
        var values = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
        foreach (var rawLine in File.ReadAllLines(configPath)) {
            var line = rawLine.Trim();
            var separator = line.IndexOf('=');
            if (line.Length == 0 || line.StartsWith('#') || separator <= 0) continue;
            values[line[..separator].Trim()] = line[(separator + 1)..].Trim();
        }
        values.TryAdd("DATABASE_URL", "jdbc:postgresql://localhost:5432/reconstroi");
        values.TryAdd("DATABASE_USERNAME", "postgres");
        values.TryAdd("DATABASE_PASSWORD", "123");
        values["SERVER_PORT"] = "8081";
        values["SERVER_ADDRESS"] = "127.0.0.1";
        values["CORS_ALLOWED_ORIGINS"] = "http://localhost:3000,http://127.0.0.1:3000";
        values["UPLOAD_DIRECTORY"] = Path.Combine(appRoot, "data", "uploads");
        values["JPA_DDL_AUTO"] = "validate";
        return values;
    }

    private Process StartService(string executable, string directory, Dictionary<string, string> environment,
        string name, params string[] arguments)
    {
        var info = new ProcessStartInfo(executable) { WorkingDirectory = directory, UseShellExecute = false,
            CreateNoWindow = true, RedirectStandardOutput = true, RedirectStandardError = true };
        foreach (var (key, value) in environment) info.Environment[key] = value;
        foreach (var argument in arguments) info.ArgumentList.Add(argument);
        var log = new StreamWriter(new FileStream(Path.Combine(logsRoot, $"{name}-{DateTime.Now:yyyyMMdd}.log"),
            FileMode.Append, FileAccess.Write, FileShare.ReadWrite)) { AutoFlush = true };
        logs.Add(log);
        var process = new Process { StartInfo = info };
        process.OutputDataReceived += (_, e) => WriteLog(log, e.Data);
        process.ErrorDataReceived += (_, e) => WriteLog(log, e.Data);
        if (!process.Start()) throw new InvalidOperationException($"Não foi possível iniciar {name}.");
        process.BeginOutputReadLine(); process.BeginErrorReadLine();
        return process;
    }

    private static void WriteLog(StreamWriter writer, string? line)
    {
        if (line == null) return;
        lock (writer) { try { writer.WriteLine($"{DateTimeOffset.Now:O} {line}"); } catch (ObjectDisposedException) { } }
    }

    private static async Task WaitUntilReadyAsync(Process process, Func<Task<bool>> probe, string name, int seconds)
    {
        var until = DateTime.UtcNow.AddSeconds(seconds);
        while (DateTime.UtcNow < until) {
            if (process.HasExited) throw new InvalidOperationException($"O serviço {name} encerrou durante a inicialização. Consulte os logs.");
            if (await probe()) return;
            await Task.Delay(500);
        }
        throw new InvalidOperationException($"Tempo esgotado ao iniciar {name}. Consulte os logs.");
    }

    private static async Task<bool> IsPortOpenAsync(int port)
    {
        try { using var client = new TcpClient(); await client.ConnectAsync(IPAddress.Loopback, port).WaitAsync(TimeSpan.FromSeconds(2)); return true; }
        catch { return false; }
    }

    private static async Task<bool> IsBackendReadyAsync()
    {
        try {
            using var client = new HttpClient { Timeout = TimeSpan.FromSeconds(3) };
            using var health = JsonDocument.Parse(await client.GetStringAsync(ApiUrl + "/actuator/health"));
            if (health.RootElement.GetProperty("status").GetString() != "UP") return false;
            var docs = await client.GetStringAsync(ApiUrl + "/v3/api-docs");
            return docs.Contains("/materiais") && docs.Contains("/usuarios");
        } catch { return false; }
    }

    private void OpenApplication()
    {
        if (!ready) { progress?.Activate(); return; }
        OpenDesktop();
    }

    private void OpenDesktop()
    {
        try {
            var next = StartService(Path.Combine(runtimeRoot, "java", "bin", "javaw.exe"),
                Path.Combine(runtimeRoot, "desktop"), ReadConfiguration(), "desktop", "-Dfile.encoding=UTF-8",
                "-Dreconstroi.api.url=" + ApiUrl, "-jar", Path.Combine(runtimeRoot, "desktop", "reconstroi-desktop.jar"));
            if (desktop is { HasExited: false }) next.Dispose();
            else { desktop?.Dispose(); desktop = next; }
        } catch (Exception e) { MessageBox.Show(e.Message, "ReConstrói"); }
    }

    private static void OpenFile(string path) {
        var info = new ProcessStartInfo("notepad.exe") { UseShellExecute = true }; info.ArgumentList.Add(path); Process.Start(info)?.Dispose();
    }
    private static void OpenFolder(string path) { Directory.CreateDirectory(path); Process.Start(new ProcessStartInfo(path) { UseShellExecute = true })?.Dispose(); }

    private void WriteStatus(string status, string? error = null) => File.WriteAllText(Path.Combine(appRoot, "launcher-status.json"),
        JsonSerializer.Serialize(new { status, error, updatedAt = DateTimeOffset.UtcNow, processId = Environment.ProcessId }));

    private async Task RestartAsync()
    {
        if (starting || stopping) return;
        starting = true;
        try { await StopOwnedProcessesAsync(); }
        finally { starting = false; }
        await InitializeAsync();
    }

    private async Task StopOwnedProcessesAsync()
    {
        ready = false;
        foreach (var process in new[] { desktop, backend }) {
            if (process == null) continue;
            try { if (!process.HasExited) { process.Kill(true); await process.WaitForExitAsync().WaitAsync(TimeSpan.FromSeconds(10)); } }
            catch (Exception) { }
            finally { process.Dispose(); }
        }
        desktop = backend = null;
        foreach (var log in logs) { lock (log) log.Dispose(); }
        logs.Clear();
        if (database != null) await database.StopOwnedAsync();
    }

    private async Task ShutdownAsync()
    {
        if (stopping) return;
        if (starting) { shutdownRequested = true; return; }
        stopping = true;
        signals.Stop();
        try { await StopOwnedProcessesAsync(); WriteStatus("stopped"); }
        catch (Exception e) { File.AppendAllText(Path.Combine(logsRoot, "launcher.log"), $"{DateTimeOffset.Now:O} {e}\n"); }
        finally { ExitThread(); }
    }

    protected override void ExitThreadCore()
    {
        signals.Stop(); signals.Dispose();
        tray.Visible = false; tray.Dispose();
        base.ExitThreadCore();
    }
}
