package com.udea.skillbridge.seguridad.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.seguridad.dto.request.ActualizarUsuariosRolesRequest;
import com.udea.skillbridge.seguridad.dto.response.UsuarioResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
	
	private final UsuarioService userService;

    /**
     * Mi perfil — cualquier usuario autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> me(@AuthenticationPrincipal UsuarioEntity usuarioActual) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(usuarioActual.getId())));
    }

    /**
     * Listar todos — solo ADMIN.
     */
    @GetMapping("/listar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(userService.findAll()));
    }

    /**
     * Ver usuario por ID — solo ADMIN.
     */
    @GetMapping("/listar-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }

    /**
     * Actualizar roles — solo ADMIN.
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> updateRoles(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuariosRolesRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateRoles(id, request),
            "Roles actualizados"
        ));
    }

    /**
     * Habilitar/deshabilitar cuenta — solo ADMIN.
     */
    @PatchMapping("/{id}/toggle-enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleEnabled(@PathVariable Long id) {
        userService.toggleEnabled(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Estado de cuenta actualizado"));
    }

}
