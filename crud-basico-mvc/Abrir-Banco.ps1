$ErrorActionPreference = 'Stop'
$psql = Join-Path $env:USERPROFILE 'Downloads\pgsql\bin\psql.exe'
if (-not (Test-Path $psql)) { $psql = (Get-Command psql.exe -ErrorAction Stop).Source }
$config = ConvertFrom-StringData (Get-Content (Join-Path $PSScriptRoot 'banco.properties') -Raw)
$url = [Uri]($config.url -replace '^jdbc:', '')
$senhaAnterior = $env:PGPASSWORD
$configAnterior = $env:PSQLRC
try {
    $env:PGPASSWORD = $config.senha
    $env:PSQLRC = Join-Path $PSScriptRoot 'sql\apresentar.psql'
    Start-Process -FilePath $psql -ArgumentList @('-h', $url.Host, '-p', $url.Port,
        '-U', $config.usuario, '-d', $url.AbsolutePath.TrimStart('/'))
} finally {
    $env:PGPASSWORD = $senhaAnterior
    $env:PSQLRC = $configAnterior
}
