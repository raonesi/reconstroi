package model;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexao {
    public static Connection abrir() throws SQLException {
        Properties config = new Properties();
        Path arquivo = Path.of("banco.properties");
        if (Files.exists(arquivo)) {
            try (Reader leitor = Files.newBufferedReader(arquivo)) {
                config.load(leitor);
            } catch (IOException e) {
                throw new SQLException("Nao foi possivel ler banco.properties.", e);
            }
        }
        String url = System.getenv("CRUD_DATABASE_URL");
        if (url == null) url = config.getProperty("url", "jdbc:postgresql://localhost:5432/crud_mvc");
        String usuario = config.getProperty("usuario", "postgres");
        String senha = System.getenv("CRUD_DATABASE_PASSWORD");
        if (senha == null) senha = config.getProperty("senha", "");
        return DriverManager.getConnection(url, usuario, senha);
    }
}
