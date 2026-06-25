package com.udea.skillbridge.seguridad.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.seguridad.dto.request.CompletarPerfilRequest;
import com.udea.skillbridge.seguridad.dto.response.EstadoPerfilResponse;
import com.udea.skillbridge.seguridad.dto.response.ProgramaIngenieriaResponse;
import com.udea.skillbridge.seguridad.dto.response.UsuarioPerfilResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.service.UsuarioPerfilService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UsuarioPerfilController {
	
    private final UsuarioPerfilService perfilService;

    /**
     * Mi perfil completo.
     */
    @GetMapping("/usuarios/me/perfil")
    public ResponseEntity<ApiResponse<UsuarioPerfilResponse>> getMyPerfil(
            @AuthenticationPrincipal UsuarioEntity usuarioActual) {
        return ResponseEntity.ok(ApiResponse.ok(
            perfilService.getMyPerfil(usuarioActual.getId())
        ));
    }
    
    /**
     * Ver perfil de otro usuario — solo ADMIN o TEACHER.
     */
    @GetMapping("/usuarios/{id}/perfil")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<ApiResponse<UsuarioPerfilResponse>> getPerfilByIdUsuario(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
        		perfilService.getPerfilByIdUsuario(id)
        ));
    }
    
    /**
     * Completar o actualizar mi perfil.
     */
    @PatchMapping("/usuarios/me/perfil")
    public ResponseEntity<ApiResponse<UsuarioPerfilResponse>> actualizarMyPerfil(
            @AuthenticationPrincipal UsuarioEntity usuarioActual,
            @Valid @RequestBody CompletarPerfilRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
        		perfilService.actualizarMyPerfil(usuarioActual.getId(), request),
            "Perfil actualizado"
        ));
    }
    
    /**
     * Subir foto de perfil.
     * Content-Type: multipart/form-data
     */
    @PostMapping(
        value = "/usuarios/me/perfil/avatar",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<UsuarioPerfilResponse>> subirAvatar(
            @AuthenticationPrincipal UsuarioEntity usuarioActual,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(
        		perfilService.subirAvatar(usuarioActual.getId(), file),
            "Foto de perfil actualizada"
        ));
    }
    
    /**
     * Estado de completitud: ¿qué le falta al perfil?
     */
    @GetMapping("/usuarios/me/perfil/estado")
    public ResponseEntity<ApiResponse<EstadoPerfilResponse>> getEstadoPerfil(
            @AuthenticationPrincipal UsuarioEntity usuarioActual) {
        return ResponseEntity.ok(ApiResponse.ok(
        		perfilService.getEstadoPerfil(usuarioActual.getId())
        ));
    }
    
    /**
     * Lista de programas de ingeniería disponibles.
     * Endpoint público — el frontend lo usa para poblar el selector.
     */
    @GetMapping("/perfil/programas")
    public ResponseEntity<ApiResponse<List<ProgramaIngenieriaResponse>>> listaProgramas() {
        return ResponseEntity.ok(ApiResponse.ok(perfilService.listaProgramas()));
    }

}
