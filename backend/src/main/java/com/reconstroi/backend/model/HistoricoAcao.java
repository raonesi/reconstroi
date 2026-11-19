package com.reconstroi.backend.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historico_acao")
@Getter @Setter @NoArgsConstructor
public class HistoricoAcao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acao", nullable = false, length = 50)
    private TipoAcaoHistorico tipoAcao;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(length = 64)
    private String ip;

    @Column(name = "recurso_tipo", nullable = false, length = 40)
    private String recursoTipo;

    @Column(name = "recurso_id", nullable = false)
    private Long recursoId;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm = Instant.now();
}
