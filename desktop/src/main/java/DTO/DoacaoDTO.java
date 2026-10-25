package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DoacaoDTO {
    private Long id;
    private MaterialDTO material;
    private UsuarioResumoDTO doador;
    private UsuarioResumoDTO beneficiario;
    private Integer quantidade;
    private String concluidaEm;
    private BigDecimal pesoEstimadoKg;
    private BigDecimal residuosEvitadosKg;
    private BigDecimal co2EvitadoKg;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MaterialDTO getMaterial() { return material; }
    public void setMaterial(MaterialDTO material) { this.material = material; }
    public UsuarioResumoDTO getDoador() { return doador; }
    public void setDoador(UsuarioResumoDTO doador) { this.doador = doador; }
    public UsuarioResumoDTO getBeneficiario() { return beneficiario; }
    public void setBeneficiario(UsuarioResumoDTO beneficiario) { this.beneficiario = beneficiario; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public String getConcluidaEm() { return concluidaEm; }
    public void setConcluidaEm(String concluidaEm) { this.concluidaEm = concluidaEm; }
    public BigDecimal getPesoEstimadoKg() { return pesoEstimadoKg; }
    public void setPesoEstimadoKg(BigDecimal pesoEstimadoKg) { this.pesoEstimadoKg = pesoEstimadoKg; }
    public BigDecimal getResiduosEvitadosKg() { return residuosEvitadosKg; }
    public void setResiduosEvitadosKg(BigDecimal residuosEvitadosKg) { this.residuosEvitadosKg = residuosEvitadosKg; }
    public BigDecimal getCo2EvitadoKg() { return co2EvitadoKg; }
    public void setCo2EvitadoKg(BigDecimal co2EvitadoKg) { this.co2EvitadoKg = co2EvitadoKg; }
}
