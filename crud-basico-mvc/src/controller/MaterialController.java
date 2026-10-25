package controller;

import java.sql.SQLException;
import java.util.List;
import model.Material;
import model.MaterialDAO;

// Recebe os dados da tela, verifica as regras e chama o DAO.
public class MaterialController {
    private final MaterialDAO dao;

    public MaterialController(MaterialDAO dao) {
        this.dao = dao;
    }

    public List<Material> listar() throws SQLException {
        return dao.listar();
    }

    public void cadastrar(String nome, String quantidade, String descricao) throws SQLException {
        int valor = validar(nome, quantidade);
        dao.cadastrar(new Material(0, nome.trim(), valor, descricao.trim()));
    }

    public void editar(int id, String nome, String quantidade, String descricao) throws SQLException {
        int valor = validar(nome, quantidade);
        dao.editar(new Material(id, nome.trim(), valor, descricao.trim()));
    }

    public void excluir(int id) throws SQLException {
        dao.excluir(id);
    }

    private int validar(String nome, String quantidade) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do material.");
        }
        int valor;
        try {
            valor = Integer.parseInt(quantidade.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("A quantidade deve ser um numero inteiro.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        return valor;
    }
}
