package com.reconstroi.backend.exception;

public class MaterialDuplicadoException extends BusinessException {
    public MaterialDuplicadoException() {
        super(org.springframework.http.HttpStatus.CONFLICT,
            "Já existe um material com o mesmo nome, endereço e quantidade cadastrado hoje. Confirme para publicar mesmo assim.");
    }
}
