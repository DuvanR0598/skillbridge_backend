package com.udea.skillbridge.seguridad.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.skillbridge.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Entry point para una API stateless con JWT.
 *
 * Cuando una petición no está autenticada (token ausente, inválido o expirado)
 * devolvemos 401 con un JSON estándar — NO redirigimos al login de OAuth2/Google.
 * Así el frontend puede atrapar el 401 e intentar refrescar el token.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
            response.getWriter(),
            ApiResponse.error(
                "No autenticado o la sesión expiró. Inicia sesión nuevamente.",
                "UNAUTHENTICATED"
            )
        );
    }
}
