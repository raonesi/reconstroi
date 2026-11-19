package DAO;

import DTO.DoacaoDTO;
import java.util.List;

public class DoacaoDAO {
    private final ConexaoDAO conexao = new ConexaoDAO();
    public List<DoacaoDTO> listar() { return conexao.getList("/doacoes", DoacaoDTO.class); }
}
