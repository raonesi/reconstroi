package com.reconstroi.backend.service;

import java.time.Duration;
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
            .issuedAt(Date.from(agora))
            .expiration(Date.from(expiracao))
            .signWith(key)
            .compact();
        return new TokenResult(token, expiracao);
    }

    public String extrairEmail(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    public record TokenResult(String token, Instant expiraEm) {}
}
