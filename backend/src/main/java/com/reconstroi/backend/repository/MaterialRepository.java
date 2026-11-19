package com.reconstroi.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.reconstroi.backend.model.Material;

public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {
    List<Material> findByProprietarioIdOrderByDataCadastroDesc(Long proprietarioId);
    Page<Material> findByExcluidoFalse(Pageable pageable);
    long countByProprietarioId(Long proprietarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Material m where m.id = :id")
    Optional<Material> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select (count(m) > 0) from Material m
        where m.proprietario.id = :usuarioId and m.excluido = false
          and lower(m.nome) = lower(:nome) and lower(m.enderecoRetirada) = lower(:endereco)
          and m.quantidade = :quantidade and m.dataCadastro >= :inicio and m.dataCadastro < :fim
        """)
    boolean existsDuplicadoNoPeriodo(@Param("usuarioId") Long usuarioId, @Param("nome") String nome,
        @Param("endereco") String endereco, @Param("quantidade") Integer quantidade,
        @Param("inicio") Instant inicio, @Param("fim") Instant fim);
}
