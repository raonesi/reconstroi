package com.reconstroi.backend.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.reconstroi.backend.exception.BusinessException;

/**
 * Limita tentativas seguidas (login errado, pedidos de recuperação de senha) por e-mail.
 * Sem isso dava para testar milhares de senhas por minuto contra uma conta.
 * Fica em memória: ao reiniciar a API o contador zera, o que é suficiente para o app local.
 */
@Component
public class LimiteTentativas {
    static final int MAXIMO = 10;
    static final Duration JANELA = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> falhas = new ConcurrentHashMap<>();

    /** Lança 429 se a chave já atingiu o máximo de tentativas dentro da janela. */
    public void verificar(String chave) {
        Deque<Instant> registro = falhas.get(chave);
        if (registro == null) return;
        synchronized (registro) {
            limparAntigas(registro);
            if (registro.size() >= MAXIMO)
                throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas seguidas. Aguarde 15 minutos e tente novamente.");
        }
    }

    public void registrarFalha(String chave) {
        Deque<Instant> registro = falhas.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (registro) {
            limparAntigas(registro);
            registro.addLast(Instant.now());
        }
    }

    public void limpar(String chave) { falhas.remove(chave); }

    private static void limparAntigas(Deque<Instant> registro) {
        Instant limite = Instant.now().minus(JANELA);
        while (!registro.isEmpty() && registro.peekFirst().isBefore(limite)) registro.pollFirst();
    }
}
