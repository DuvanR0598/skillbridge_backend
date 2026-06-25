package com.udea.skillbridge.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Excepción para recursos no encontrados.
 * Siempre retorna 404.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	private final HttpStatus status = HttpStatus.NOT_FOUND;
    private final String errorCode;

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " no encontrado con id: " + id);
        this.errorCode = resource.toUpperCase() + "_NOT_FOUND";
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = "RESOURCE_NOT_FOUND";
    }

}
