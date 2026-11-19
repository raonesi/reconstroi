package com.reconstroi.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.reconstroi.backend.model.Usuario;

@Service
public class EmailService {
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

    public void enviarRecuperacao(Usuario usuario, String token) {
        if (!enabled) return;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(usuario.getEmail());
        message.setSubject("Recuperação de senha — ReConstrói");
        message.setText("Olá, " + usuario.getNome() + "!\n\nUse o token abaixo para redefinir sua senha em "
            + frontendUrl + "/recuperar-senha:\n\n" + token + "\n\nO token expira em 30 minutos.");
        mailSender.send(message);
    }

    public void enviarStatusReserva(Usuario usuario, String material, boolean aprovada) {
        if (!enabled) return;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(usuario.getEmail());
        message.setSubject((aprovada ? "Reserva aprovada" : "Reserva recusada") + " — ReConstrói");
        message.setText("Olá, " + usuario.getNome() + "!\n\nSua solicitação para “" + material + "” foi "
            + (aprovada ? "aprovada. Consulte a plataforma para combinar a retirada." : "recusada.")
            + "\n\n" + frontendUrl + "/minhas-reservas");
        mailSender.send(message);
    }
}
