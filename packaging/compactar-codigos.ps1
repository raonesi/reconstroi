$ErrorActionPreference = 'Stop'
$raiz = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$destino = Join-Path $raiz 'entrega'
New-Item -ItemType Directory -Path $destino -Force | Out-Null
$zip = Join-Path $destino ('ReConstroi-Codigos-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.zip')

# Somente fontes e recursos do projeto: nao inclui banco, uploads ou dependencias.
$pastas = @('backend/src', 'backend/.mvn', 'backend/service', 'desktop/src',
    'front/app', 'front/components', 'front/lib', 'front/public', 'front/scripts')
$arquivos = @()
foreach ($pasta in $pastas) {
    $caminho = Join-Path $raiz $pasta
    if (Test-Path -LiteralPath $caminho) {
        $arquivos += Get-ChildItem -LiteralPath $caminho -File -Recurse -Force
    }
}
foreach ($pasta in @('', 'backend', 'desktop', 'front', 'packaging', 'packaging/ReConstroi.Launcher')) {
    $arquivos += Get-ChildItem -LiteralPath (Join-Path $raiz $pasta) -File -Force |
        Where-Object {
            $_.Name -notin @('AGENTS.md', 'CLAUDE.md', 'next-env.d.ts') -and
            ($_.Extension -in @('.md', '.java', '.cs', '.csproj', '.xml', '.json', '.yaml', '.yml',
                '.ps1', '.cmd', '.ts', '.mjs', '.manifest', '.ico', '.asta') -or
             $_.Name -in @('mvnw', 'Dockerfile', '.gitignore', '.gitattributes', '.dockerignore', '.env.example', 'NuGet.Config'))
        }
}
$arquivos = @($arquivos | Sort-Object FullName -Unique | Where-Object {
    $_.Name -notmatch '^\.env(\.|$)' -or $_.Name -eq '.env.example'
})

Add-Type -AssemblyName System.IO.Compression.FileSystem
$pacote = [IO.Compression.ZipFile]::Open($zip, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($arquivo in $arquivos) {
        $relativo = $arquivo.FullName.Substring($raiz.Length + 1).Replace('\', '/')
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($pacote, $arquivo.FullName,
            ('ReConstroi/' + $relativo), [IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
} finally {
    $pacote.Dispose()
}

# Confere nomes e conteudo de cada arquivo, inclusive todas as classes Java.
$pacote = [IO.Compression.ZipFile]::OpenRead($zip)
try {
    if ($pacote.Entries.Count -ne $arquivos.Count) { throw 'Quantidade incorreta de arquivos no ZIP.' }
    foreach ($arquivo in $arquivos) {
        $relativo = $arquivo.FullName.Substring($raiz.Length + 1).Replace('\', '/')
        $entrada = $pacote.GetEntry('ReConstroi/' + $relativo)
        $fluxo = $entrada.Open()
        $sha = [Security.Cryptography.SHA256]::Create()
        try {
            $hash = [BitConverter]::ToString($sha.ComputeHash($fluxo)).Replace('-', '')
            if ($hash -ne (Get-FileHash -LiteralPath $arquivo.FullName -Algorithm SHA256).Hash) {
                throw "Arquivo diferente no ZIP: $relativo"
            }
        } finally { $fluxo.Dispose(); $sha.Dispose() }
    }
} finally { $pacote.Dispose() }

Write-Output "ZIP conferido: $zip"
Write-Output "Arquivos: $($arquivos.Count)"
Write-Output "Fontes Java: $(@($arquivos | Where-Object Extension -eq '.java').Count)"
Write-Output "Tamanho: $([math]::Round((Get-Item -LiteralPath $zip).Length / 1MB, 2)) MB"
