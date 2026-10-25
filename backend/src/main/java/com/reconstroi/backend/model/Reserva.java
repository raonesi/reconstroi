package com.reconstroi.backend.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Reserva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material materialRelacionado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitanteRelacionado;
    @Column(name = "quantidade_solicitada")
    private Integer quantidadeSolicitada;
    @Enumerated(EnumType.STRING)
    @Column(name = "status_reserva", nullable = false, length = 30)
    private StatusReserva statusReserva = StatusReserva.SOLICITADA;
    @Column(length = 500)
    private String mensagem;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();
    @PreUpdate void atualizarData() { atualizadoEm = Instant.now(); }
}
