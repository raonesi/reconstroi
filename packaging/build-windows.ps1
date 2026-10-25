param(
    [switch]$SkipApplicationBuild,
    [string]$JdkRoot,
    [string]$PostgresRoot = (Join-Path $env:USERPROFILE 'Downloads\pgsql'),
    [string]$DotnetPath
)

$ErrorActionPreference = 'Stop'
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$workRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'work-v3'))
$buildId = (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [Guid]::NewGuid().ToString('N').Substring(0, 8)
$buildRoot = Join-Path $workRoot $buildId
$payloadRoot = Join-Path $buildRoot 'payload'
$runtimeOutput = Join-Path $payloadRoot 'java'
$launcherRoot = Join-Path $PSScriptRoot 'ReConstroi.Launcher'
$publishRoot = Join-Path $buildRoot 'publish'
$distRoot = Join-Path $projectRoot 'dist'

function Assert-UnderProject([string]$Path) {
    $resolved = [System.IO.Path]::GetFullPath($Path)
    if (-not $resolved.StartsWith($projectRoot + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Caminho fora do projeto: $resolved"
    }
}

function Find-Executable([string]$ExplicitPath, [string]$CommandName, [string[]]$Fallbacks) {
    if ($ExplicitPath) {
        if (-not (Test-Path -LiteralPath $ExplicitPath -PathType Leaf)) { throw "Executavel nao encontrado: $ExplicitPath" }
        return [System.IO.Path]::GetFullPath($ExplicitPath)
    }
    $command = Get-Command -Name $CommandName -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($command) { return $command.Source }
    foreach ($candidate in $Fallbacks) {
        if (Test-Path -LiteralPath $candidate -PathType Leaf) { return $candidate }
    }
    throw "$CommandName nao encontrado. Informe o caminho no parametro correspondente do script de build."
}

function Copy-RuntimeDirectory([string]$Source, [string]$Destination) {
    if (-not (Test-Path -LiteralPath $Source -PathType Container)) { throw "Diretorio ausente: $Source" }
    Assert-UnderProject $Destination
    # Sem /MIR ou /PURGE: nunca apaga as pastas de origem ou builds anteriores.
    & robocopy.exe $Source $Destination /E /R:2 /W:1 /NFL /NDL /NJH /NJS /NP /XD .git .svn .hg /XF .git .gitignore .gitattributes .DS_Store | Out-Host
    if ($LASTEXITCODE -ge 8) { throw "Falha ao copiar runtime de $Source (robocopy $LASTEXITCODE)." }
}

if (-not $JdkRoot) {
    $jdkCandidates = @(
        $env:JAVA_HOME,
        'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot',
        'C:\Program Files\Java\jdk-17'
    )
    foreach ($candidate in $jdkCandidates) {
        if ($candidate -and (Test-Path -LiteralPath (Join-Path $candidate 'bin\jlink.exe'))) {
            $releaseFile = Join-Path $candidate 'release'
            if ((Test-Path -LiteralPath $releaseFile) -and ((Get-Content -LiteralPath $releaseFile -Raw) -match 'JAVA_VERSION="17[."]')) {
                $JdkRoot = $candidate
                break
            }
        }
    }
}
if (-not $JdkRoot -or -not (Test-Path -LiteralPath (Join-Path $JdkRoot 'bin\jlink.exe'))) {
    throw 'JDK 17 com jlink nao encontrado. Informe -JdkRoot.'
}
$jdkRelease = Join-Path $JdkRoot 'release'
if (-not (Test-Path -LiteralPath $jdkRelease) -or (Get-Content -LiteralPath $jdkRelease -Raw) -notmatch 'JAVA_VERSION="17[."]') {
    throw 'O runtime empacotado deve ser gerado com JDK 17.'
}
$jdkBin = Join-Path $JdkRoot 'bin'
$DotnetPath = Find-Executable $DotnetPath 'dotnet.exe' @((Join-Path $PSScriptRoot 'tools\dotnet\dotnet.exe'))
$PostgresRoot = [System.IO.Path]::GetFullPath($PostgresRoot)
foreach ($required in @('bin\postgres.exe', 'bin\initdb.exe', 'bin\pg_ctl.exe', 'bin\psql.exe', 'bin\createdb.exe', 'lib', 'share')) {
    if (-not (Test-Path -LiteralPath (Join-Path $PostgresRoot $required))) {
        throw "Distribuicao PostgreSQL incompleta: $PostgresRoot\$required"
    }
}

Assert-UnderProject $buildRoot
Assert-UnderProject $distRoot
# Cada tentativa recebe uma pasta propria. Builds anteriores e dist sao preservados.
New-Item -ItemType Directory -Path $payloadRoot, $publishRoot, $distRoot -Force | Out-Null
Write-Host "Arquivos desta compilacao: $buildRoot"

$previousJavaHome = $env:JAVA_HOME
$previousPath = $env:Path
try {
    $env:JAVA_HOME = $JdkRoot
    $env:Path = "$jdkBin;$previousPath"

    if (-not $SkipApplicationBuild) {
        Push-Location (Join-Path $projectRoot 'backend')
        try {
            & '.\mvnw.cmd' -q -DskipTests package
            if ($LASTEXITCODE -ne 0) { throw 'Falha ao compilar o backend.' }
            & '.\mvnw.cmd' -q -f '..\desktop\pom.xml' -DskipTests package
            if ($LASTEXITCODE -ne 0) { throw 'Falha ao compilar o aplicativo desktop.' }
        }
        finally { Pop-Location }
    }

    $backendJar = Get-ChildItem -LiteralPath (Join-Path $projectRoot 'backend\target') -Filter 'backend-*.jar' |
        Where-Object { $_.Name -notmatch '-(sources|javadoc|tests)\.jar$' } |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1
    if (-not $backendJar) { throw 'JAR do backend nao encontrado.' }
    $desktopJar = Join-Path $projectRoot 'desktop\target\reconstroi-desktop.jar'
    if (-not (Test-Path -LiteralPath $desktopJar)) { throw 'JAR do aplicativo desktop nao encontrado.' }

    $jlinkArguments = @(
        '--add-modules', 'java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.security.jgss,java.sql,jdk.crypto.ec,jdk.management,jdk.unsupported',
        '--strip-debug', '--no-header-files', '--no-man-pages', '--compress=2', '--output', $runtimeOutput
    )
    & (Join-Path $jdkBin 'jlink.exe') @jlinkArguments
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar o runtime Java.' }

    foreach ($directory in @('backend', 'desktop', 'postgres')) {
        New-Item -ItemType Directory -Path (Join-Path $payloadRoot $directory) -Force | Out-Null
    }
    Copy-Item -LiteralPath $backendJar.FullName -Destination (Join-Path $payloadRoot 'backend\backend.jar')
    Copy-Item -LiteralPath $desktopJar -Destination (Join-Path $payloadRoot 'desktop\reconstroi-desktop.jar')
    foreach ($directory in @('bin', 'lib', 'share')) {
        Copy-RuntimeDirectory (Join-Path $PostgresRoot $directory) (Join-Path $payloadRoot "postgres\$directory")
    }
    foreach ($license in Get-ChildItem -LiteralPath $PostgresRoot -File | Where-Object { $_.Name -match '^(server.*license|commandlinetools.*license|license|copying|copyright)' }) {
        Copy-Item -LiteralPath $license.FullName -Destination (Join-Path $payloadRoot "postgres\$($license.Name)")
    }
    # Empacota somente os runtimes e a interface desktop.
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $stagedPayloadZip = Join-Path $buildRoot 'payload.zip'
    [System.IO.Compression.ZipFile]::CreateFromDirectory($payloadRoot, $stagedPayloadZip, [System.IO.Compression.CompressionLevel]::Optimal, $false)
    Copy-Item -LiteralPath $stagedPayloadZip -Destination (Join-Path $launcherRoot 'payload.zip') -Force
    Copy-Item -LiteralPath (Join-Path $projectRoot 'front\app\favicon.ico') -Destination (Join-Path $launcherRoot 'reconstroi.ico') -Force

    $launcherProject = Join-Path $launcherRoot 'ReConstroi.Launcher.csproj'
    & $DotnetPath restore $launcherProject --configfile (Join-Path $PSScriptRoot 'NuGet.Config') -r win-x64
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao restaurar o launcher. O build requer .NET SDK 10 e acesso ao NuGet.' }
    & $DotnetPath publish $launcherProject -c Release -r win-x64 --self-contained true --no-restore -o $publishRoot
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao publicar o launcher.' }
    $publishedExe = Join-Path $publishRoot 'ReConstroi.exe'
    if (-not (Test-Path -LiteralPath $publishedExe -PathType Leaf)) { throw 'O publish nao produziu ReConstroi.exe.' }

    $distExe = Join-Path $distRoot 'ReConstroi.exe'
    $stagedExe = Join-Path $distRoot "ReConstroi.$buildId.tmp"
    Assert-UnderProject $stagedExe
    Copy-Item -LiteralPath $publishedExe -Destination $stagedExe
    $publishedHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $publishedExe).Hash
    if ((Get-FileHash -Algorithm SHA256 -LiteralPath $stagedExe).Hash -ne $publishedHash) { throw 'Falha ao conferir a copia do executavel.' }
    if (Test-Path -LiteralPath $distExe) {
        $backupRoot = Join-Path $distRoot 'backups'
        New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null
        $backupExe = Join-Path $backupRoot "ReConstroi-$buildId.exe"
        Assert-UnderProject $backupExe
        # Substituicao atomica: o executavel anterior fica guardado no backup.
        [System.IO.File]::Replace($stagedExe, $distExe, $backupExe)
        Write-Host "Executavel anterior preservado: $backupExe"
    } else {
        Assert-UnderProject $distExe
        Move-Item -LiteralPath $stagedExe -Destination $distExe
    }
    Set-Content -LiteralPath (Join-Path $distRoot 'ReConstroi.exe.sha256.txt') -Value "$publishedHash  ReConstroi.exe"
    $sizeMb = [math]::Round((Get-Item -LiteralPath $distExe).Length / 1MB, 1)
    Write-Host "Executavel criado: $distExe ($sizeMb MB)"
}
finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:Path = $previousPath
}

