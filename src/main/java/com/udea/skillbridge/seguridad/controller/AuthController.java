package com.udea.skillbridge.seguridad.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.seguridad.dto.request.LoginRequest;
import com.udea.skillbridge.seguridad.dto.request.RefreshTokenRequest;
import com.udea.skillbridge.seguridad.dto.request.RegistrarRequest;
import com.udea.skillbridge.seguridad.dto.response.AuthResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegistrarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                    authService.register(request),
                    "Usuario registrado exitosamente"
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
            authService.login(request),
            "Login exitoso"
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
            authService.refresh(request),
            "Token renovado"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UsuarioEntity usuarioActual) {
        authService.logout(usuarioActual.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Sesión cerrada"));
    }

}
