package DAO;

import DTO.ReservaDTO;
import java.util.List;
import java.util.Map;

public class ReservaDAO {
    private final ConexaoDAO conexao = new ConexaoDAO();
    public List<ReservaDTO> listarMinhas() { return conexao.getList("/reservas", ReservaDTO.class); }
    public List<ReservaDTO> listarRecebidas() { return conexao.getList("/reservas/recebidas", ReservaDTO.class); }
    public ReservaDTO solicitar(long materialId, int quantidade, String mensagem) {
        return conexao.post("/reservas", Map.of("materialId", materialId, "quantidade", quantidade,
            "mensagem", mensagem == null ? "" : mensagem), ReservaDTO.class);
    }
    public ReservaDTO executarAcao(long id, String acao) {
        return conexao.patch("/reservas/" + id + "/" + acao, null, ReservaDTO.class);
    }
}
