package com.reconstroi.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.reconstroi.backend.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationHours;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-hours:24}") long expirationHours) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationHours = expirationHours;
    }

    public TokenResult gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(Duration.ofHours(expirationHours));
        String token = Jwts.builder()
            .subject(usuario.getEmail())
            .claim("tipo", usuario.getTipo().name())
            .claim(VERSAO_SENHA, versaoSenha(usuario))
            .issuedAt(Date.from(agora))
            .expiration(Date.from(expiracao))
            .signWith(key)
            .compact();
        return new TokenResult(token, expiracao);
    }

    /** Valida assinatura e validade; lança JwtException se o token for inválido. */
    public Claims ler(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String extrairEmail(String token) {
        return ler(token).getSubject();
    }

    /**
     * O token guarda um resumo do hash da senha. Se a senha mudar (ex.: redefinição por suspeita de invasão),
     * o resumo deixa de bater e todos os tokens emitidos antes param de funcionar.
     */
    public boolean versaoConfere(Claims claims, Usuario usuario) {
        String versao = claims.get(VERSAO_SENHA, String.class);
        return versao != null && MessageDigest.isEqual(versao.getBytes(StandardCharsets.UTF_8),
            versaoSenha(usuario).getBytes(StandardCharsets.UTF_8));
    }

    private static String versaoSenha(Usuario usuario) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(usuario.getSenha().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo, 0, 8);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String VERSAO_SENHA = "sv";

    public record TokenResult(String token, Instant expiraEm) {}
}
