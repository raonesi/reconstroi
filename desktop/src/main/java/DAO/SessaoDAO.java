package DAO;

import DTO.UsuarioDTO;

public final class SessaoDAO {
    private static String token;
    private static UsuarioDTO usuario;
    private SessaoDAO() {}
    public static String getToken() { return token; }
    public static UsuarioDTO getUsuario() { return usuario; }
    public static void iniciar(String novoToken, UsuarioDTO novoUsuario) {
        token = novoToken;
        usuario = novoUsuario;
    }
    public static void atualizarUsuario(UsuarioDTO novoUsuario) { usuario = novoUsuario; }
    public static void encerrar() { token = null; usuario = null; }
}
