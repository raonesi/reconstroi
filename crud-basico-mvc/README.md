# CRUD básico MVC com PostgreSQL

Projeto separado do ReConstrói completo. Interface Java Swing e conexão JDBC,
sem Spring ou servidor web. O banco é crud_mvc e a tabela é material.

## Abrir

Com o PostgreSQL ligado, abra Iniciar.cmd. Para mostrar o banco no SQL Shell,
abra Abrir-Banco.cmd. Ele exibe a conexão, a estrutura da tabela e os registros.

## As seis classes

- Main: monta os objetos e abre a janela.
- model/Material: representa ID, nome, quantidade e descrição.
- model/Conexao: abre a conexão JDBC.
- model/MaterialDAO: executa INSERT, SELECT, UPDATE e DELETE.
- controller/MaterialController: valida os campos e chama o DAO.
- view/MaterialView: formulário, botões e tabela.

Caminho do cadastro: View → Controller → DAO → PostgreSQL. O banco gera o ID.
PreparedStatement separa valores do SQL. try-with-resources fecha conexões e comandos.

## Preparar em outro computador

1. Instale Java 17 e PostgreSQL e inicie o serviço do PostgreSQL.
2. Execute CREATE DATABASE crud_mvc; em uma conexão administrativa.
3. Conecte ao crud_mvc e execute sql/criar-tabela.sql.
4. Copie banco.properties.example para banco.properties e informe sua senha.
5. Mantenha lib/postgresql-42.7.5.jar junto do JAR e abra Iniciar.cmd.

Para compilar: configure JAVA_HOME e execute powershell -File Compilar.ps1.
O driver JDBC está em lib. A configuração local com senha não acompanha o ZIP.
No pgAdmin, conecte a localhost:5432, selecione crud_mvc e abra o Query Tool.
Execute sql/consultar.sql para mostrar os dados.

## Apresentação

1. Cadastre Tijolo, quantidade 20 e descrição Material para doação.
2. Execute SELECT * FROM material; no SQL Shell e mostre o registro.
3. Selecione na tela, altere para 15 e clique em Editar selecionado.
4. Execute o SELECT novamente.
5. Feche e abra o aplicativo: o banco mantém os dados.
6. Exclua na tela e confirme a exclusão com outro SELECT.
7. Tente cadastrar nome vazio ou quantidade negativa para mostrar a validação.

O banco reconstroi não é utilizado. Os arquivos materiais.properties antigos
não são mais utilizados e não foram apagados. Os testes só aceitam o banco
crud_mvc_teste via CRUD_DATABASE_URL. A senha pode ser fornecida por
CRUD_DATABASE_PASSWORD. Use um banco de testes vazio com a mesma tabela.
