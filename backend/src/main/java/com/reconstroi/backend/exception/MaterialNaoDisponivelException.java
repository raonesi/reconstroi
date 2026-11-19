package com.reconstroi.backend.exception;

public class MaterialNaoDisponivelException extends BusinessException {
    public MaterialNaoDisponivelException() { super(org.springframework.http.HttpStatus.CONFLICT, "Este material não está disponível."); }
}
