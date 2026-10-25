package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Cada metodo executa uma das quatro operacoes do CRUD no PostgreSQL.
public class MaterialDAO {
    public List<Material> listar() throws SQLException {
        List<Material> materiais = new ArrayList<>();
        String sql = "SELECT id, nome, quantidade, descricao FROM material ORDER BY id";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                Material material = new Material(resultado.getInt("id"), resultado.getString("nome"),
                    resultado.getInt("quantidade"), resultado.getString("descricao"));
                materiais.add(material);
            }
        }
        return materiais;
    }

    public void cadastrar(Material material) throws SQLException {
        String sql = "INSERT INTO material (nome, quantidade, descricao) VALUES (?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            preencherComando(comando, material);
            comando.executeUpdate();
        }
    }

    public void editar(Material material) throws SQLException {
        String sql = "UPDATE material SET nome = ?, quantidade = ?, descricao = ? WHERE id = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            preencherComando(comando, material);
            comando.setInt(4, material.getId());
            if (comando.executeUpdate() == 0) {
                throw new IllegalArgumentException("Material nao encontrado.");
            }
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM material WHERE id = ?";
        try (Connection conexao = Conexao.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            if (comando.executeUpdate() == 0) {
                throw new IllegalArgumentException("Material nao encontrado.");
            }
        }
    }

    private void preencherComando(PreparedStatement comando, Material material) throws SQLException {
        // Os ? recebem valores separados do SQL, inclusive nomes com apostrofos.
        comando.setString(1, material.getNome());
        comando.setInt(2, material.getQuantidade());
        comando.setString(3, material.getDescricao());
    }
}
