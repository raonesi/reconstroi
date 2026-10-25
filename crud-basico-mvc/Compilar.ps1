$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$jdk = $env:JAVA_HOME
if (-not $jdk) { $jdk = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot' }
New-Item -ItemType Directory -Path build -Force | Out-Null
$fontes = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName
& "$jdk\bin\javac.exe" -encoding UTF-8 --release 17 -d build $fontes
if ($LASTEXITCODE -ne 0) { throw 'Falha na compilacao.' }
if (-not (Test-Path 'lib/postgresql-42.7.5.jar')) { throw 'Driver PostgreSQL ausente na pasta lib.' }
& "$jdk\bin\jar.exe" --create --file CrudBasico.jar --manifest manifest.mf -C build .
if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar o JAR.' }
Write-Output 'Pronto. Abra Iniciar.cmd.'
