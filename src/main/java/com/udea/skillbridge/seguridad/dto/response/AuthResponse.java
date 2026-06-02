package com.udea.skillbridge.seguridad.dto.response;

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
    private String tipoToken;       // "Bearer"
    private Long expiraEn;          // segundos hasta expiración del access token
    private UsuarioResponse usuario;
    private Boolean perfilCompletado;  // ← el frontend decide si redirigir
}
