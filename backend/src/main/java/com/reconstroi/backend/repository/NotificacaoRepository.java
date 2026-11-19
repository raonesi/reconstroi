package com.reconstroi.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.reconstroi.backend.model.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);
    Optional<Notificacao> findByIdAndUsuarioId(Long id, Long usuarioId);
    long countByUsuarioIdAndLidaFalse(Long usuarioId);
}
