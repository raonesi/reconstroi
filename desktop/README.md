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

Por padrão o aplicativo usa `http://127.0.0.1:8081`. Para outra API, defina `RECONSTROI_API_URL`.

## Fotos de materiais

O cadastro aceita arrastar arquivos do Explorador para a área de fotos e selecionar arquivos pelo botão. JPG, JPEG, PNG e WebP são enviados com o tipo MIME explícito, independentemente das associações de arquivos do Windows. Limite: cinco fotos de até 5 MB; a mesma foto não é adicionada duas vezes. Remover da seleção não apaga o arquivo original.

O teste `VIEW.FotosMaterialCheck` em `src/test/java` verifica a transferência Swing, limites, remoção e os cabeçalhos multipart WebP. Após `mvn package`, execute com Java 17, classpath `target/test-classes;target/reconstroi-desktop.jar` e uma pasta temporária existente como argumento; o teste não usa o banco real.
