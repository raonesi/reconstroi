package com.reconstroi.backend.exception;

public class ReservaJaExistenteException extends BusinessException {
    public ReservaJaExistenteException() { super(org.springframework.http.HttpStatus.CONFLICT, "Este material já possui uma reserva ativa."); }
}
