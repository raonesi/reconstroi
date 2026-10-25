package com.reconstroi.backend.exception;

public class MaterialFinalizadoException extends BusinessException {
    public MaterialFinalizadoException() { super(org.springframework.http.HttpStatus.CONFLICT, "Materiais finalizados não podem ser editados."); }
}
