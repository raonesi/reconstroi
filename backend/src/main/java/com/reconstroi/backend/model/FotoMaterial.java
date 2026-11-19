package com.reconstroi.backend.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "foto_material")
public class FotoMaterial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;
    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;
    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;
    @Column(nullable = false, length = 500)
    private String url;
    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm = Instant.now();
}
