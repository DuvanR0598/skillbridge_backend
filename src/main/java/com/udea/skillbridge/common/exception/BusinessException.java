package com.udea.skillbridge.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Excepción para violaciones de reglas de negocio.
 * Ej: "No se puede publicar un cuestionario en DRAFT"
 */
@Getter
public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final HttpStatus status;
    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = errorCode;
    }

}
