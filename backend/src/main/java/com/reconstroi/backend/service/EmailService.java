package com.reconstroi.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String frontendUrl;
    private final String from;

    public EmailService(JavaMailSender mailSender, @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl,
            @Value("${spring.mail.username:no-reply@reconstroi.local}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.frontendUrl = frontendUrl;
        this.from = from == null || from.isBlank() ? "no-reply@reconstroi.local" : from;
    }

    /**
     * Executa depois do commit e fora da thread da requisição: falha ou lentidão do SMTP
     * não desfaz a decisão da reserva nem segura a transação.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarStatusReserva(StatusReservaAtualizada evento) {
        if (!enabled) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(evento.email());
            message.setSubject((evento.aprovada() ? "Reserva aprovada" : "Reserva recusada") + " — ReConstrói");
            message.setText("Olá, " + evento.nome() + "!\n\nSua solicitação para “" + evento.material() + "” foi "
                + (evento.aprovada() ? "aprovada. Consulte a plataforma para combinar a retirada." : "recusada.")
                + "\n\n" + frontendUrl + "/minhas-reservas");
            mailSender.send(message);
        } catch (RuntimeException erro) {
            log.warn("Não foi possível enviar o e-mail de status da reserva: {}", erro.getMessage());
        }
    }
}
