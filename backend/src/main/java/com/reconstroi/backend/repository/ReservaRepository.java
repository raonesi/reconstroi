package com.reconstroi.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.reconstroi.backend.model.Reserva;
import com.reconstroi.backend.model.StatusReserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findBySolicitanteRelacionadoIdOrderByCriadoEmDesc(Long usuarioId);
    List<Reserva> findByMaterialRelacionadoProprietarioIdOrderByCriadoEmDesc(Long usuarioId);
    List<Reserva> findByMaterialRelacionadoIdAndStatusReserva(Long materialId, StatusReserva status);
    Optional<Reserva> findByMaterialRelacionadoIdAndSolicitanteRelacionadoIdAndStatusReservaIn(
        Long materialId, Long usuarioId, List<StatusReserva> statuses);
    boolean existsByMaterialRelacionadoIdAndStatusReservaIn(Long materialId, List<StatusReserva> statuses);
    List<Reserva> findAllByOrderByCriadoEmDesc();
    long countByStatusReserva(StatusReserva status);
}
