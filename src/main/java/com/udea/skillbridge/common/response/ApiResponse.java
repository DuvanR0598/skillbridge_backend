package com.udea.skillbridge.common.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * Wrapper estándar para todas las respuestas de la API.
 *
 * Éxito:  { success: true,  data: {...},  message: "Cuestionario creado" }
 * Error:  { success: false, data: null,   message: "...", errorCode: "..." }
 *
 * @JsonInclude(NON_NULL) evita que campos nulos aparezcan en el JSON.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	
	private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;
    
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
    
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, "Operación exitosa");
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

}
