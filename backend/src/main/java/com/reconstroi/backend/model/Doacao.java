package com.reconstroi.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Doacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reservaRelacionada;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material materialRelacionado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doador_id")
    private Usuario doadorRelacionado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiario_id")
    private Usuario beneficiarioRelacionado;
    @Column(name = "quantidade_reaproveitada")
    private Integer quantidadeReaproveitada;
    @Column(name = "concluida_em")
    private Instant concluidaEm;
    @Column(name = "co2_evitado_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal co2EvitadoKg = BigDecimal.ZERO;
    @Column(name = "peso_estimado_kg", nullable = false, precision = 14, scale = 2)
    private BigDecimal pesoEstimadoKg = BigDecimal.ZERO;
    @Column(name = "residuos_evitados_kg", nullable = false, precision = 14, scale = 2)
    private BigDecimal residuosEvitadosKg = BigDecimal.ZERO;
}
