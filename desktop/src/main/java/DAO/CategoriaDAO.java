package DAO;

import DTO.CategoriaDTO;
import java.util.List;

public class CategoriaDAO {
    private final ConexaoDAO conexao = new ConexaoDAO();
    public List<CategoriaDTO> listar() { return conexao.getList("/categorias", CategoriaDTO.class); }
}
