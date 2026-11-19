package com.reconstroi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.reconstroi.backend.model.Denuncia;
import com.reconstroi.backend.model.StatusDenuncia;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {
    long countByStatus(StatusDenuncia status);
}
