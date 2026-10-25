package DAO;

import DTO.NotificacaoDTO;
import java.util.List;

public class NotificacaoDAO {
    private final ConexaoDAO conexao = new ConexaoDAO();
    public List<NotificacaoDTO> listar() { return conexao.getList("/notificacoes", NotificacaoDTO.class); }
    public NotificacaoDTO marcarLida(long id) { return conexao.patch("/notificacoes/" + id + "/lida", null, NotificacaoDTO.class); }
}
