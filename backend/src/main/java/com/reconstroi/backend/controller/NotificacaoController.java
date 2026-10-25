package com.reconstroi.backend.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.reconstroi.backend.dto.ApiDtos.NotificacaoResponse;
import com.reconstroi.backend.model.Usuario;
import com.reconstroi.backend.service.NotificacaoService;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {
    private final NotificacaoService service;
    public NotificacaoController(NotificacaoService service) { this.service = service; }
    @GetMapping public List<NotificacaoResponse> listar(@AuthenticationPrincipal Usuario usuario) { return service.listar(usuario); }
    @PatchMapping("/{id}/lida") public NotificacaoResponse lida(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return service.marcarComoLida(id, usuario);
    }
}
