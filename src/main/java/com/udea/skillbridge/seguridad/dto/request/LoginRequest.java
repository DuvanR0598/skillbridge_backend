package com.udea.skillbridge.seguridad.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
	
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    // Opcional — para mostrar sesiones activas por dispositivo
    private String deviceInfo;

}
