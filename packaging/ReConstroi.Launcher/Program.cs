using System.Diagnostics;
using System.Drawing;
using System.IO.Compression;
using System.Net;
using System.Net.Sockets;
using System.Reflection;
using System.Security.Cryptography;

namespace ReConstroi.Launcher;

internal static class Program
{
    [STAThread]
    private static void Main()
    {
        using var mutex = new Mutex(true, "ReConstroi.Launcher.SingleInstance", out var firstInstance);
        if (!firstInstance)
        {
            OpenInstalledDesktop();
            return;
        }

        ApplicationConfiguration.Initialize();
        Application.Run(new LauncherContext());
    }

    private static void OpenInstalledDesktop()
    {
        try
        {
            var root = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "ReConstroi", $"runtime-v{LauncherContext.AppVersion}");
            var java = Path.Combine(root, "java", "bin", "javaw.exe");
            var jar = Path.Combine(root, "desktop", "reconstroi-desktop.jar");
            if (!File.Exists(java) || !File.Exists(jar)) return;
            var info = new ProcessStartInfo(java)
            {
                WorkingDirectory = Path.Combine(root, "desktop"),
                UseShellExecute = false,
                CreateNoWindow = true
            };
            info.ArgumentList.Add("-Dfile.encoding=UTF-8");
            info.ArgumentList.Add("-Dreconstroi.api.url=http://127.0.0.1:8081");
            info.ArgumentList.Add("-jar");
            info.ArgumentList.Add(jar);
            Process.Start(info);
        }
        catch
        {
            MessageBox.Show("Abra http://localhost:3000 no navegador.", "ReConstrói",
                MessageBoxButtons.OK, MessageBoxIcon.Information);
        }
    }
}

internal sealed class LauncherContext : ApplicationContext
{
    // Aumente a cada mudança no pacote interno: força a extração da versão nova.
    internal const string AppVersion = "6";
    private readonly NotifyIcon tray;
    private readonly string appRoot;
    private readonly string runtimeRoot;
    private readonly string dataRoot;
    private readonly string logsRoot;
    private readonly string configPath;
    private Process? backend;
    private Process? desktop;
    private StreamWriter? backendLog;
    private StreamWriter? desktopLog;
    private bool ready;
    private bool starting;

    public LauncherContext()
    {
        appRoot = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "ReConstroi");
        runtimeRoot = Path.Combine(appRoot, $"runtime-v{AppVersion}");
        dataRoot = Path.Combine(appRoot, "data");
        logsRoot = Path.Combine(appRoot, "logs");
        configPath = Path.Combine(appRoot, "reconstroi.env");

        var menu = new ContextMenuStrip();
        menu.Items.Add("Abrir ReConstrói", null, (_, _) => OpenApplication());
        menu.Items.Add("Reiniciar serviços", null, async (_, _) => await RestartAsync());
        menu.Items.Add("Editar configuração", null, (_, _) => OpenFile(configPath));
        menu.Items.Add("Abrir logs", null, (_, _) => OpenFolder(logsRoot));
        menu.Items.Add(new ToolStripSeparator());
        menu.Items.Add("Sair", null, (_, _) => ExitThread());

        tray = new NotifyIcon
        {
            Icon = Icon.ExtractAssociatedIcon(Environment.ProcessPath!) ?? SystemIcons.Application,
            Text = "ReConstrói — iniciando",
            Visible = true,
            ContextMenuStrip = menu
        };
        tray.DoubleClick += (_, _) => OpenApplication();

