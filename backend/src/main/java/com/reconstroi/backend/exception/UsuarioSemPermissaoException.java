package com.reconstroi.backend.exception;

public class UsuarioSemPermissaoException extends BusinessException {
    public UsuarioSemPermissaoException(String mensagem) { super(org.springframework.http.HttpStatus.FORBIDDEN, mensagem); }
}
