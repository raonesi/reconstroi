package com.reconstroi.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.reconstroi.backend.model.Doacao;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {
    List<Doacao> findByDoadorRelacionadoIdOrBeneficiarioRelacionadoIdOrderByConcluidaEmDesc(Long doadorId, Long beneficiarioId);
    List<Doacao> findAllByOrderByConcluidaEmDesc();
    Optional<Doacao> findByReservaRelacionadaId(Long reservaId);
}
