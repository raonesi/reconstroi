package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificacaoDTO {
    private Long id;
    private String titulo;
    private String mensagem;
    private boolean lida;
    private String criadaEm;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
    public String getCriadaEm() { return criadaEm; }
    public void setCriadaEm(String criadaEm) { this.criadaEm = criadaEm; }
}
