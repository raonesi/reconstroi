package com.reconstroi.backend.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token_recuperacao_senha")
@Getter @Setter @NoArgsConstructor
public class TokenRecuperacaoSenha {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;
    @Column(nullable = false)
    private boolean usado;
}
