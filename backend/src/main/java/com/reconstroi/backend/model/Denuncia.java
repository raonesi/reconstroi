package com.reconstroi.backend.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Denuncia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "denunciante_id", nullable = false)
    private Usuario denunciante;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;
    @Column(nullable = false, length = 500)
    private String motivo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusDenuncia status = StatusDenuncia.ABERTA;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();
    @Column(name = "resolvido_em")
    private Instant resolvidoEm;
}
