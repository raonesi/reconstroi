package com.reconstroi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.reconstroi.backend.model.TipoUsuario;

import com.reconstroi.backend.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);
    long countByTipo(TipoUsuario tipo);
    long countByAtivoTrue();

}
