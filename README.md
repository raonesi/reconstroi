# ReConstrói

Plataforma completa de reaproveitamento de materiais de construção. O projeto conecta doadores, construtoras, ONGs e famílias e acompanha o ciclo do anúncio até a doação finalizada.

## Stack

- Frontend: Next.js 16, React 19, TypeScript, Tailwind CSS 4, Lucide e Chart.js.
- Desktop: Java Swing no padrão acadêmico `VIEW → DTO → DAO`, integrado à mesma API REST.
- Backend: Spring Boot 3.4, Java 17, MVC, Spring Security e JWT.
- Dados: PostgreSQL com migrations Flyway.
- Operação: Docker Compose, imagens multi-stage, health checks e volumes persistentes.

## Execução local

Pré-requisitos: Java 17, Node.js 22 e PostgreSQL disponível em `localhost:5432`, com banco `reconstroi`, usuário `postgres` e senha definida em `DATABASE_PASSWORD`. Esses valores podem ser substituídos pelas variáveis `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD`.

Terminal 1 — API:

```powershell
cd C:\Users\raone\Desktop\reconstroi\backend
.\mvnw.cmd spring-boot:run
```

Terminal 2 — aplicação web:

```powershell
cd C:\Users\raone\Desktop\reconstroi\front
npm.cmd install
npm.cmd run dev
```

Acesse [http://localhost:3000](http://localhost:3000). O frontend encaminha `/api/*` para a API na porta `8081`; não é necessário abrir a API diretamente no navegador.

Documentação da API: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html).

## Perfis e fluxo

- Nível `DOADOR` (pessoa física ou construtora): cadastra materiais com foto obrigatória, gerencia os próprios anúncios e conduz reservas recebidas.
- Nível `BENEFICIARIO` (ONG ou família): pesquisa materiais, cria uma solicitação e pode cancelá-la somente antes da aprovação.
- Nível `ADMINISTRADOR`: gerencia usuários, anúncios, reservas, doações, denúncias, históricos e relatórios globais.

Além do nível, o backend emite autoridades granulares (`MATERIAL_CADASTRAR`, `RESERVA_SOLICITAR`, `RESERVA_GERENCIAR_RECEBIDA`, `USUARIO_GERENCIAR` etc.). Elas são recalculadas a partir do usuário no banco em toda requisição JWT; bloquear uma conta invalida imediatamente inclusive tokens já emitidos.

Fluxo transacional: `DISPONÍVEL → SOLICITADA → APROVADA/RESERVADO → RETIRADA → FINALIZADA`. Não é permitido pular etapas e há uma garantia no serviço e no PostgreSQL contra duas reservas ativas para o mesmo material. A doação permanente, o peso estimado, os resíduos e o impacto de CO₂ são registrados apenas na finalização.

Toda publicação exige ao menos uma imagem e é criada atomicamente por `multipart/form-data`. Duplicidades do mesmo doador no mesmo dia exigem confirmação explícita. Criações, edições, decisões de reserva, retirada, finalização, remoções e bloqueios geram histórico com usuário, data/hora e IP; aprovações e recusas também geram notificação persistente para o beneficiário.

Para criar o primeiro administrador local, inicie o backend definindo:

```powershell
$env:ADMIN_EMAIL="admin@reconstroi.local"
$env:ADMIN_PASSWORD="UmaSenhaForte123!"
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
4. Configure SMTP e `MAIL_ENABLED=true` para recuperação de senha por e-mail.
5. Execute:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f --tail=100
```

Somente a porta do frontend é publicada. PostgreSQL e API ficam na rede interna. Os volumes `postgres_data` e `uploads_data` precisam entrar na política de backup. Em produção, mantenha `EXPOSE_RESET_TOKEN=false`, use HTTPS no proxy reverso e armazene os segredos em um cofre da plataforma.

## Executável Windows

Um pacote portátil de 64 bits pode ser aberto com dois cliques:

```text
dist\ReConstroi.exe
```

O executável contém o runtime Java, o backend e a interface Swing. Ele inicia a API sem janela de terminal, abre o aplicativo desktop e permanece na bandeja do Windows. O PostgreSQL continua obrigatório, mas o pgAdmin não precisa ficar aberto. Configuração, dados e logs são mantidos em `%LOCALAPPDATA%\ReConstroi`.

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
