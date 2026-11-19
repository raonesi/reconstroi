# Executável Windows — ReConstrói Desktop

O arquivo final fica em `dist/ReConstroi.exe`. Ele é um aplicativo Windows 64 bits autoextraível que contém:

- runtime Java 17 reduzido;
- backend Spring Boot;
- cliente Java Swing organizado em `VIEW`, `DTO` e `DAO`;
- launcher com ícone na bandeja, logs e gerenciamento do backend.

## Uso

1. Garanta que o serviço do PostgreSQL esteja ativo e que exista o banco `reconstroi`.
2. Dê dois cliques em `ReConstroi.exe`.
3. Aguarde a primeira extração; a janela nativa de login será aberta.
4. Use o ícone do ReConstrói na bandeja para abrir novamente, reiniciar, editar a configuração, consultar logs ou encerrar.

Não é necessário manter o pgAdmin aberto. O pgAdmin é apenas uma ferramenta administrativa; somente o serviço PostgreSQL precisa estar em execução.

Na primeira execução são criados:

- `%LOCALAPPDATA%\ReConstroi\reconstroi.env`: configuração do PostgreSQL, JWT, administrador e e-mail;
- `%LOCALAPPDATA%\ReConstroi\data`: fotos enviadas;
- `%LOCALAPPDATA%\ReConstroi\logs`: logs da API e do aplicativo;
- `%LOCALAPPDATA%\ReConstroi\runtime-vN`: componentes extraídos do executável (N = `AppVersion` do launcher; versões antigas podem ser apagadas).

O padrão local é PostgreSQL em `localhost:5432`, banco `reconstroi`, usuário `postgres`. Na primeira execução o `reconstroi.env` vem com `DATABASE_PASSWORD=troque-pela-senha-do-postgres`: troque pela senha real e reinicie. A API do executável escuta apenas em `127.0.0.1:8081`.

Passo a passo completo (requisitos, banco, NetBeans, problemas comuns): [README principal](../README.md).

## Reconstrução

Execute na raiz do projeto:

```powershell
powershell -ExecutionPolicy Bypass -File .\packaging\build-windows.ps1
```

O executável não possui assinatura digital; o Windows SmartScreen pode exibir um aviso de editor desconhecido.
