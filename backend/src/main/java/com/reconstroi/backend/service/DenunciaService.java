package com.reconstroi.backend.service;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.exception.BusinessException;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.*;

@Service
public class DenunciaService {
    private final DenunciaRepository repository;
    private final MaterialRepository materiais;
    private final AutorizacaoService autorizacao;

    public DenunciaService(DenunciaRepository repository, MaterialRepository materiais, AutorizacaoService autorizacao) {
        this.repository = repository;
        this.materiais = materiais;
        this.autorizacao = autorizacao;
    }

    @Transactional
    public DenunciaResponse criar(DenunciaRequest request, Usuario usuario) {
        autorizacao.exigirAtivo(usuario);
        Material material = materiais.findById(request.materialId()).filter(m -> !m.isExcluido())
            .orElseThrow(() -> BusinessException.notFound("Material não encontrado."));
        Denuncia denuncia = new Denuncia();
        denuncia.setDenunciante(usuario);
        denuncia.setMaterial(material);
        denuncia.setMotivo(request.motivo().trim());
        return toResponse(repository.save(denuncia));
    }

    @Transactional(readOnly = true)
    public List<DenunciaResponse> listarTodas() { return repository.findAll().stream().map(this::toResponse).toList(); }

    @Transactional
    public DenunciaResponse atualizarStatus(Long id, StatusDenuncia status) {
        Denuncia denuncia = repository.findById(id).orElseThrow(() -> BusinessException.notFound("Denúncia não encontrada."));
        denuncia.setStatus(status);
        if (status == StatusDenuncia.RESOLVIDA || status == StatusDenuncia.ARQUIVADA) denuncia.setResolvidoEm(Instant.now());
        else denuncia.setResolvidoEm(null);
        return toResponse(denuncia);
    }

    public DenunciaResponse toResponse(Denuncia d) {
        return new DenunciaResponse(d.getId(), d.getMaterial() == null ? null : d.getMaterial().getId(),
            d.getMaterial() == null ? null : d.getMaterial().getNome(), UsuarioResumo.from(d.getDenunciante()),
            d.getMotivo(), d.getStatus(), d.getCriadoEm(), d.getResolvidoEm());
    }
}
