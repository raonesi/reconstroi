package com.reconstroi.backend.service;

import org.springframework.stereotype.Service;
import com.reconstroi.backend.exception.UsuarioSemPermissaoException;
import com.reconstroi.backend.model.Usuario;

@Service
public class AutorizacaoService {
    public void exigirAtivo(Usuario usuario) {
        if (usuario == null || !usuario.isAtivo()) {
            throw new UsuarioSemPermissaoException("Usuário bloqueado ou não autenticado.");
        }
    }
}
