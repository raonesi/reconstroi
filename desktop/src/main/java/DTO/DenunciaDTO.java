package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DenunciaDTO {
    private Long id;
    private Long materialId;
    private String materialNome;
    private UsuarioResumoDTO denunciante;
    private String motivo;
    private String status;
    private String criadoEm;
    private String resolvidoEm;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialNome() { return materialNome; }
    public void setMaterialNome(String materialNome) { this.materialNome = materialNome; }
    public UsuarioResumoDTO getDenunciante() { return denunciante; }
    public void setDenunciante(UsuarioResumoDTO denunciante) { this.denunciante = denunciante; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }
    public String getResolvidoEm() { return resolvidoEm; }
    public void setResolvidoEm(String resolvidoEm) { this.resolvidoEm = resolvidoEm; }
}
