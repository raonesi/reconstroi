package com.reconstroi.backend.service;

import java.time.Instant;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.exception.*;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.*;

@Service
public class ReservaService {
    private final ReservaRepository repository;
    private final DoacaoRepository doacaoRepository;
    private final MaterialService materialService;
    private final HistoricoService historicoService;
    private final NotificacaoService notificacaoService;
    private final ApplicationEventPublisher eventos;
    private final ImpactoAmbientalService impactoService;
    private final AutorizacaoService autorizacaoService;

    public ReservaService(ReservaRepository repository, DoacaoRepository doacaoRepository, MaterialService materialService,
            HistoricoService historicoService, NotificacaoService notificacaoService, ApplicationEventPublisher eventos,
            ImpactoAmbientalService impactoService, AutorizacaoService autorizacaoService) {
        this.repository = repository;
        this.doacaoRepository = doacaoRepository;
        this.materialService = materialService;
        this.historicoService = historicoService;
        this.notificacaoService = notificacaoService;
        this.eventos = eventos;
        this.impactoService = impactoService;
        this.autorizacaoService = autorizacaoService;
    }

    @Transactional
    public ReservaResponse solicitar(ReservaRequest request, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        if (usuario.getTipo() != TipoUsuario.BENEFICIARIO_ONG && usuario.getTipo() != TipoUsuario.BENEFICIARIO_FAMILIA)
            throw new UsuarioSemPermissaoException("Apenas ONGs e famílias podem solicitar materiais.");
        if (request == null || request.materialId() == null || request.quantidade() == null || request.quantidade() <= 0)
            throw new QuantidadeInvalidaException("A quantidade solicitada deve ser maior que zero.");

        Material material = materialService.findForUpdate(request.materialId());
        if (material.getProprietario() == null)
            throw new FluxoInvalidoException("Este anúncio legado precisa ser atualizado pelo administrador antes de receber reservas.");
        if (material.getStatus() != StatusMaterial.DISPONIVEL) throw new MaterialNaoDisponivelException();
        if (material.getProprietario().getId().equals(usuario.getId()))
            throw new UsuarioSemPermissaoException("Você não pode reservar seu próprio material.");
        if (request.quantidade() > material.getQuantidade())
            throw new QuantidadeInvalidaException("A quantidade solicitada supera a disponível.");
        if (repository.existsByMaterialRelacionadoIdAndStatusReservaIn(material.getId(), StatusReserva.ativos()))
            throw new ReservaJaExistenteException();

        Reserva reserva = new Reserva();
        reserva.setMaterialRelacionado(material);
        reserva.setSolicitanteRelacionado(usuario);
        reserva.setQuantidadeSolicitada(request.quantidade());
        reserva.setMensagem(trim(request.mensagem()));
        try {
            repository.saveAndFlush(reserva);
        } catch (DataIntegrityViolationException exception) {
            throw new ReservaJaExistenteException();
        }
        historicoService.registrar(usuario, TipoAcaoHistorico.SOLICITACAO_CRIADA,
            "Solicitação criada para “" + material.getNome() + "”.", "RESERVA", reserva.getId());
        notificacaoService.enviar(material.getProprietario(), "Nova solicitação recebida",
            usuario.getNome() + " solicitou " + reserva.getQuantidadeSolicitada() + " " + material.getUnidade()
                + " de “" + material.getNome() + "”.");
        return toResponse(reserva);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> minhas(Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        return repository.findBySolicitanteRelacionadoIdOrderByCriadoEmDesc(usuario.getId()).stream()
            .filter(this::isNova).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> recebidas(Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        if (!usuario.getTipo().isDoador()) throw new UsuarioSemPermissaoException("Ação exclusiva de doadores.");
        if (usuario.getTipo() == TipoUsuario.ADMIN) return todas(usuario);
        return repository.findByMaterialRelacionadoProprietarioIdOrderByCriadoEmDesc(usuario.getId()).stream()
            .filter(this::isNova).map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> todas(Usuario admin) {
        autorizacaoService.exigirAtivo(admin);
        if (admin.getTipo() != TipoUsuario.ADMIN) throw new UsuarioSemPermissaoException("Ação exclusiva de administradores.");
        return repository.findAllByOrderByCriadoEmDesc().stream().filter(this::isNova).map(this::toResponse).toList();
    }

    @Transactional
    public ReservaResponse aprovar(Long id, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        Reserva reserva = find(id);
        validarDoador(reserva, usuario);
        exigir(reserva, StatusReserva.SOLICITADA);
        Material material = materialService.findForUpdate(reserva.getMaterialRelacionado().getId());
        if (material.getStatus() != StatusMaterial.DISPONIVEL) throw new MaterialNaoDisponivelException();
        if (reserva.getQuantidadeSolicitada() > material.getQuantidade())
            throw new QuantidadeInvalidaException("A quantidade disponível ficou menor que a solicitada.");
        reserva.setStatusReserva(StatusReserva.APROVADA);
        material.setStatus(StatusMaterial.RESERVADO);
        historicoService.registrar(usuario, TipoAcaoHistorico.SOLICITACAO_ACEITA,
            "Solicitação aprovada para “" + material.getNome() + "”.", "RESERVA", reserva.getId());
        notificarStatus(reserva, true);
        return toResponse(reserva);
    }

    @Transactional
    public ReservaResponse recusar(Long id, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        Reserva reserva = find(id);
        validarDoador(reserva, usuario);
        exigir(reserva, StatusReserva.SOLICITADA);
        reserva.setStatusReserva(StatusReserva.RECUSADA);
        historicoService.registrar(usuario, TipoAcaoHistorico.SOLICITACAO_RECUSADA,
            "Solicitação recusada para “" + reserva.getMaterialRelacionado().getNome() + "”.", "RESERVA", reserva.getId());
        notificarStatus(reserva, false);
        return toResponse(reserva);
    }

    @Transactional
    public ReservaResponse cancelar(Long id, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        Reserva reserva = find(id);
        boolean solicitante = reserva.getSolicitanteRelacionado().getId().equals(usuario.getId());
        boolean doador = usuario.getTipo() == TipoUsuario.ADMIN
            || reserva.getMaterialRelacionado().getProprietario().getId().equals(usuario.getId());
        if (!solicitante && !doador) throw new UsuarioSemPermissaoException("Você não pode cancelar esta reserva.");
        if (solicitante && !doador && reserva.getStatusReserva() != StatusReserva.SOLICITADA)
            throw new FluxoInvalidoException("O beneficiário só pode cancelar sua solicitação enquanto ela aguarda aprovação.");
        if (doador && reserva.getStatusReserva() != StatusReserva.SOLICITADA
                && reserva.getStatusReserva() != StatusReserva.APROVADA)
            throw new FluxoInvalidoException("Esta reserva não pode mais ser cancelada.");
        if (reserva.getStatusReserva() == StatusReserva.APROVADA)
            reserva.getMaterialRelacionado().setStatus(StatusMaterial.DISPONIVEL);
        reserva.setStatusReserva(StatusReserva.CANCELADA);
        historicoService.registrar(usuario, TipoAcaoHistorico.RESERVA_CANCELADA,
            "Reserva cancelada para “" + reserva.getMaterialRelacionado().getNome() + "”.", "RESERVA", reserva.getId());
        return toResponse(reserva);
    }

    @Transactional
    public ReservaResponse confirmarRetirada(Long id, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        Reserva reserva = find(id);
        validarDoador(reserva, usuario);
        exigir(reserva, StatusReserva.APROVADA);
        reserva.setStatusReserva(StatusReserva.RETIRADA);
        reserva.getMaterialRelacionado().setStatus(StatusMaterial.RETIRADO);
        historicoService.registrar(usuario, TipoAcaoHistorico.RETIRADA_CONFIRMADA,
            "Retirada confirmada para “" + reserva.getMaterialRelacionado().getNome() + "”.", "RESERVA", reserva.getId());
        return toResponse(reserva);
    }

    @Transactional
    public ReservaResponse finalizar(Long id, Usuario usuario) {
        autorizacaoService.exigirAtivo(usuario);
        Reserva reserva = find(id);
        validarDoador(reserva, usuario);
        exigir(reserva, StatusReserva.RETIRADA);
        reserva.setStatusReserva(StatusReserva.FINALIZADA);
        // Doação parcial: o que foi entregue sai do estoque; havendo saldo, o anúncio volta à busca.
        Material material = reserva.getMaterialRelacionado();
        int saldo = material.getQuantidade() - reserva.getQuantidadeSolicitada();
        if (saldo > 0) {
            material.setQuantidade(saldo);
            material.setStatus(StatusMaterial.DISPONIVEL);
        } else {
            material.setStatus(StatusMaterial.FINALIZADO);
        }
        if (doacaoRepository.findByReservaRelacionadaId(id).isEmpty()) {
            var impacto = impactoService.calcular(reserva.getMaterialRelacionado(), reserva.getQuantidadeSolicitada());
            Doacao doacao = new Doacao();
            doacao.setReservaRelacionada(reserva);
            doacao.setMaterialRelacionado(reserva.getMaterialRelacionado());
            doacao.setDoadorRelacionado(reserva.getMaterialRelacionado().getProprietario());
            doacao.setBeneficiarioRelacionado(reserva.getSolicitanteRelacionado());
            doacao.setQuantidadeReaproveitada(reserva.getQuantidadeSolicitada());
            doacao.setConcluidaEm(Instant.now());
            doacao.setPesoEstimadoKg(impacto.pesoEstimadoKg());
            doacao.setResiduosEvitadosKg(impacto.residuosEvitadosKg());
            doacao.setCo2EvitadoKg(impacto.co2EvitadoKg());
            doacaoRepository.save(doacao);
        }
        historicoService.registrar(usuario, TipoAcaoHistorico.DOACAO_FINALIZADA,
            "Doação finalizada para “" + material.getNome() + "”."
                + (saldo > 0 ? " Saldo de " + saldo + " " + material.getUnidade() + " voltou a ficar disponível." : ""),
            "RESERVA", reserva.getId());
        return toResponse(reserva);
    }

    Reserva find(Long id) {
        return repository.findById(id).filter(this::isNova)
            .orElseThrow(() -> BusinessException.notFound("Reserva não encontrada."));
    }

    private boolean isNova(Reserva r) {
        return r.getMaterialRelacionado() != null && r.getSolicitanteRelacionado() != null;
    }

    private void validarDoador(Reserva reserva, Usuario usuario) {
        if (usuario.getTipo() != TipoUsuario.ADMIN
                && !reserva.getMaterialRelacionado().getProprietario().getId().equals(usuario.getId()))
            throw new UsuarioSemPermissaoException("Somente o doador responsável pode executar esta ação.");
    }

    private void exigir(Reserva reserva, StatusReserva status) {
        if (reserva.getStatusReserva() != status)
            throw new FluxoInvalidoException("Transição inválida: era esperado o estado " + status + ".");
    }

    private void notificarStatus(Reserva reserva, boolean aprovada) {
        Usuario beneficiario = reserva.getSolicitanteRelacionado();
        String material = reserva.getMaterialRelacionado().getNome();
        notificacaoService.enviar(beneficiario, aprovada ? "Reserva aprovada" : "Reserva recusada",
            "Sua solicitação para “" + material + "” foi " + (aprovada ? "aprovada." : "recusada."));
        eventos.publishEvent(new StatusReservaAtualizada(beneficiario.getEmail(), beneficiario.getNome(), material, aprovada));
    }

    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public ReservaResponse toResponse(Reserva r) {
        return new ReservaResponse(r.getId(), materialService.toResumo(r.getMaterialRelacionado()),
            UsuarioResumo.from(r.getSolicitanteRelacionado()), r.getQuantidadeSolicitada(), r.getStatusReserva(),
            r.getMensagem(), r.getCriadoEm(), r.getAtualizadoEm());
    }
}
