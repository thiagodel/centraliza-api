package com.centraliza.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> tratarResponseStatus(
            ResponseStatusException ex) {

        HttpStatus status = HttpStatus.valueOf(
                ex.getStatusCode().value()
        );

        ErroResponse erro = new ErroResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getReason()
        );

        return ResponseEntity.status(status).body(erro);
    }

    public record ErroResponse(
            Instant timestamp,
            int status,
            String erro,
            String mensagem
    ) {}
}