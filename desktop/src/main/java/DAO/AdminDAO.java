package DAO;

import DTO.*;
import java.util.List;
import java.util.Map;

public class AdminDAO {
    private final ConexaoDAO conexao = new ConexaoDAO();
    public List<UsuarioDTO> listarUsuarios() { return conexao.getPageContent("/admin/usuarios?size=200", UsuarioDTO.class); }
    public UsuarioDTO alterarAtivo(long id, boolean ativo) {
        return conexao.patch("/admin/usuarios/" + id + "/ativo", Map.of("ativo", ativo), UsuarioDTO.class);
    }
    public List<MaterialDTO> listarMateriais() { return conexao.getPageContent("/admin/materiais?size=200", MaterialDTO.class); }
    public void removerMaterial(long id, String motivo) { conexao.delete("/admin/materiais/" + id, Map.of("motivo", motivo)); }
    public List<ReservaDTO> listarReservas() { return conexao.getList("/admin/reservas", ReservaDTO.class); }
    public List<DoacaoDTO> listarDoacoes() { return conexao.getList("/admin/doacoes", DoacaoDTO.class); }
    public List<DenunciaDTO> listarDenuncias() { return conexao.getList("/admin/denuncias", DenunciaDTO.class); }
    public DenunciaDTO atualizarDenuncia(long id, String status) {
        return conexao.patch("/admin/denuncias/" + id, Map.of("status", status), DenunciaDTO.class);
    }
}
