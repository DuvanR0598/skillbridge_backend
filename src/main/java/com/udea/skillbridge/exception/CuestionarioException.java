package com.udea.skillbridge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

/**
 * Excepción de negocio para el módulo de cuestionarios.
 * Al anotarla con @ResponseStatus, Spring retorna automáticamente
 * el código HTTP correspondiente cuando se lanza esta excepción.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class CuestionarioException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final HttpStatus status;

    public CuestionarioException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
    
    public CuestionarioException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
