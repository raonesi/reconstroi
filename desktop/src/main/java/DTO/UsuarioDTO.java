package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String tipo;
    private String nivelAcesso;
    private Set<String> permissoes = new LinkedHashSet<>();
    private boolean ativo;
    private String cidade;
    private String estado;
    private String documento;
    private String criadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNivelAcesso() { return nivelAcesso; }
    public void setNivelAcesso(String nivelAcesso) { this.nivelAcesso = nivelAcesso; }
    public Set<String> getPermissoes() { return permissoes; }
    public void setPermissoes(Set<String> permissoes) { this.permissoes = permissoes == null ? new LinkedHashSet<>() : permissoes; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }
    public boolean possuiPermissao(String permissao) { return permissoes.contains(permissao); }
    public boolean isAdmin() { return "ADMIN".equals(tipo); }
    public boolean isDoador() { return tipo != null && (tipo.startsWith("DOADOR_") || isAdmin()); }
    public boolean isBeneficiario() { return tipo != null && (tipo.startsWith("BENEFICIARIO_") || isAdmin()); }
}
