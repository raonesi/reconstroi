package com.reconstroi.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.exception.BusinessException;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.UsuarioRepository;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse cadastrar(CadastroUsuarioRequest dados) {
        String email = dados.email().trim().toLowerCase();
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw BusinessException.conflict("Este e-mail já está cadastrado.");
        }
        if (dados.tipo() == TipoUsuario.ADMIN) {
            throw BusinessException.forbidden("Administradores não podem ser criados pelo cadastro público.");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setTelefone(dados.telefone().trim());
        usuario.setSenha(passwordEncoder.encode(dados.senha()));
        usuario.setTipo(dados.tipo());
        usuario.setCidade(limparTexto(dados.cidade()));
        usuario.setEstado(formatarEstado(dados.estado()));
        usuario.setDocumento(limparTexto(dados.documento()));
        usuarioRepository.save(usuario);
        return autenticar(usuario);
    }

    @Transactional
    public AuthResponse login(LoginRequest dados) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dados.email().trim())
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));
        boolean senhaCorreta = passwordEncoder.matches(dados.senha(), usuario.getSenha());
        if (!usuario.isAtivo() || !senhaCorreta) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
        return autenticar(usuario);
    }

    @Transactional
    public void encerrarSessao(String token) {
        jwtService.revogar(token);
    }

    @Transactional
    public UsuarioResponse atualizar(Usuario usuario, AtualizarPerfilRequest dados) {
        usuario.setNome(dados.nome().trim());
        usuario.setTelefone(dados.telefone().trim());
        usuario.setCidade(limparTexto(dados.cidade()));
        usuario.setEstado(formatarEstado(dados.estado()));
        usuario.setDocumento(limparTexto(dados.documento()));
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    private AuthResponse autenticar(Usuario usuario) {
        var jwt = jwtService.gerar(usuario);
        return new AuthResponse(UsuarioResponse.from(usuario), jwt.token(), jwt.expiraEm());
    }
    private String limparTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private String formatarEstado(String texto) {
        String estado = limparTexto(texto);
        if (estado == null) {
            return null;
        }
        return estado.toUpperCase();
    }
}
