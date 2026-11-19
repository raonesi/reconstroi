package com.reconstroi.backend.exception;

public class QuantidadeInvalidaException extends BusinessException {
    public QuantidadeInvalidaException(String mensagem) { super(org.springframework.http.HttpStatus.BAD_REQUEST, mensagem); }
}
