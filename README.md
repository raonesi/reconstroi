# ReConstrói

Plataforma de reaproveitamento de materiais de construção. Conecta doadores (pessoas e construtoras) a beneficiários (ONGs e famílias) e acompanha cada material do anúncio até a doação finalizada, calculando o impacto ambiental (resíduos e CO₂ evitados).

Este guia tem tudo o que outra pessoa precisa para **usar**, **desenvolver** e **gerar o executável** do projeto no Windows.

---

## Sumário

1. [O que tem neste repositório](#1-o-que-tem-neste-repositório)
2. [Só quero usar o programa (ReConstroi.exe)](#2-só-quero-usar-o-programa-reconstroiexe)
3. [Quero abrir o código e rodar (NetBeans)](#3-quero-abrir-o-código-e-rodar-netbeans)
4. [Gerar o ReConstroi.exe](#4-gerar-o-reconstroiexe)
5. [Testes](#5-testes)
6. [Configuração (reconstroi.env)](#6-configuração-reconstroienv)
7. [Segurança](#7-segurança)
8. [Problemas comuns](#8-problemas-comuns)
9. [Regras de negócio](#9-regras-de-negócio)
10. [Estrutura das pastas](#10-estrutura-das-pastas)
11. [Versão web e Docker (opcional)](#11-versão-web-e-docker-opcional)

---

## 1. O que tem neste repositório

| Pasta | O que é | Tecnologia |
|---|---|---|
| `backend/` | API REST com todas as regras de negócio, login (JWT) e banco | Spring Boot 3.4, Java 17, Flyway |
| `desktop/` | Aplicativo de janelas no padrão acadêmico `VIEW → DTO → DAO` | Java Swing, FlatLaf, OpenPDF |
| `packaging/` | Gera o `ReConstroi.exe` (Java + API + desktop num arquivo só) | .NET 10 + PowerShell |
| `ReConstroi-Diagrama-Banco.asta` | Diagrama do banco de dados | Astah |

O banco de dados é **PostgreSQL**. As tabelas são criadas sozinhas na primeira vez que a API sobe (migrations do Flyway em `backend/src/main/resources/db/migration`).

> O desktop **não** acessa o banco direto: ele conversa com a API, que aplica login, permissões e regras. Por isso a API precisa estar rodando para o desktop funcionar.

---

## 2. Só quero usar o programa (ReConstroi.exe)

### Requisitos

- Windows 10 ou 11, 64 bits.
- **PostgreSQL** 15 ou mais novo (testado no 18). Download: https://www.postgresql.org/download/windows/
  - Na instalação, anote a **senha do usuário `postgres`** que você escolher.
  - O pgAdmin **não** precisa ficar aberto; basta o serviço do PostgreSQL estar ativo.
- Não precisa instalar Java: ele já vem dentro do `.exe`.

### Passo a passo

**1. Criar o banco `reconstroi`** (uma vez só). No PowerShell (ajuste o `18` para a sua versão do PostgreSQL):

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE reconstroi;"
```

Ele pede a senha do `postgres`.

**2. Conseguir o `ReConstroi.exe`.** O arquivo não fica no Git (tem ~200 MB). Peça a quem já tem, ou gere seguindo a [seção 4](#4-gerar-o-reconstroiexe). Ele fica em `dist\ReConstroi.exe`.

**3. Abrir uma vez** com dois cliques. Na primeira execução ele cria a pasta `%LOCALAPPDATA%\ReConstroi` com o arquivo de configuração `reconstroi.env`.

**4. Informar a senha do banco.** Abra `%LOCALAPPDATA%\ReConstroi\reconstroi.env` no Bloco de Notas (ou pelo ícone do ReConstrói na bandeja → editar configuração) e troque:

```text
DATABASE_PASSWORD=troque-pela-senha-do-postgres
```

pela senha do `postgres` que você definiu. Salve e reinicie o ReConstrói pelo ícone da bandeja (ou feche e abra de novo).

**5. Pronto.** A tela de login abre em alguns segundos. Clique em **"Cadastre-se grátis"** para criar sua conta.

### Criar um administrador

Contas de administrador não podem ser criadas pelo cadastro. No `reconstroi.env`, preencha:

```text
ADMIN_EMAIL=admin@seudominio.com
ADMIN_PASSWORD=UmaSenhaForteDeVerdade!
```

e reinicie o ReConstrói. O administrador é criado uma vez (se o e-mail ainda não existir). Depois você pode apagar a senha do arquivo.

### Onde ficam as coisas

| Caminho | Conteúdo |
|---|---|
| `%LOCALAPPDATA%\ReConstroi\reconstroi.env` | Configuração (senha do banco, chave JWT, e-mail) |
| `%LOCALAPPDATA%\ReConstroi\data\uploads` | Fotos dos materiais |
| `%LOCALAPPDATA%\ReConstroi\logs` | Logs da API e do aplicativo |
| `%LOCALAPPDATA%\ReConstroi\runtime-vN` | Arquivos extraídos do `.exe` (pode apagar versões antigas) |

---

## 3. Quero abrir o código e rodar (NetBeans)

### Requisitos

| Programa | Para quê | Download |
|---|---|---|
| **JDK 17** | Compilar e rodar | https://learn.microsoft.com/java/openjdk/download (Microsoft Build of OpenJDK 17) |
| **Apache NetBeans 25** (ou outra IDE com Maven) | Abrir e rodar os projetos | https://netbeans.apache.org/front/main/download/ |
| **PostgreSQL** 15+ | Banco de dados | https://www.postgresql.org/download/windows/ |
| **Git** | Baixar o código | https://git-scm.com/download/win |

Não precisa instalar Maven: o projeto usa o Maven Wrapper (`backend\mvnw.cmd`), e o NetBeans já traz um Maven embutido.

### Passo a passo

**1. Baixar o código**

```powershell
git clone -b nova-versao https://github.com/raonesi/reconstroi.git
cd reconstroi
```

**2. Criar o banco** (igual ao [passo 1 da seção 2](#passo-a-passo)).

**3. Criar o arquivo de configuração.** O backend lê automaticamente `%LOCALAPPDATA%\ReConstroi\reconstroi.env` (o mesmo arquivo do `.exe`). Se você já abriu o `.exe` nesta máquina, ele existe — só confira a senha. Se não, crie com este comando (troque `SUA_SENHA_DO_POSTGRES`):

```powershell
$b = New-Object byte[] 48; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
New-Item -ItemType Directory -Force "$env:LOCALAPPDATA\ReConstroi" | Out-Null
@"
DATABASE_URL=jdbc:postgresql://localhost:5432/reconstroi
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=SUA_SENHA_DO_POSTGRES
JWT_SECRET=$([Convert]::ToBase64String($b))
JWT_EXPIRATION_HOURS=24
ADMIN_EMAIL=
ADMIN_PASSWORD=
MAIL_ENABLED=false
EXPOSE_RESET_TOKEN=true
"@ | Set-Content -Encoding ASCII "$env:LOCALAPPDATA\ReConstroi\reconstroi.env"
```

> Nunca coloque esse arquivo no Git. Ele fica fora da pasta do projeto justamente por isso.

**4. Abrir no NetBeans:** `File → Open Project` e abra a pasta `backend`. Repita para a pasta `desktop`. Na primeira vez o NetBeans baixa as bibliotecas (alguns minutos).

**5. Rodar, nesta ordem:**

1. Botão direito em **backend → Run**. Espere aparecer `Started BackendApplication` na janela Output. A API fica em `http://127.0.0.1:8081`.
2. Botão direito em **reconstroi-desktop → Run**. A tela de login abre.

> Se o `ReConstroi.exe` estiver aberto, feche-o antes: os dois usam a porta 8081.

### Rodar sem IDE (linha de comando)

Terminal 1 — API:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Terminal 2 — desktop:

```powershell
cd backend
.\mvnw.cmd -f ..\desktop\pom.xml package
java -jar ..\desktop\target\reconstroi-desktop.jar
```

Documentação interativa da API (só neste computador): http://127.0.0.1:8081/swagger-ui.html

---

## 4. Gerar o ReConstroi.exe

### Requisitos extras

- **JDK 17** com `jlink` (o mesmo da seção 3). Defina `JAVA_HOME` se não estiver em `C:\Program Files\Java\jdk-17`.
- **.NET 10 SDK**: https://dotnet.microsoft.com/download/dotnet/10.0

### Comando (na raiz do projeto)

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot"   # ajuste para o seu JDK
powershell -ExecutionPolicy Bypass -File .\packaging\build-windows.ps1
```

O resultado fica em `dist\ReConstroi.exe` (o anterior vai para `dist-anterior\`). O hash SHA-256 fica em `dist\ReConstroi.exe.sha256.txt`.

**Importante:** feche o `ReConstroi.exe` antes de gerar de novo (o Windows não deixa substituir um programa aberto).

**Ao mudar o código**, aumente `AppVersion` em `packaging/ReConstroi.Launcher/Program.cs`. Sem isso, quem já usou uma versão anterior continua rodando os arquivos antigos já extraídos.

O `.exe` não tem assinatura digital: o Windows SmartScreen pode mostrar "editor desconhecido" → **Mais informações → Executar assim mesmo**.

Detalhes do empacotamento: [packaging/README.md](packaging/README.md).

---

## 5. Testes

Os testes do backend usam o banco configurado no `reconstroi.env` e **desfazem tudo ao final** (não deixam dados).

```powershell
cd backend
.\mvnw.cmd test
```

Devem passar **15 testes**, cobrindo: níveis de acesso, validações, fluxo completo da reserva à doação, reserva parcial, quantidade do lote, bloqueio de usuário e proteções de segurança (upload falso, ordenação pela senha, força bruta e sessão antiga após troca de senha).

---

## 6. Configuração (reconstroi.env)

Cada linha é `NOME=valor`. Variáveis de ambiente do Windows têm prioridade sobre o arquivo.

| Variável | Obrigatória | Padrão | Para que serve |
|---|---|---|---|
| `DATABASE_URL` | não | `jdbc:postgresql://localhost:5432/reconstroi` | Endereço do banco |
| `DATABASE_USERNAME` | não | `postgres` | Usuário do banco |
| `DATABASE_PASSWORD` | **sim** | — | Senha do banco |
| `JWT_SECRET` | **sim** | — | Chave (Base64, 32+ bytes) que assina os logins. O `.exe` gera uma aleatória |
| `JWT_EXPIRATION_HOURS` | não | `24` | Validade do login |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | não | vazio | Cria o primeiro administrador |
| `MAIL_ENABLED` | não | `false` | Liga o envio de e-mail (recuperação de senha) |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` | se `MAIL_ENABLED=true` | — | Servidor de e-mail |
| `EXPOSE_RESET_TOKEN` | não | `false` | Mostra o código de troca de senha no próprio app (uso local sem e-mail). Veja [Segurança](#7-segurança) |
| `SERVER_ADDRESS` | não | `127.0.0.1` | Em que endereço a API escuta. Deixe o padrão no uso local |
| `SERVER_PORT` | não | `8081` | Porta da API |
| `UPLOAD_DIRECTORY` | não | `uploads` | Pasta das fotos |

---

## 7. Segurança

O que o sistema já garante (verificado por testes automáticos e por testes de ataque à API):

- **A API só aceita conexões do próprio computador** (`127.0.0.1`). Outras pessoas na mesma rede Wi-Fi não alcançam a API.
- Senhas guardadas com **BCrypt**; nenhuma resposta da API traz a senha ou o hash.
- Login por **JWT** assinado; tokens forjados, alterados ou com `alg=none` são recusados.
- **Trocar a senha encerra todas as sessões** abertas antes.
- **Força bruta bloqueada:** 10 senhas erradas seguidas bloqueiam o login daquele e-mail por 15 minutos (o mesmo vale para pedidos de recuperação de senha).
- O tempo de resposta do login é igual para e-mail existente e inexistente (não dá para descobrir quem é cadastrado).
- Ninguém se cadastra como administrador; campos extras no cadastro/perfil (tipo, permissões) são ignorados.
- Cada perfil só acessa o que é seu: doadores só mexem nos próprios materiais, beneficiários nas próprias solicitações, rotas `/admin` só para administradores.
- Fotos: só JPG, PNG ou WebP **de verdade** (o conteúdo do arquivo é conferido), até 5 MB.
- Ordenação de listas só por campos permitidos (impede ordenar pelo hash da senha de outros).
- Busca protegida contra SQL injection; erros não mostram detalhes internos.
- O desktop mostra nomes e textos sempre como **texto puro** (um nome com `<html>` não é interpretado).

**Recuperação de senha sem e-mail.** Sem servidor de e-mail, o app desktop mostra o código de troca de senha na tela (`EXPOSE_RESET_TOKEN=true`, como o `.exe` configura). Isso só funciona com a API presa a `127.0.0.1`: se `SERVER_ADDRESS` for outro endereço, a API se recusa a mostrar o código. Para uso em servidor, configure o e-mail e deixe `EXPOSE_RESET_TOKEN=false`.

**Boas práticas para quem for usar:**

- Use uma senha forte no `postgres` e não a escreva em arquivos do projeto.
- Nunca suba o `reconstroi.env` para o Git.
- Se o Windows perguntar se o Java pode se comunicar na rede, pode **negar**: o ReConstrói não precisa disso.

---

## 8. Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `password authentication failed for user "postgres"` | Senha errada no `reconstroi.env` | Corrija `DATABASE_PASSWORD` e reinicie |
| `database "reconstroi" does not exist` | Banco não criado | Rode o comando `CREATE DATABASE reconstroi;` da seção 2 |
| `Connection refused` / "Não foi possível conectar à API" | PostgreSQL parado ou API não iniciou | Inicie o serviço `postgresql-x64-NN` em `services.msc`; veja `%LOCALAPPDATA%\ReConstroi\logs` |
| `Could not resolve placeholder 'JWT_SECRET'` | Falta a chave JWT | Crie o `reconstroi.env` como na seção 3, passo 3 |
| `Port 8081 was already in use` | O `.exe` e o NetBeans rodando juntos | Feche um dos dois |
| "Muitas tentativas seguidas" | 10 senhas erradas | Aguarde 15 minutos |
| SmartScreen bloqueia o `.exe` | Executável sem assinatura | **Mais informações → Executar assim mesmo** |
| Não sei a senha do `postgres` | — | Reinstale o PostgreSQL ou redefina a senha seguindo a documentação oficial do PostgreSQL |

---

## 9. Regras de negócio

**Perfis**

- `DOADOR` (pessoa física ou construtora): cadastra materiais (foto obrigatória), edita/remove os próprios anúncios e conduz as reservas recebidas.
- `BENEFICIARIO` (ONG ou família): pesquisa materiais, solicita reserva e pode cancelá-la enquanto aguarda aprovação.
- `ADMINISTRADOR`: gerencia usuários, anúncios, reservas, doações, denúncias e relatórios.

**Fluxo da reserva:** `SOLICITADA → APROVADA → RETIRADA → FINALIZADA` (sem pular etapas). Só uma reserva ativa por material.

- Ao **aprovar**, o material fica `RESERVADO`. A aprovação confere se o lote ainda tem a quantidade pedida.
- Ao **finalizar**, a doação é registrada com peso, resíduos e CO₂ evitados.
- **Reserva parcial:** se a doação levou só parte do lote (ex.: 10 de 100), a sobra (90) volta a ficar `DISPONIVEL`. Se levou tudo, o material fica `FINALIZADO`.
- O doador não pode reduzir o lote abaixo de uma solicitação pendente.
- Cancelar uma reserva aprovada devolve o material para `DISPONIVEL`.
- Cadastro do mesmo material no mesmo dia pede confirmação.
- Ações importantes geram histórico (usuário, data/hora, IP) e notificações.

---

## 10. Estrutura das pastas

```text
backend/src/main/java/com/reconstroi/backend/
  config/        segurança, JWT, CORS, criação do admin
  controller/    rotas REST e tratamento de erros
  service/       regras de negócio
  repository/    acesso ao banco (Spring Data JPA)
  model/         entidades e enums
  dto/           objetos de entrada/saída da API
backend/src/main/resources/
  application.properties        configuração (lê o reconstroi.env)
  db/migration/                 criação e evolução do banco (Flyway)
desktop/src/main/java/
  VIEW/          janelas e telas Swing
  DTO/           objetos de dados do desktop
  DAO/           chamadas à API e sessão do usuário
packaging/
  build-windows.ps1             gera o dist\ReConstroi.exe
  ReConstroi.Launcher/          programa .NET que abre a API e o desktop
```

---

## 11. Versão web e Docker (opcional)

Existe uma interface web em Next.js que **não está neste repositório** (fica numa pasta `front` com repositório próprio). O `compose.yaml` espera essa pasta em `./front`. Sem ela, use o desktop/`.exe`.

Com a pasta `front` presente:

1. Copie `.env.example` para `.env` e troque **todas** as senhas e a chave JWT.
2. Configure o domínio em `CORS_ALLOWED_ORIGINS` e `FRONTEND_URL`, e o e-mail (`MAIL_ENABLED=true` + SMTP).
3. Rode `docker compose up --build -d`.

No Docker a API escuta em todas as interfaces dentro da rede interna (`SERVER_ADDRESS=0.0.0.0`), mas só o front é publicado. Mantenha `EXPOSE_RESET_TOKEN=false` e use HTTPS no proxy.
