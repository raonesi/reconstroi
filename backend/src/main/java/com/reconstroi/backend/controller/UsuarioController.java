package com.reconstroi.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.model.Usuario;
import com.reconstroi.backend.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }

    @PostMapping public ResponseEntity<AuthResponse> cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
    }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }
    @GetMapping("/me") public UsuarioResponse atual(@AuthenticationPrincipal Usuario usuario) { return UsuarioResponse.from(usuario); }
    @PutMapping("/me") public UsuarioResponse atualizar(@AuthenticationPrincipal Usuario usuario, @Valid @RequestBody AtualizarPerfilRequest request) {
        return service.atualizar(usuario, request);
    }
    @PostMapping("/recuperar-senha") public RecuperarSenhaResponse recuperar(@Valid @RequestBody RecuperarSenhaRequest request) {
        return service.solicitarRecuperacao(request);
    }
    @PostMapping("/redefinir-senha") public ResponseEntity<Void> redefinir(@Valid @RequestBody RedefinirSenhaRequest request) {
        service.redefinirSenha(request); return ResponseEntity.noContent().build();
    }
    @PostMapping("/logout") public ResponseEntity<Void> logout() { return ResponseEntity.noContent().build(); }
}
