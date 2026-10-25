package com.reconstroi.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.exception.UsuarioSemPermissaoException;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.DoacaoRepository;

@Service
public class DoacaoService {
    private final DoacaoRepository repository;
    private final MaterialService materialService;
    private final AutorizacaoService autorizacaoService;

    public DoacaoService(DoacaoRepository repository, MaterialService materialService, AutorizacaoService autorizacaoService) {
        this.repository = repository;
        this.materialService = materialService;
        this.autorizacaoService = autorizacaoService;
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponse> listar(Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        if (usuario.getTipo() == TipoUsuario.ADMIN) return listarTodas(usuario);
        return repository.findByDoadorRelacionadoIdOrBeneficiarioRelacionadoIdOrderByConcluidaEmDesc(usuario.getId(), usuario.getId())
            .stream().filter(this::valida).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponse> listarTodas(Usuario admin) {
        autorizacaoService.exigirAtivo(admin);
        if (admin.getTipo() != TipoUsuario.ADMIN) throw new UsuarioSemPermissaoException("Ação exclusiva de administradores.");
        return repository.findAllByOrderByConcluidaEmDesc().stream().filter(this::valida).map(this::toResponse).toList();
    }

    private boolean valida(Doacao d) {
        return d.getMaterialRelacionado() != null && d.getDoadorRelacionado() != null && d.getBeneficiarioRelacionado() != null;
    }

    private DoacaoResponse toResponse(Doacao d) {
        return new DoacaoResponse(d.getId(), materialService.toResumo(d.getMaterialRelacionado()),
            UsuarioResumo.from(d.getDoadorRelacionado()), UsuarioResumo.from(d.getBeneficiarioRelacionado()),
            d.getQuantidadeReaproveitada(), d.getConcluidaEm(), d.getPesoEstimadoKg(),
            d.getResiduosEvitadosKg(), d.getCo2EvitadoKg());
    }
}
