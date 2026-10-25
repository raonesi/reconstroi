package com.reconstroi.backend.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final DenunciaService denunciaService;
    private final ReservaService reservaService;
    private final DoacaoService doacaoService;
    private final RelatorioService relatorioService;

    public AdminController(AdminService adminService, DenunciaService denunciaService, ReservaService reservaService,
            DoacaoService doacaoService, RelatorioService relatorioService) {
        this.adminService = adminService;
        this.denunciaService = denunciaService;
        this.reservaService = reservaService;
        this.doacaoService = doacaoService;
        this.relatorioService = relatorioService;
    }

    @GetMapping("/usuarios")
    public Page<UsuarioResponse> usuarios(@PageableDefault(size = 20, sort = "criadoEm") Pageable page,
            @AuthenticationPrincipal Usuario admin) {
        return adminService.listarUsuarios(page, admin);
    }

    @PatchMapping("/usuarios/{id}/ativo")
    public UsuarioResponse alterarUsuario(@PathVariable Long id, @Valid @RequestBody AtivoRequest request,
            @AuthenticationPrincipal Usuario admin) {
        return adminService.alterarAtivo(id, request.ativo(), admin);
    }

    @GetMapping("/materiais")
    public Page<MaterialResponse> materiais(@PageableDefault(size = 20, sort = "dataCadastro") Pageable page,
            @AuthenticationPrincipal Usuario admin) {
        return adminService.listarMateriais(page, admin);
    }

    @DeleteMapping("/materiais/{id}")
    public ResponseEntity<Void> removerMaterial(@PathVariable Long id, @RequestBody(required = false) RemocaoRequest request,
            @AuthenticationPrincipal Usuario admin) {
        adminService.removerMaterial(id, request == null ? null : request.motivo(), admin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservas")
    public List<ReservaResponse> reservas(@AuthenticationPrincipal Usuario admin) { return reservaService.todas(admin); }

    @GetMapping("/doacoes")
    public List<DoacaoResponse> doacoes(@AuthenticationPrincipal Usuario admin) { return doacaoService.listarTodas(admin); }

    @GetMapping("/denuncias")
    public List<DenunciaResponse> denuncias() { return denunciaService.listarTodas(); }

    @PatchMapping("/denuncias/{id}")
    public DenunciaResponse atualizarDenuncia(@PathVariable Long id, @Valid @RequestBody StatusDenunciaRequest request) {
        return denunciaService.atualizarStatus(id, request.status());
    }

    @GetMapping("/relatorios")
    public DashboardResponse relatorios() { return relatorioService.dashboard(); }

    public record AtivoRequest(boolean ativo) {}
    public record RemocaoRequest(@Size(max = 500) String motivo) {}
    public record StatusDenunciaRequest(@NotNull StatusDenuncia status) {}
}
