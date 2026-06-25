package com.udea.skillbridge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.dto.request.DimensionRequest;
import com.udea.skillbridge.dto.response.DimensionResponse;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.service.IDimensionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Gestión de dimensiones de Power Skills.
 * Solo ADMIN/COORDINADOR — el estudiante nunca debe ver las dimensiones evaluadas.
 */
@RestController
@RequestMapping("/dimension")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
public class DimensionController {

    private final IDimensionService dimensionService;

    @PostMapping("/crear")
    public ResponseEntity<ApiResponse<DimensionResponse>> crear(
            @Valid @RequestBody DimensionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(dimensionService.crear(request), "Dimensión creada exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DimensionResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DimensionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                dimensionService.actualizar(id, request), "Dimensión actualizada"));
    }

    /** Lista todas, o filtra por skill con ?skill=PENSAMIENTO_CRITICO */
    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<DimensionResponse>>> listar(
            @RequestParam(required = false) SkillTipo skill) {
        return ResponseEntity.ok(ApiResponse.ok(dimensionService.listar(skill)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DimensionResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(dimensionService.findById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        dimensionService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Dimensión eliminada"));
    }
}
