package com.reconstroi.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reconstroi.backend.dto.ApiDtos.NotificacaoResponse;
import com.reconstroi.backend.exception.BusinessException;
import com.reconstroi.backend.model.Notificacao;
import com.reconstroi.backend.model.Usuario;
import com.reconstroi.backend.repository.NotificacaoRepository;

@Service
public class NotificacaoService {
    private final NotificacaoRepository repository;

    public NotificacaoService(NotificacaoRepository repository) { this.repository = repository; }

    @Transactional
    public void enviar(Usuario usuario, String titulo, String mensagem) {
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuario);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        repository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listar(Usuario usuario) {
        return repository.findByUsuarioIdOrderByCriadaEmDesc(usuario.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public NotificacaoResponse marcarComoLida(Long id, Usuario usuario) {
        Notificacao notificacao = repository.findByIdAndUsuarioId(id, usuario.getId())
            .orElseThrow(() -> BusinessException.notFound("Notificação não encontrada."));
        notificacao.setLida(true);
        return toResponse(notificacao);
    }

    private NotificacaoResponse toResponse(Notificacao n) {
        return new NotificacaoResponse(n.getId(), n.getTitulo(), n.getMensagem(), n.isLida(), n.getCriadaEm());
    }
}
