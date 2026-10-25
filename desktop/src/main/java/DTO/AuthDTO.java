package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthDTO {
    private UsuarioDTO usuario;
    private String token;
    private String expiraEm;
    public UsuarioDTO getUsuario() { return usuario; }
    public void setUsuario(UsuarioDTO usuario) { this.usuario = usuario; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getExpiraEm() { return expiraEm; }
    public void setExpiraEm(String expiraEm) { this.expiraEm = expiraEm; }
}
