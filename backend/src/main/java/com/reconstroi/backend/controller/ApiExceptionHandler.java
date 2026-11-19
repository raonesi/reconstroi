package com.reconstroi.backend.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import com.reconstroi.backend.exception.BusinessException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ProblemDetail> business(BusinessException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
        detail.setTitle("Não foi possível concluir a operação");
        return ResponseEntity.status(exception.getStatus()).body(detail);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Revise os campos informados.");
        detail.setTitle("Dados inválidos");
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        detail.setProperty("campos", fields); return detail;
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail mismatch(MethodArgumentTypeMismatchException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + exception.getName());
        detail.setTitle("Parâmetro inválido"); return detail;
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail upload(MaxUploadSizeExceededException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, "O arquivo excede o limite permitido.");
        detail.setTitle("Arquivo muito grande"); return detail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail bodyInvalido(HttpMessageNotReadableException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
            "O corpo da requisição contém campos ou valores inválidos. Confira os valores permitidos para os campos ENUM.");
        detail.setTitle("Dados inválidos");
        return detail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(DataIntegrityViolationException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
            "A operação viola uma regra de integridade dos dados.");
        detail.setTitle("Conflito de dados");
        return detail;
    }
}
