package com.reconstroi.backend.exception;

public class FluxoInvalidoException extends BusinessException {
    public FluxoInvalidoException(String mensagem) { super(org.springframework.http.HttpStatus.CONFLICT, mensagem); }
}
