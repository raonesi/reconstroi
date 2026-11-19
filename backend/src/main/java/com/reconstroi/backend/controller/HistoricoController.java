package com.reconstroi.backend.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.reconstroi.backend.dto.ApiDtos.HistoricoResponse;
import com.reconstroi.backend.model.Usuario;
import com.reconstroi.backend.service.HistoricoService;

@RestController
@RequestMapping("/historico")
public class HistoricoController {
    private final HistoricoService service;
    public HistoricoController(HistoricoService service) { this.service = service; }
    @GetMapping("/meu") public List<HistoricoResponse> meu(@AuthenticationPrincipal Usuario usuario) { return service.meus(usuario); }
    @GetMapping("/{tipo}/{id}") @PreAuthorize("hasRole('ADMIN')")
    public List<HistoricoResponse> recurso(@PathVariable String tipo, @PathVariable Long id) {
        return service.doRecurso(tipo.toUpperCase(), id);
    }
}
