# ReConstrói — guia dos códigos

O projeto permite cadastrar materiais de construção para doação, consultar os
anúncios, editar informações e remover anúncios. Também possui cadastro de
usuários, login, reservas e registro de doações.

## Pastas

| Pasta | Conteúdo |
| --- | --- |
| desktop/src/main/java/VIEW | Telas do aplicativo Java Swing |
| desktop/src/main/java/DAO | Requisições feitas pelas telas ao servidor |
| desktop/src/main/java/DTO | Objetos usados para receber e enviar dados |
| backend/src/main/java/com/reconstroi/backend/controller | Recebe as requisições |
| backend/src/main/java/com/reconstroi/backend/service | Confere e executa as regras |
| backend/src/main/java/com/reconstroi/backend/model | Classes que representam as tabelas |
| backend/src/main/java/com/reconstroi/backend/repository | Consulta e gravação no PostgreSQL pelo Spring Data |
| backend/src/main/resources/db/migration | Scripts SQL que montam e atualizam o banco |
| backend/src/test | Testes das regras e da estrutura do banco |
| front | Interface web alternativa em Next.js |
| packaging | Código C# e scripts que geram o executável Windows |

## Por onde começar a leitura

1. `FrmLoginVIEW.java`: cria a tela e envia e-mail e senha para `UsuarioDAO`.
2. `FrmMaterialVIEW.java`: lê os campos e escolhe entre cadastrar e editar.
3. `MaterialDAO.java`: envia os dados para as rotas do backend.
4. `MaterialController.java`: recebe a requisição e chama `MaterialService`.
5. `MaterialService.java`: verifica permissões, fotos e regras do material.
6. `MaterialRepository.java` e `Material.java`: representam o acesso ao banco e o registro salvo.

Exemplo ao clicar em SALVAR: tela → DAO → Controller → Service → Repository → banco.
A resposta faz o caminho de volta e a tela mostra o resultado.

## Onde está o CRUD

| Operação | Desktop (MaterialDAO) | Rota no backend |
| --- | --- | --- |
| Criar | cadastrar | POST /materiais |
| Consultar | pesquisar / consultar | GET /materiais e GET /materiais/{id} |
| Atualizar | alterar | PUT /materiais/{id} |
| Excluir | excluir | DELETE /materiais/{id} |

A exclusão de material é lógica: marca o anúncio como excluído para preservar
o histórico. As fotos aceitam JPG, PNG e WebP. O banco segue o diagrama fornecido,
exceto a recuperação de senha, removida a pedido do responsável pelo projeto.

## Termos usados

- DTO: objeto que transporta dados entre as partes do sistema.
- DAO: neste desktop, faz chamadas HTTP ao backend; não abre conexão direta com o banco.
- Controller: ponto de entrada de uma requisição HTTP.
- Service: concentra as regras, como permitir que apenas o dono edite o material.
- Repository: usa Spring Data para consultar e salvar entidades.
- JWT: token recebido no login e enviado nas próximas requisições.
- `@Transactional`: agrupa alterações no banco; em caso de erro, elas são desfeitas.
- `Estilo.executar`: executa a comunicação sem travar a janela Swing.

As partes principais de login e cadastro de material foram reorganizadas para
leitura passo a passo. A segurança, as validações e as bibliotecas do projeto
continuam presentes. A versão web é independente da interface Swing do executável.

## Compilar

Instale Java 17, configure PostgreSQL e consulte o README principal para as
variáveis de ambiente. Não há cópia do banco pessoal neste pacote.

No PowerShell, a partir da pasta do projeto:

```powershell
cd backend
.\mvnw.cmd package -DskipTests
.\mvnw.cmd -f ..\desktop\pom.xml package
```

Para os testes do backend, use um banco separado configurado por `DATABASE_URL`.
Para a interface web, instale Node.js e execute `npm ci` dentro de `front`.
As instruções do executável estão em `packaging/README.md`; os runtimes e o
executável pronto não acompanham o ZIP de códigos.
