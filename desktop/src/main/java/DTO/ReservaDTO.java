package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservaDTO {
    private Long id;
    private MaterialDTO material;
    private UsuarioResumoDTO solicitante;
    private Integer quantidade;
    private String status;
    private String mensagem;
    private String criadoEm;
    private String atualizadoEm;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MaterialDTO getMaterial() { return material; }
    public void setMaterial(MaterialDTO material) { this.material = material; }
    public UsuarioResumoDTO getSolicitante() { return solicitante; }
    public void setSolicitante(UsuarioResumoDTO solicitante) { this.solicitante = solicitante; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }
    public String getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(String atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
