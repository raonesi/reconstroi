package com.reconstroi.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.exception.*;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.*;

@Service
public class AdminService {
    private final UsuarioRepository usuarios;
    private final MaterialRepository materiais;
    private final MaterialService materialService;
    private final HistoricoService historicoService;
    private final AutorizacaoService autorizacaoService;

    public AdminService(UsuarioRepository usuarios, MaterialRepository materiais, MaterialService materialService,
            HistoricoService historicoService, AutorizacaoService autorizacaoService) {
        this.usuarios = usuarios;
        this.materiais = materiais;
        this.materialService = materialService;
        this.historicoService = historicoService;
        this.autorizacaoService = autorizacaoService;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarUsuarios(Pageable page, Usuario admin) {
        exigirAdmin(admin);
        return usuarios.findAll(page).map(UsuarioResponse::from);
    }

    @Transactional
    public UsuarioResponse alterarAtivo(Long id, boolean ativo, Usuario admin) {
        exigirAdmin(admin);
        if (admin.getId().equals(id) && !ativo)
            throw new FluxoInvalidoException("O administrador não pode bloquear a própria conta.");
        Usuario usuario = usuarios.findById(id).orElseThrow(() -> BusinessException.notFound("Usuário não encontrado."));
        usuario.setAtivo(ativo);
        historicoService.registrar(admin, ativo ? TipoAcaoHistorico.USUARIO_DESBLOQUEADO : TipoAcaoHistorico.USUARIO_BLOQUEADO,
            "Usuário “" + usuario.getNome() + "” " + (ativo ? "desbloqueado." : "bloqueado."), "USUARIO", usuario.getId());
        return UsuarioResponse.from(usuario);
    }

    @Transactional(readOnly = true)
    public Page<MaterialResponse> listarMateriais(Pageable page, Usuario admin) {
        exigirAdmin(admin);
        return materiais.findByExcluidoFalse(page).map(materialService::toResponse);
    }

    @Transactional
    public void removerMaterial(Long id, String motivo, Usuario admin) {
        exigirAdmin(admin);
        materialService.removerComoAdmin(id, admin, motivo);
    }

    private void exigirAdmin(Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        if (usuario.getTipo() != TipoUsuario.ADMIN) throw new UsuarioSemPermissaoException("Ação exclusiva de administradores.");
    }
}
