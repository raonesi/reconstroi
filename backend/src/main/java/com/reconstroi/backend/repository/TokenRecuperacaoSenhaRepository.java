package com.reconstroi.backend.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.reconstroi.backend.model.TokenRecuperacaoSenha;

public interface TokenRecuperacaoSenhaRepository extends JpaRepository<TokenRecuperacaoSenha, Long> {
    Optional<TokenRecuperacaoSenha> findByTokenHashAndUsadoFalseAndExpiraEmAfter(String hash, Instant agora);
}