        Application.Idle += StartOnce;
    }

    private async void StartOnce(object? sender, EventArgs args)
    {
        Application.Idle -= StartOnce;
        await InitializeAsync();
    }

    private async Task InitializeAsync()
    {
        if (starting) return;
        starting = true;
        ready = false;
        tray.Text = "ReConstrói — iniciando";

        try
        {
            Directory.CreateDirectory(appRoot);
            Directory.CreateDirectory(dataRoot);
            Directory.CreateDirectory(logsRoot);
            EnsureRuntimeExtracted();
            EnsureConfiguration();
            var environment = ReadConfiguration();

            if (!await IsHealthyAsync("http://localhost:8081/actuator/health"))
            {
                backend = StartBackend(environment);
                if (!await WaitForPortAsync(8081, backend, TimeSpan.FromSeconds(55)))
                    throw new InvalidOperationException("A API não iniciou. Verifique se o PostgreSQL está ativo e confira o log do backend.");
            }

            ready = true;
            tray.Text = "ReConstrói — em execução";
            tray.ShowBalloonTip(2500, "ReConstrói", "Sistema iniciado. Clique duas vezes no ícone para abrir.", ToolTipIcon.Info);
            OpenApplication();
        }
        catch (Exception exception)
        {
            StopOwnedProcesses();
            tray.Text = "ReConstrói — erro ao iniciar";
            var message = exception.Message + Environment.NewLine + Environment.NewLine
                + "Configuração: " + configPath + Environment.NewLine
                + "Logs: " + logsRoot;
            MessageBox.Show(message, "Não foi possível iniciar o ReConstrói", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
        finally
        {
            starting = false;
        }
    }

    private void EnsureRuntimeExtracted()
    {
        var marker = Path.Combine(runtimeRoot, ".ready");
        if (File.Exists(marker)) return;

        if (Directory.Exists(runtimeRoot)) Directory.Delete(runtimeRoot, true);
        Directory.CreateDirectory(runtimeRoot);
        using var payload = Assembly.GetExecutingAssembly().GetManifestResourceStream("ReConstroi.payload.zip")
            ?? throw new InvalidOperationException("O pacote interno do aplicativo não foi encontrado.");
        using var archive = new ZipArchive(payload, ZipArchiveMode.Read);

        foreach (var entry in archive.Entries)
        {
            var destination = Path.GetFullPath(Path.Combine(runtimeRoot, entry.FullName));
            if (!destination.StartsWith(runtimeRoot + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase))
                throw new InvalidOperationException("O pacote interno contém um caminho inválido.");
            if (string.IsNullOrEmpty(entry.Name))
            {
                Directory.CreateDirectory(destination);
                continue;
            }
            Directory.CreateDirectory(Path.GetDirectoryName(destination)!);
            entry.ExtractToFile(destination, true);
        }
        File.WriteAllText(marker, DateTimeOffset.UtcNow.ToString("O"));
    }

    private void EnsureConfiguration()
    {
        if (File.Exists(configPath)) return;
        var secret = Convert.ToBase64String(RandomNumberGenerator.GetBytes(48));
        File.WriteAllText(configPath, $"""
            # Configuração local do ReConstrói
            DATABASE_URL=jdbc:postgresql://localhost:5432/reconstroi
            DATABASE_USERNAME=postgres
            DATABASE_PASSWORD=troque-pela-senha-do-postgres
            JWT_SECRET={secret}
            JWT_EXPIRATION_HOURS=24
            ADMIN_EMAIL=
            ADMIN_PASSWORD=
            MAIL_ENABLED=false
            EXPOSE_RESET_TOKEN=true
            """);
    }

    private Dictionary<string, string> ReadConfiguration()
    {
        var values = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
        foreach (var rawLine in File.ReadAllLines(configPath))
        {
            var line = rawLine.Trim();
            if (line.Length == 0 || line.StartsWith('#')) continue;
            var separator = line.IndexOf('=');
            if (separator <= 0) continue;
            values[line[..separator].Trim()] = line[(separator + 1)..].Trim();
        }

        values["SERVER_PORT"] = "8081";
        values["PORT"] = "3000";
        values["HOSTNAME"] = "127.0.0.1";
        values["CORS_ALLOWED_ORIGINS"] = "http://localhost:3000,http://127.0.0.1:3000";
        values["FRONTEND_URL"] = "http://localhost:3000";
        values["UPLOAD_DIRECTORY"] = Path.Combine(dataRoot, "uploads");
        values["JPA_DDL_AUTO"] = "validate";
        return values;
    }

    private Process StartBackend(Dictionary<string, string> environment)
    {
        var java = Path.Combine(runtimeRoot, "java", "bin", "javaw.exe");
        var jar = Path.Combine(runtimeRoot, "backend", "backend.jar");
        var info = BaseProcess(java, Path.Combine(runtimeRoot, "backend"), environment);
        info.ArgumentList.Add("-Dfile.encoding=UTF-8");
        info.ArgumentList.Add("-jar");
        info.ArgumentList.Add(jar);
        backendLog = NewLog("backend");
        return StartProcess(info, backendLog, "backend");
    }

    private Process StartDesktop(Dictionary<string, string> environment)
    {
        var java = Path.Combine(runtimeRoot, "java", "bin", "javaw.exe");
        var desktopDirectory = Path.Combine(runtimeRoot, "desktop");
        var jar = Path.Combine(desktopDirectory, "reconstroi-desktop.jar");
        var info = BaseProcess(java, desktopDirectory, environment);
        info.ArgumentList.Add("-Dfile.encoding=UTF-8");
        info.ArgumentList.Add("-Dreconstroi.api.url=http://127.0.0.1:8081");
        info.ArgumentList.Add("-jar");
        info.ArgumentList.Add(jar);
        desktopLog = NewLog("desktop");
        desktop = StartProcess(info, desktopLog, "interface desktop");
        return desktop;
    }

    private static ProcessStartInfo BaseProcess(string executable, string workingDirectory,
        Dictionary<string, string> environment)
    {
        var info = new ProcessStartInfo
        {
            FileName = executable,
            WorkingDirectory = workingDirectory,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true
        };
        foreach (var (key, value) in environment) info.Environment[key] = value;
        return info;
    }

    private Process StartProcess(ProcessStartInfo info, StreamWriter log, string name)
    {
        var process = new Process { StartInfo = info, EnableRaisingEvents = true };
        process.OutputDataReceived += (_, eventArgs) => WriteLog(log, eventArgs.Data);
        process.ErrorDataReceived += (_, eventArgs) => WriteLog(log, eventArgs.Data);
        process.Exited += (_, _) =>
        {
            if (!ready) return;
            tray.ShowBalloonTip(4000, "ReConstrói", $"O serviço {name} foi encerrado. Use “Reiniciar serviços”.", ToolTipIcon.Warning);
        };
        if (!process.Start()) throw new InvalidOperationException($"Não foi possível iniciar o serviço {name}.");
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
        return process;
    }

    private StreamWriter NewLog(string service)
    {
        var path = Path.Combine(logsRoot, $"{service}-{DateTime.Now:yyyyMMdd}.log");
        return new StreamWriter(new FileStream(path, FileMode.Append, FileAccess.Write, FileShare.ReadWrite)) { AutoFlush = true };
    }

    private static void WriteLog(StreamWriter writer, string? line)
    {
        if (line == null) return;
        lock (writer) writer.WriteLine($"{DateTimeOffset.Now:O} {line}");
    }

    private static async Task<bool> WaitForPortAsync(int port, Process? process, TimeSpan timeout)
    {
        var until = DateTime.UtcNow + timeout;
        while (DateTime.UtcNow < until)
        {
            if (process is { HasExited: true }) return false;
            if (await IsPortOpenAsync(port)) return true;
            await Task.Delay(500);
        }
        return false;
    }

    private static async Task<bool> IsPortOpenAsync(int port)
    {
        try
        {
            using var client = new TcpClient();
            await client.ConnectAsync(IPAddress.Loopback, port).WaitAsync(TimeSpan.FromSeconds(2));
            return client.Connected;
        }
        catch
        {
            return false;
        }
    }

    private static async Task<bool> IsHealthyAsync(string url)
    {
        try
        {
            using var client = new HttpClient { Timeout = TimeSpan.FromSeconds(2) };
            using var response = await client.GetAsync(url);
            return response.StatusCode is >= HttpStatusCode.OK and < HttpStatusCode.MultipleChoices;
        }
        catch
        {
            return false;
        }
    }

    private async Task RestartAsync()
    {
        if (starting) return;
        StopOwnedProcesses();
        await Task.Delay(500);
        await InitializeAsync();
    }

    private void OpenApplication()
    {
        try
        {
            if (desktop is { HasExited: false }) ActivateDesktop();
            else StartDesktop(ReadConfiguration());
        }
        catch { }
    }

    private void ActivateDesktop()
    {
        var java = Path.Combine(runtimeRoot, "java", "bin", "javaw.exe");
        var desktopDirectory = Path.Combine(runtimeRoot, "desktop");
        var info = new ProcessStartInfo(java)
        {
            WorkingDirectory = desktopDirectory,
            UseShellExecute = false,
            CreateNoWindow = true
        };
        info.ArgumentList.Add("-Dfile.encoding=UTF-8");
        info.ArgumentList.Add("-Dreconstroi.api.url=http://127.0.0.1:8081");
        info.ArgumentList.Add("-jar");
        info.ArgumentList.Add(Path.Combine(desktopDirectory, "reconstroi-desktop.jar"));
        Process.Start(info)?.Dispose();
    }

    private static void OpenUrl(string url)
    {
        try { Process.Start(new ProcessStartInfo(url) { UseShellExecute = true }); }
        catch { }
    }

    private static void OpenFile(string path)
    {
        try { Process.Start(new ProcessStartInfo("notepad.exe", $"\"{path}\"") { UseShellExecute = true }); }
        catch { }
    }

    private static void OpenFolder(string path)
    {
        Directory.CreateDirectory(path);
        try { Process.Start(new ProcessStartInfo("explorer.exe", $"\"{path}\"") { UseShellExecute = true }); }
        catch { }
    }

    private void StopOwnedProcesses()
    {
        ready = false;
        StopProcess(desktop);
        StopProcess(backend);
        desktop = null;
        backend = null;
        desktopLog?.Dispose();
        backendLog?.Dispose();
        desktopLog = null;
        backendLog = null;
    }

    private static void StopProcess(Process? process)
    {
        if (process == null) return;
        try
        {
            if (!process.HasExited)
            {
                process.Kill(true);
                process.WaitForExit(5000);
            }
        }
        catch { }
        finally { process.Dispose(); }
    }

    protected override void ExitThreadCore()
    {
        StopOwnedProcesses();
        tray.Visible = false;
        tray.Dispose();
        base.ExitThreadCore();
    }
}
