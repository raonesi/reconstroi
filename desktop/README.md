# ReConstrói Desktop

Cliente Java Swing organizado conforme o modelo acadêmico `VIEW → DTO → DAO`.

- `VIEW`: janelas, formulários, tabelas e ações do usuário.
- `DTO`: objetos usados para transportar os dados.
- `DAO`: comunicação com a API REST do backend.

Os DAOs não acessam o PostgreSQL diretamente. Eles utilizam o backend Spring Boot para preservar JWT, permissões, validações, notificações e todas as regras de negócio.

## Executar durante o desenvolvimento

Com o PostgreSQL e o backend iniciados:

```powershell
cd backend
.\mvnw.cmd -f ..\desktop\pom.xml package
java -jar ..\desktop\target\reconstroi-desktop.jar
```

No NetBeans: abra esta pasta (`File → Open Project`) e use **Run** — a classe inicial já está configurada (`VIEW.FrmLoginVIEW`).

Por padrão o aplicativo usa `http://127.0.0.1:8081`. Para outra API, defina `RECONSTROI_API_URL`.

Passo a passo completo (requisitos, banco, configuração e problemas comuns): [README principal](../README.md).
