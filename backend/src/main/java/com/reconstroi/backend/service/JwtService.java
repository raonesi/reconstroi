package com.reconstroi.backend.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

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
    private final JdbcTemplate jdbc;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-hours:24}") long expirationHours, JdbcTemplate jdbc) {
        this.key = criarChave(secret);
        this.expirationHours = expirationHours;
        this.jdbc = jdbc;
    }

    public TokenResult gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(Duration.ofHours(expirationHours));
        String token = Jwts.builder()
            .subject(usuario.getEmail())
            .claim("tipo", usuario.getTipo().name())
            .issuedAt(Date.from(agora))
            .expiration(Date.from(expiracao))
            .signWith(key)
            .compact();
        registrarSessao(usuario, token, expiracao);
        return new TokenResult(token, expiracao);
    }

    private static SecretKey criarChave(String secret) {
        if (secret == null || secret.isBlank())
            throw new IllegalStateException("Defina JWT_SECRET com uma chave Base64 de pelo menos 32 bytes.");
        try {
            return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret.trim()));
        } catch (IllegalArgumentException | io.jsonwebtoken.security.WeakKeyException e) {
            throw new IllegalStateException("JWT_SECRET deve ser Base64 válido e ter pelo menos 32 bytes.", e);
        }
    }

    /** O token só vale enquanto existir uma sessão não expirada registrada no login. */
    public boolean sessaoAtiva(String token) {
        Integer total = jdbc.queryForObject(
            "SELECT count(*) FROM sessao WHERE token_hash = ? AND expira_em > now()", Integer.class, calcularHash(token));
        return total != null && total > 0;
    }

    public void revogar(String token) {
        jdbc.update("DELETE FROM sessao WHERE token_hash = ?", calcularHash(token));
    }

    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "PT1H")
    public void removerSessoesExpiradas() {
        jdbc.update("DELETE FROM sessao WHERE expira_em <= now()");
    }

    private void registrarSessao(Usuario usuario, String token, Instant expiracao) {
        String hash = calcularHash(token);
        String sql = "INSERT INTO sessao (token_hash, usuario_id, expira_em) VALUES (?, ?, ?) "
            + "ON CONFLICT (token_hash) DO UPDATE SET expira_em = EXCLUDED.expira_em";
        jdbc.update(sql, hash, usuario.getId(), Timestamp.from(expiracao));
    }

    private String calcularHash(String token) {
        try {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8)));
            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    public String extrairEmail(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    public record TokenResult(String token, Instant expiraEm) {}
}
