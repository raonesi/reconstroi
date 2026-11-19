package com.reconstroi.backend.exception;

public class FotoObrigatoriaException extends BusinessException {
    public FotoObrigatoriaException() { super(org.springframework.http.HttpStatus.BAD_REQUEST, "Adicione pelo menos uma foto do material."); }
}
