package com.reconstroi.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.reconstroi.backend.model.HistoricoAcao;

public interface HistoricoAcaoRepository extends JpaRepository<HistoricoAcao, Long> {
    List<HistoricoAcao> findByUsuarioIdOrderByOcorridoEmDesc(Long usuarioId);
    List<HistoricoAcao> findByRecursoTipoAndRecursoIdOrderByOcorridoEmDesc(String recursoTipo, Long recursoId);
}
