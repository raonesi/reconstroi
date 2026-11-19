package com.reconstroi.backend.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Material {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(nullable = false, length = 2000)
    private String descricao;
    @Column(nullable = false)
    private Integer quantidade;
    @Column(nullable = false, length = 30)
    private String unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conservacao", nullable = false, length = 30)
    private EstadoConservacao estadoConservacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_material", nullable = false, length = 30)
    private StatusMaterial status = StatusMaterial.DISPONIVEL;

    @Column(name = "endereco_retirada", nullable = false, length = 240)
    private String enderecoRetirada;
    @Column(nullable = false, length = 120)
    private String cidade;
    @Column(name = "estado_uf", nullable = false, length = 2)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietario_id")
    private Usuario proprietario;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<FotoMaterial> fotos = new ArrayList<>();

    @Column(name = "data_cadastro", nullable = false)
    private Instant dataCadastro = Instant.now();
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    @Column(nullable = false)
    private boolean excluido;

    @Column(name = "excluido_em")
    private Instant excluidoEm;

    @Column(name = "motivo_remocao", length = 500)
    private String motivoRemocao;

    @Version
    private Long versao;

    @PreUpdate void atualizarData() { atualizadoEm = Instant.now(); }
}
