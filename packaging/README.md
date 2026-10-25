# Executável Windows — ReConstrói Desktop

O arquivo final fica em `dist/ReConstroi.exe`. O pacote Windows 64 bits inclui PostgreSQL, runtime Java 17, backend Spring Boot e interface Java Swing no padrão `VIEW → DTO → DAO`.

## Uso

1. Dê dois cliques em `ReConstroi.exe`.
2. Aguarde a preparação do banco e a abertura da janela de login.
3. Clique em **Criar conta**, escolha um perfil de doador e entre no sistema.
4. Na tela **Materiais**, cadastre um material com foto, consulte, edite e exclua para demonstrar o CRUD.
   No cadastro, arraste as fotos do Explorador para a área de fotos ou use **Selecionar fotos**.
   São aceitos JPG, PNG e WebP, até cinco fotos de 5 MB cada. **Remover selecionadas** retira arquivos da seleção, sem apagá-los do computador.
5. Use o ícone do ReConstrói na bandeja para reabrir a janela, reiniciar, consultar logs ou sair.

O programa abre somente janelas nativas, sem navegador e sem terminais. Não precisa instalar Java, .NET, PostgreSQL, Node.js ou pgAdmin separadamente. Depois de obter o executável, a apresentação local funciona sem internet.

Os dados ficam em `%USERPROFILE%\.reconstroi`, preservados entre execuções:

- `postgres-data`: banco PostgreSQL;
- `data/uploads`: fotos dos materiais;
- `reconstroi.env`: configuração local;
- `logs`: diagnóstico;
- `runtime-v3/<identificador do pacote>`: componentes extraídos. Atualizações não apagam os componentes anteriores nem os dados.

O banco local padrão é `reconstroi`, usuário `postgres`, senha `123`, porta `5432`. A API interna usa `127.0.0.1:8081`. Os serviços atendem apenas no computador local. Configurações existentes são preservadas; um banco externo configurado continua sob administração do usuário.

Fechar a janela permite reabri-la pela bandeja. Para encerrar todos os serviços iniciados pelo aplicativo, use **Sair** no menu da bandeja. Servidores que já estavam rodando antes do aplicativo não são encerrados por ele.

Copiar somente o executável para outro computador leva o programa. Os cadastros permanecem no computador em que foram criados. A distribuição usa PostgreSQL 18: clusters locais de outra versão são preservados e exigem migração.

## Reconstrução

Requisitos somente para quem compila o pacote:

- Windows x64 e PowerShell com `robocopy`;
- JDK 17 completo, incluindo `jlink`;
- .NET SDK 10 no PATH, em `packaging/tools/dotnet`, ou informado por `-DotnetPath`;
- PostgreSQL portátil x64 com `bin`, `lib` e `share`, por padrão em `%USERPROFILE%\Downloads\pgsql`;
- acesso ao Maven e NuGet quando as dependências ainda não estiverem no cache.

```powershell
powershell -ExecutionPolicy Bypass -File .\packaging\build-windows.ps1
```

Parâmetros opcionais: `-JdkRoot`, `-PostgresRoot`, `-DotnetPath`. `-SkipApplicationBuild` reutiliza os JARs de `backend/target` e `desktop/target`. O frontend web não faz parte deste executável.

Cada build recebe uma pasta própria em `packaging/work-v3`. O executável anterior é preservado em `dist/backups` antes da substituição; o hash fica em `dist/ReConstroi.exe.sha256.txt`. O banco pessoal não é incluído no pacote.

O arquivo não possui assinatura digital. Para diagnóstico automatizado, `--no-open` inicia os serviços sem abrir a janela e `--stop` solicita o encerramento. `RECONSTROI_APP_ROOT` permite testar em uma pasta de dados separada.

