package com.udea.skillbridge.seguridad.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response unificado para login, registro y refresh. Incluye access token
 * (corta duración) y refresh token (larga duración).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
	
    private String accessToken;
    private String refreshToken;

    @JsonProperty("tokenType")
    private String tipoToken;       // "Bearer"

    @JsonProperty("expiresIn")
    private Long expiraEn;          // segundos hasta expiración del access token

    @JsonProperty("user")
    private UsuarioResponse usuario;

    @JsonProperty("profileCompleted")
    private Boolean perfilCompletado;  // ← el frontend decide si redirigir
}
