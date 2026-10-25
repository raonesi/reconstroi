param(
    [string]$PostgresBin = (Join-Path $env:USERPROFILE 'Downloads\pgsql\bin'),
    [string]$DataDirectory = (Join-Path $env:USERPROFILE '.reconstroi\postgres-data')
)

$ErrorActionPreference = 'Stop'
$pgCtl = Join-Path $PostgresBin 'pg_ctl.exe'
if (-not (Test-Path -LiteralPath $pgCtl)) {
    throw "PostgreSQL nao encontrado em $PostgresBin. Informe -PostgresBin com a pasta bin correta."
}
if (-not (Test-Path -LiteralPath (Join-Path $DataDirectory 'PG_VERSION'))) {
    throw "Banco ainda nao inicializado em $DataDirectory. Este comando apenas inicia um banco existente."
}

# Uma verificacao pela porta funciona mesmo quando o pg_ctl nao consegue
# consultar o processo por causa das permissoes do Windows.
$pgIsReady = Join-Path $PostgresBin 'pg_isready.exe'
if (Test-Path -LiteralPath $pgIsReady) {
    & $pgIsReady -h localhost -p 5432 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host 'O PostgreSQL do ReConstroi ja esta ligado.'
        exit 0
    }
}

& $pgCtl status -D $DataDirectory
if ($LASTEXITCODE -eq 0) {
    Write-Host 'O PostgreSQL do ReConstroi ja esta ligado.'
    exit 0
}
if ($LASTEXITCODE -ne 3) { throw 'Nao foi possivel verificar o estado do PostgreSQL.' }

# Se o Windows foi encerrado enquanto o banco estava ligado, pode sobrar um
# postmaster.pid antigo. Removemos somente quando nao existe mais o processo
# indicado e a porta do banco esta livre.
$pidFile = Join-Path $DataDirectory 'postmaster.pid'
if (Test-Path -LiteralPath $pidFile) {
    $oldPid = 0
    $pidText = Get-Content -LiteralPath $pidFile -TotalCount 1 -ErrorAction SilentlyContinue
    [int]::TryParse($pidText, [ref]$oldPid) | Out-Null
    $oldProcess = if ($oldPid -gt 0) { Get-Process -Id $oldPid -ErrorAction SilentlyContinue } else { $null }
    $portOwner = Get-NetTCPConnection -LocalPort 5432 -State Listen -ErrorAction SilentlyContinue
    if (-not $oldProcess -and -not $portOwner) {
        Remove-Item -LiteralPath $pidFile -Force
        Write-Host 'Arquivo de bloqueio antigo do PostgreSQL removido.'
    }
}

$logPath = Join-Path (Split-Path -Parent $DataDirectory) 'postgres.log'
# Start-Process recebe uma linha de comando do Windows. As aspas abaixo
# protegem os caminhos e fazem -o chegar como um unico argumento ao pg_ctl.
$arguments = 'start -D "{0}" -l "{1}" -o "-h localhost -p 5432" -w -t 30' -f $DataDirectory, $logPath
$launcher = Start-Process -FilePath $pgCtl -ArgumentList $arguments -WindowStyle Hidden -PassThru
# Espera apenas pg_ctl, e nao toda a arvore de processos do servidor.
if (-not $launcher.WaitForExit(35000)) { throw "Tempo esgotado. Consulte $logPath" }
if ($launcher.ExitCode -ne 0) { throw "Falha ao iniciar PostgreSQL. Consulte $logPath" }
Write-Host 'PostgreSQL iniciado em localhost:5432. Banco: reconstroi.'
exit 0
