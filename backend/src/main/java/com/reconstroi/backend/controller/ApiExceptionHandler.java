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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
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

    @ExceptionHandler(MissingServletRequestPartException.class)
    ProblemDetail parteAusente(MissingServletRequestPartException exception) {
        String parte = exception.getRequestPartName();
        String mensagem = "fotos".equals(parte) || "arquivo".equals(parte)
            ? "Envie ao menos uma foto do material (JPG, PNG ou WebP de até 5 MB)."
            : "A requisição está incompleta: falta a parte \"" + parte + "\".";
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, mensagem);
        detail.setTitle("Dados inválidos");
        return detail;
    }

    /** @PreAuthorize negado dentro do controller: devolve 403 com a mesma estrutura dos demais erros. */
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail semPermissao(AccessDeniedException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta ação.");
        detail.setTitle("Acesso negado");
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
