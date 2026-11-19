package com.reconstroi.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.reconstroi.backend.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByAtivoTrueOrderByNome();
    Optional<Categoria> findBySlugIgnoreCase(String slug);
}
