param(
    [switch]$SkipApplicationBuild
)

$ErrorActionPreference = 'Stop'
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$workRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'work'))
$payloadRoot = Join-Path $workRoot 'payload'
$runtimeOutput = Join-Path $payloadRoot 'java'
$launcherRoot = Join-Path $PSScriptRoot 'ReConstroi.Launcher'
$publishRoot = Join-Path $workRoot 'publish'
$distRoot = Join-Path $projectRoot 'dist'

function Assert-UnderProject([string]$Path) {
    $resolved = [System.IO.Path]::GetFullPath($Path)
    if (-not $resolved.StartsWith($projectRoot + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Caminho fora do projeto: $resolved"
    }
}

Assert-UnderProject $workRoot
Assert-UnderProject $distRoot
if (Test-Path -LiteralPath $workRoot) { Remove-Item -LiteralPath $workRoot -Recurse -Force }
if (Test-Path -LiteralPath (Join-Path $distRoot 'ReConstroi.exe')) {
    # Guarda o executável anterior para não perdê-lo caso o build falhe.
    # Move os ARQUIVOS (não a pasta): a pasta pode estar aberta pelo OneDrive/Explorer e não poder ser movida.
    $backupRoot = Join-Path $projectRoot 'dist-anterior'
    Assert-UnderProject $backupRoot
    if (Test-Path -LiteralPath $backupRoot) { Remove-Item -LiteralPath $backupRoot -Recurse -Force }
    New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null
    Get-ChildItem -LiteralPath $distRoot -Force | Move-Item -Destination $backupRoot
}
New-Item -ItemType Directory -Path $payloadRoot, $publishRoot, $distRoot -Force | Out-Null

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
    Where-Object { $_.Name -notlike '*.original' } |
    Sort-Object Length -Descending |
    Select-Object -First 1
if (-not $backendJar) { throw 'JAR do backend não encontrado.' }

$desktopJar = Join-Path $projectRoot 'desktop\target\reconstroi-desktop.jar'
if (-not (Test-Path -LiteralPath $desktopJar)) { throw 'JAR do aplicativo desktop não encontrado.' }

$jdkBin = if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\jlink.exe'))) {
    Join-Path $env:JAVA_HOME 'bin'
} elseif (Test-Path -LiteralPath 'C:\Program Files\Java\jdk-17\bin\jlink.exe') {
    'C:\Program Files\Java\jdk-17\bin'
} else {
    throw 'JDK 17 com jlink não encontrado.'
}

& (Join-Path $jdkBin 'jlink.exe') `
    --add-modules 'java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.security.jgss,java.sql,jdk.crypto.ec,jdk.management,jdk.unsupported' `
    --strip-debug --no-header-files --no-man-pages --compress=2 --output $runtimeOutput
if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar o runtime Java.' }

New-Item -ItemType Directory -Path (Join-Path $payloadRoot 'backend'), (Join-Path $payloadRoot 'desktop') -Force | Out-Null
Copy-Item -LiteralPath $backendJar.FullName -Destination (Join-Path $payloadRoot 'backend\backend.jar')
Copy-Item -LiteralPath $desktopJar -Destination (Join-Path $payloadRoot 'desktop\reconstroi-desktop.jar')

$payloadZip = Join-Path $launcherRoot 'payload.zip'
if (Test-Path -LiteralPath $payloadZip) { Remove-Item -LiteralPath $payloadZip -Force }
Compress-Archive -Path (Join-Path $payloadRoot '*') -DestinationPath $payloadZip -CompressionLevel Optimal

$favicon = Join-Path $projectRoot 'front\app\favicon.ico'
Copy-Item -LiteralPath $favicon -Destination (Join-Path $launcherRoot 'reconstroi.ico') -Force

$launcherProject = Join-Path $launcherRoot 'ReConstroi.Launcher.csproj'
& dotnet restore $launcherProject --configfile (Join-Path $PSScriptRoot 'NuGet.Config') -r win-x64
if ($LASTEXITCODE -ne 0) { throw 'Falha ao restaurar o launcher.' }
& dotnet publish $launcherProject -c Release -r win-x64 --self-contained true --no-restore -o $publishRoot
if ($LASTEXITCODE -ne 0) { throw 'Falha ao publicar o launcher.' }
Copy-Item -LiteralPath (Join-Path $publishRoot 'ReConstroi.exe') -Destination (Join-Path $distRoot 'ReConstroi.exe')

$hash = Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $distRoot 'ReConstroi.exe')
Set-Content -LiteralPath (Join-Path $distRoot 'ReConstroi.exe.sha256.txt') -Value "$($hash.Hash)  ReConstroi.exe"
$sizeMb = [math]::Round((Get-Item -LiteralPath (Join-Path $distRoot 'ReConstroi.exe')).Length / 1MB, 1)
Write-Host "Executável criado: $(Join-Path $distRoot 'ReConstroi.exe') ($sizeMb MB)"
