package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardDTO {
    private long materiaisCadastrados;
    private long materiaisDisponiveis;
    private long materiaisReutilizados;
    private long reservas;
    private long doacoesConcluidas;
    private long usuarios;
    private long ongs;
    private long construtoras;
    private long familias;
    private long quantidadeReaproveitada;
    private BigDecimal residuosEvitadosKg = BigDecimal.ZERO;
    private BigDecimal pesoEstimadoKg = BigDecimal.ZERO;
    private BigDecimal co2EvitadoKg = BigDecimal.ZERO;
    private Map<String, Long> categoriasMaisUtilizadas = new LinkedHashMap<>();
    private Map<String, Long> doacoesPorMes = new LinkedHashMap<>();
    private List<RankingDTO> materiaisMaisDoados = List.of();
    private List<RankingDTO> materiaisMaisProcurados = List.of();

    public long getMateriaisCadastrados() { return materiaisCadastrados; }
    public void setMateriaisCadastrados(long v) { materiaisCadastrados = v; }
    public long getMateriaisDisponiveis() { return materiaisDisponiveis; }
    public void setMateriaisDisponiveis(long v) { materiaisDisponiveis = v; }
    public long getMateriaisReutilizados() { return materiaisReutilizados; }
    public void setMateriaisReutilizados(long v) { materiaisReutilizados = v; }
    public long getReservas() { return reservas; }
    public void setReservas(long v) { reservas = v; }
    public long getDoacoesConcluidas() { return doacoesConcluidas; }
    public void setDoacoesConcluidas(long v) { doacoesConcluidas = v; }
    public long getUsuarios() { return usuarios; }
    public void setUsuarios(long v) { usuarios = v; }
    public long getOngs() { return ongs; }
    public void setOngs(long v) { ongs = v; }
    public long getConstrutoras() { return construtoras; }
    public void setConstrutoras(long v) { construtoras = v; }
    public long getFamilias() { return familias; }
    public void setFamilias(long v) { familias = v; }
    public long getQuantidadeReaproveitada() { return quantidadeReaproveitada; }
    public void setQuantidadeReaproveitada(long v) { quantidadeReaproveitada = v; }
    public BigDecimal getResiduosEvitadosKg() { return residuosEvitadosKg; }
    public void setResiduosEvitadosKg(BigDecimal v) { residuosEvitadosKg = v; }
    public BigDecimal getPesoEstimadoKg() { return pesoEstimadoKg; }
    public void setPesoEstimadoKg(BigDecimal v) { pesoEstimadoKg = v; }
    public BigDecimal getCo2EvitadoKg() { return co2EvitadoKg; }
    public void setCo2EvitadoKg(BigDecimal v) { co2EvitadoKg = v; }
    public Map<String, Long> getCategoriasMaisUtilizadas() { return categoriasMaisUtilizadas; }
    public void setCategoriasMaisUtilizadas(Map<String, Long> v) { categoriasMaisUtilizadas = v; }
    public Map<String, Long> getDoacoesPorMes() { return doacoesPorMes; }
    public void setDoacoesPorMes(Map<String, Long> v) { doacoesPorMes = v; }
    public List<RankingDTO> getMateriaisMaisDoados() { return materiaisMaisDoados; }
    public void setMateriaisMaisDoados(List<RankingDTO> v) { materiaisMaisDoados = v; }
    public List<RankingDTO> getMateriaisMaisProcurados() { return materiaisMaisProcurados; }
    public void setMateriaisMaisProcurados(List<RankingDTO> v) { materiaisMaisProcurados = v; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RankingDTO {
        private String nome;
        private long quantidade;
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public long getQuantidade() { return quantidade; }
        public void setQuantidade(long quantidade) { this.quantidade = quantidade; }
    }
}
