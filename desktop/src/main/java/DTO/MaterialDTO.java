package DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MaterialDTO {
    private Long id;
    private String nome;
    private String descricao;
    private Integer quantidade;
    private String unidade;
    private String estadoConservacao;
    private String status;
    private CategoriaDTO categoria;
    private String enderecoRetirada;
    private String cidade;
    private String estado;
    private List<FotoDTO> fotos = new ArrayList<>();
    private UsuarioResumoDTO proprietario;
    private String dataCadastro;
    private String atualizadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public String getEstadoConservacao() { return estadoConservacao; }
    public void setEstadoConservacao(String estadoConservacao) { this.estadoConservacao = estadoConservacao; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public CategoriaDTO getCategoria() { return categoria; }
    public void setCategoria(CategoriaDTO categoria) { this.categoria = categoria; }
    public String getEnderecoRetirada() { return enderecoRetirada; }
    public void setEnderecoRetirada(String enderecoRetirada) { this.enderecoRetirada = enderecoRetirada; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<FotoDTO> getFotos() { return fotos; }
    public void setFotos(List<FotoDTO> fotos) { this.fotos = fotos == null ? new ArrayList<>() : fotos; }
    public UsuarioResumoDTO getProprietario() { return proprietario; }
    public void setProprietario(UsuarioResumoDTO proprietario) { this.proprietario = proprietario; }
    public String getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(String dataCadastro) { this.dataCadastro = dataCadastro; }
    public String getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(String atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
