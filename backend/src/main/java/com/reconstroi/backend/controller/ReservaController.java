package com.reconstroi.backend.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.model.Usuario;
import com.reconstroi.backend.service.ReservaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/reservas")
public class ReservaController {
    private final ReservaService service;
    public ReservaController(ReservaService service) { this.service = service; }
    @GetMapping public List<ReservaResponse> minhas(@AuthenticationPrincipal Usuario usuario) { return service.minhas(usuario); }
    @GetMapping("/recebidas") @PreAuthorize("hasAuthority('RESERVA_GERENCIAR_RECEBIDA')")
    public List<ReservaResponse> recebidas(@AuthenticationPrincipal Usuario usuario) { return service.recebidas(usuario); }
    @PostMapping @PreAuthorize("hasAuthority('RESERVA_SOLICITAR')")
    public ResponseEntity<ReservaResponse> solicitar(@Valid @RequestBody ReservaRequest request, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitar(request, usuario));
    }
    @PatchMapping("/{id}/aprovar") @PreAuthorize("hasAuthority('RESERVA_GERENCIAR_RECEBIDA')")
    public ReservaResponse aprovar(@PathVariable Long id, @AuthenticationPrincipal Usuario u) { return service.aprovar(id, u); }
    @PatchMapping("/{id}/recusar") @PreAuthorize("hasAuthority('RESERVA_GERENCIAR_RECEBIDA')")
    public ReservaResponse recusar(@PathVariable Long id, @AuthenticationPrincipal Usuario u) { return service.recusar(id, u); }
    @PatchMapping("/{id}/cancelar") public ReservaResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Usuario u) { return service.cancelar(id, u); }
    @PatchMapping("/{id}/confirmar-retirada") @PreAuthorize("hasAuthority('RESERVA_GERENCIAR_RECEBIDA')")
    public ReservaResponse retirar(@PathVariable Long id, @AuthenticationPrincipal Usuario u) { return service.confirmarRetirada(id, u); }
    @PatchMapping("/{id}/finalizar") @PreAuthorize("hasAuthority('RESERVA_GERENCIAR_RECEBIDA')")
    public ReservaResponse finalizar(@PathVariable Long id, @AuthenticationPrincipal Usuario u) { return service.finalizar(id, u); }
}
