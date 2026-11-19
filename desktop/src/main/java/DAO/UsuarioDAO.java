package DAO;

import DTO.AuthDTO;
import DTO.UsuarioDTO;
import java.util.LinkedHashMap;
import java.util.Map;

public class UsuarioDAO {
    private final ConexaoDAO conexao = new ConexaoDAO();

    public UsuarioDTO autenticar(String email, String senha) {
        AuthDTO auth = conexao.post("/usuarios/login", Map.of("email", email, "senha", senha), AuthDTO.class);
        SessaoDAO.iniciar(auth.getToken(), auth.getUsuario());
        return auth.getUsuario();
    }

    public UsuarioDTO cadastrar(String nome, String email, String telefone, String senha, String tipo,
                                String cidade, String estado, String documento) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("nome", nome); dados.put("email", email); dados.put("telefone", telefone);
        dados.put("senha", senha); dados.put("tipo", tipo); dados.put("cidade", nulo(cidade));
        dados.put("estado", nulo(estado)); dados.put("documento", nulo(documento));
        AuthDTO auth = conexao.post("/usuarios", dados, AuthDTO.class);
        SessaoDAO.iniciar(auth.getToken(), auth.getUsuario());
        return auth.getUsuario();
    }

    public UsuarioDTO consultarPerfil() {
        UsuarioDTO usuario = conexao.get("/usuarios/me", UsuarioDTO.class);
        SessaoDAO.atualizarUsuario(usuario);
        return usuario;
    }

    public UsuarioDTO atualizarPerfil(String nome, String telefone, String cidade, String estado, String documento) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("nome", nome); dados.put("telefone", telefone); dados.put("cidade", nulo(cidade));
        dados.put("estado", nulo(estado)); dados.put("documento", nulo(documento));
        UsuarioDTO usuario = conexao.put("/usuarios/me", dados, UsuarioDTO.class);
        SessaoDAO.atualizarUsuario(usuario);
        return usuario;
    }

    public String recuperarSenha(String email) {
        var resposta = conexao.post("/usuarios/recuperar-senha", Map.of("email", email), com.fasterxml.jackson.databind.JsonNode.class);
        String mensagem = resposta.path("mensagem").asText();
        String token = resposta.path("tokenDesenvolvimento").asText();
        return token.isBlank() ? mensagem : mensagem + "\n\nToken para teste: " + token;
    }

    public void redefinirSenha(String token, String novaSenha) {
        conexao.post("/usuarios/redefinir-senha", Map.of("token", token, "novaSenha", novaSenha), Void.class);
    }

    public void logout() { SessaoDAO.encerrar(); }
    private static Object nulo(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
