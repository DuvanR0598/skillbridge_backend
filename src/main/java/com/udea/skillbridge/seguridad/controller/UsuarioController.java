package com.udea.skillbridge.seguridad.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.seguridad.dto.request.ActualizarUsuariosRolesRequest;
import com.udea.skillbridge.seguridad.dto.request.CambiarContrasenaRequest;
import com.udea.skillbridge.seguridad.dto.response.EstudianteResumenResponse;
import com.udea.skillbridge.seguridad.dto.response.UsuarioResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.service.UsuarioExportService;
import com.udea.skillbridge.seguridad.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

	private final UsuarioService userService;
	private final UsuarioExportService userExportService;

    /**
     * Mi perfil — cualquier usuario autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> me(@AuthenticationPrincipal UsuarioEntity usuarioActual) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(usuarioActual.getId())));
    }

    /**
     * Cambiar mi propia contraseña — cualquier usuario autenticado.
     */
    @PatchMapping("/me/cambiar-contrasena")
    public ResponseEntity<ApiResponse<Void>> cambiarContrasena(
            @AuthenticationPrincipal UsuarioEntity usuarioActual,
            @Valid @RequestBody CambiarContrasenaRequest request) {
        userService.cambiarContrasena(usuarioActual.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Contraseña actualizada correctamente"));
    }

    /**
     * Exportar la información de todos los usuarios a XLSX — solo ADMIN.
     */
    @GetMapping("/exportar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportarUsuarios() {
        byte[] archivo = userExportService.exportarUsuariosXlsx();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"usuarios.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
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
     * Listar estudiantes (solo lectura) — ADMIN y COORDINADOR.
     */
    @GetMapping("/estudiantes")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<ApiResponse<List<EstudianteResumenResponse>>> listarEstudiantes() {
        return ResponseEntity.ok(ApiResponse.ok(userService.listarEstudiantes()));
    }

    /**
     * Exportar los estudiantes a XLSX, aplicando filtros — ADMIN y COORDINADOR.
     */
    @GetMapping("/estudiantes/exportar")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<byte[]> exportarEstudiantes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String programa) {
        byte[] archivo = userExportService.exportarEstudiantesXlsx(search, programa);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"estudiantes.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
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
