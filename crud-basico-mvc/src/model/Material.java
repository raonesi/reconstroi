package model;

public class Material {
    private int id;
    private String nome;
    private int quantidade;
    private String descricao;

    public Material(int id, String nome, int quantidade, String descricao) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.descricao = descricao;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getQuantidade() { return quantidade; }
    public String getDescricao() { return descricao; }
}
