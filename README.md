# ReConstrói

Plataforma completa de reaproveitamento de materiais de construção. O projeto conecta doadores, construtoras, ONGs e famílias e acompanha o ciclo do anúncio até a doação finalizada.

## Stack

- Frontend: Next.js 16, React 19, TypeScript, Tailwind CSS 4, Lucide e Chart.js.
- Desktop: Java Swing no padrão acadêmico `VIEW → DTO → DAO`, integrado à mesma API REST.
- Backend: Spring Boot 3.4, Java 17, MVC, Spring Security e JWT.
- Dados: PostgreSQL com migrations Flyway.
- Operação: Docker Compose, imagens multi-stage, health checks e volumes persistentes.

## Execução local

Pré-requisitos: Java 17, Node.js 22 e PostgreSQL 17 ou 18 disponível em `localhost:5432`, com banco `reconstroi` e usuário `postgres`. A senha é definida por você: não use exemplos prontos, escolha uma senha forte e informe-a na variável `DATABASE_PASSWORD`. Os valores de conexão podem ser substituídos pelas variáveis `DATABASE_URL` e `DATABASE_USERNAME`.

Mantenha o PostgreSQL acessível somente pelo próprio computador: em `postgresql.conf`, deixe `listen_addresses = 'localhost'`, e em `pg_hba.conf` libere apenas `127.0.0.1` e `::1`. Assim o banco não fica disponível na rede local.

O backend **não traz** segredo nem senha de banco padrão: sem `JWT_SECRET` (Base64, mínimo de 32 bytes) e `DATABASE_PASSWORD` a aplicação se recusa a iniciar, inclusive os testes. Gere uma chave de desenvolvimento e defina as variáveis no terminal antes de rodar a API ou os testes:

```powershell
$bytes = New-Object byte[] 48; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:DATABASE_PASSWORD = "<a-senha-do-seu-postgres>"
```

Cada chave é específica do ambiente onde foi gerada: nunca reutilize a de um ambiente em outro.

Terminal 1 — API:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Terminal 2 — aplicação web:

```powershell
cd front
npm.cmd install
npm.cmd run dev
```

Acesse [http://localhost:3000](http://localhost:3000). O frontend encaminha `/api/*` para a API na porta `8081`; não é necessário abrir a API diretamente no navegador.

Documentação da API: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html).

### PostgreSQL portátil no Windows

Para a instalação portátil configurada neste computador, os binários ficam em
`%USERPROFILE%\Downloads\pgsql\bin` e os dados persistentes em
`%USERPROFILE%\.reconstroi\postgres-data`, fora da pasta do projeto.
Depois de reiniciar o Windows, abra `Iniciar-Banco.cmd` na raiz do projeto antes
de iniciar o backend. Esse comando inicia somente o cluster já existente, em
`localhost:5432`; não recria nem apaga os dados. Mantenha a pasta `pgsql` em Downloads.
O servidor não foi instalado como serviço de inicialização automática.

Conexão local: banco `reconstroi`, usuário `postgres` e a senha que você definiu
em `DATABASE_PASSWORD`. No pgAdmin, registre um servidor usando
host `localhost`, porta `5432` e banco de manutenção `postgres`; após conectar,
`reconstroi` aparece em Databases. O pgAdmin pode ser fechado sem parar o servidor.

Em bancos novos, o Flyway executa `V0__initial_schema.sql` antes de V1 e V2 para
criar as tabelas-base. A configuração do Hibernate continua em `validate`.

## Perfis e fluxo

- Nível `DOADOR` (pessoa física ou construtora): cadastra materiais com foto obrigatória, gerencia os próprios anúncios e conduz reservas recebidas.
- Nível `BENEFICIARIO` (ONG ou família): pesquisa materiais, cria uma solicitação e pode cancelá-la somente antes da aprovação.
- Nível `ADMINISTRADOR`: gerencia usuários, anúncios, reservas, doações, denúncias, históricos e relatórios globais.

