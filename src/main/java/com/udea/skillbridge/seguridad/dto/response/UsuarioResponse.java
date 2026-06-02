package com.udea.skillbridge.seguridad.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import com.udea.skillbridge.seguridad.enums.AuthProvider;
import com.udea.skillbridge.seguridad.enums.TipoRol;

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
public class UsuarioResponse {
	
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String avatarUrl;
    private AuthProvider authProvider;
    private Boolean emailVerificado;
    private Set<TipoRol> roles;
    private Set<String> permisos;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

}
