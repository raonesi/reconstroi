package com.reconstroi.backend.service;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reconstroi.backend.dto.ApiDtos.HistoricoResponse;
import com.reconstroi.backend.dto.ApiDtos.UsuarioResumo;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.HistoricoAcaoRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class HistoricoService {
    private final HistoricoAcaoRepository repository;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    public HistoricoService(HistoricoAcaoRepository repository, ObjectProvider<HttpServletRequest> requestProvider) {
        this.repository = repository;
        this.requestProvider = requestProvider;
    }

    @Transactional
    public void registrar(Usuario usuario, TipoAcaoHistorico tipo, String descricao, String recursoTipo, Long recursoId) {
        HistoricoAcao historico = new HistoricoAcao();
        historico.setUsuario(usuario);
        historico.setTipoAcao(tipo);
        historico.setDescricao(descricao);
        historico.setIp(ipAtual());
        historico.setRecursoTipo(recursoTipo);
        historico.setRecursoId(recursoId);
        repository.save(historico);
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> meus(Usuario usuario) {
        return repository.findByUsuarioIdOrderByOcorridoEmDesc(usuario.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> doRecurso(String tipo, Long id) {
        return repository.findByRecursoTipoAndRecursoIdOrderByOcorridoEmDesc(tipo, id).stream().map(this::toResponse).toList();
    }

    private String ipAtual() {
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
        return ip == null || ip.isBlank() ? null : ip.substring(0, Math.min(ip.length(), 64));
    }

    private HistoricoResponse toResponse(HistoricoAcao h) {
        return new HistoricoResponse(h.getId(), h.getTipoAcao(), h.getDescricao(), h.getIp(), h.getRecursoTipo(),
            h.getRecursoId(), UsuarioResumo.from(h.getUsuario()), h.getOcorridoEm());
    }
}