Além do nível, o backend emite autoridades granulares (`MATERIAL_CADASTRAR`, `RESERVA_SOLICITAR`, `RESERVA_GERENCIAR_RECEBIDA`, `USUARIO_GERENCIAR` etc.). Elas são recalculadas a partir do usuário no banco em toda requisição JWT; bloquear uma conta invalida imediatamente inclusive tokens já emitidos. Cada token também só vale enquanto existir sua sessão registrada no login (tabela `sessao`); `POST /usuarios/logout` apaga a sessão e sessões expiradas são removidas de hora em hora.

Fluxo transacional: `DISPONÍVEL → SOLICITADA → APROVADA/RESERVADO → RETIRADA → FINALIZADA`. Não é permitido pular etapas e há uma garantia no serviço e no PostgreSQL contra duas reservas ativas para o mesmo material. A doação permanente, o peso estimado, os resíduos e o impacto de CO₂ são registrados apenas na finalização. A reserva pode ser parcial: ao finalizar, a quantidade entregue é descontada do material; havendo saldo, o anúncio volta a `DISPONÍVEL` com a quantidade restante, e só vira `FINALIZADO` quando o estoque zera. Um anúncio cuja quantidade foi reduzida abaixo do solicitado não pode ter a reserva aprovada.

Toda publicação exige ao menos uma imagem e é criada atomicamente por `multipart/form-data`. Duplicidades do mesmo doador no mesmo dia exigem confirmação explícita. Criações, edições, decisões de reserva, retirada, finalização, remoções e bloqueios geram histórico com usuário, data/hora e IP; aprovações e recusas também geram notificação persistente para o beneficiário.

Para criar o primeiro administrador local, inicie o backend definindo:

```powershell
$env:ADMIN_EMAIL="admin@reconstroi.local"
$env:ADMIN_PASSWORD="<defina-uma-senha-forte>"
.\mvnw.cmd spring-boot:run
```

## Testes e build

```powershell
cd backend
.\mvnw.cmd test

cd ..\front
npm.cmd run lint
npm.cmd run build
npm.cmd run start
```

## Docker / produção

1. Copie `.env.example` para `.env`.
2. Gere uma chave JWT Base64 com pelo menos 32 bytes e troque todas as senhas.
3. Defina o domínio HTTPS em `CORS_ALLOWED_ORIGINS` e `FRONTEND_URL`.
4. Se desejar notificações de reservas por e-mail, configure SMTP e `MAIL_ENABLED=true`.
5. Execute:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f --tail=100
```

Somente a porta do frontend é publicada. PostgreSQL e API ficam na rede interna. Os volumes `postgres_data` e `uploads_data` precisam entrar na política de backup. Em produção, use HTTPS no proxy reverso e armazene os segredos em um cofre da plataforma.

## Executável Windows

Um pacote portátil de 64 bits pode ser aberto com dois cliques:

```text
dist\ReConstroi.exe
```

O executável inclui PostgreSQL, Java, backend e a interface Swing. Ele prepara o banco local, inicia os serviços em segundo plano e abre a janela nativa de login, sem navegador nem terminal. Não é necessário instalar dependências separadamente no computador de apresentação. Configuração, banco, fotos e logs são mantidos em `%USERPROFILE%\.reconstroi` e preservados entre execuções.

Para reconstruí-lo:

```powershell
powershell -ExecutionPolicy Bypass -File .\packaging\build-windows.ps1
```

Consulte [packaging/README.md](packaging/README.md) para detalhes de configuração, distribuição e assinatura digital.

## Estrutura

- `backend/src/main/java`: controllers, services, repositories, DTOs, entidades, segurança e exceções.
- `backend/src/main/resources/db/migration`: schema versionado e dados iniciais de categorias.
- `front/app`: páginas públicas e rotas autenticadas do App Router.
- `front/components`: shell responsivo, autenticação, formulários, cards, gráficos e notificações.
- `desktop/src/main/java/VIEW`: janelas, formulários, painéis e ações Swing.
- `desktop/src/main/java/DTO`: objetos de transporte da aplicação desktop.
- `desktop/src/main/java/DAO`: comunicação segura com a API REST e gerenciamento da sessão JWT.


