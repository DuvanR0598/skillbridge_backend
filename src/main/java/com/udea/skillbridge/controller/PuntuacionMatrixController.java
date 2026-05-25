package com.udea.skillbridge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.udea.skillbridge.dto.request.ActualizarPuntuacionMatrixRequest;
import com.udea.skillbridge.dto.request.PuntuacionMatrixRequest;
import com.udea.skillbridge.dto.response.PuntuacionMatrixResponse;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.service.IPuntuacionMatrixService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cuestionarios/{idCuestionario}/puntuacion_matrix")
@RequiredArgsConstructor
public class PuntuacionMatrixController {
	
	private final IPuntuacionMatrixService puntuacionMatrixService;
	
    @PostMapping("/crear")
    public ResponseEntity<ApiResponse<PuntuacionMatrixResponse>> crear(
            @PathVariable Long idCuestionario,
            @Valid @RequestBody PuntuacionMatrixRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                		puntuacionMatrixService.crear(idCuestionario, request),
                    "Entrada de matriz creada exitosamente"
                ));
    }
    
    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<PuntuacionMatrixResponse>>> findAll(
            @PathVariable Long idCuestionario,
            @RequestParam(required = false) SkillTipo skill) {

        List<PuntuacionMatrixResponse> resultado = (skill != null)
                ? puntuacionMatrixService.findByCuestionarioAndSkill(idCuestionario, skill)
                : puntuacionMatrixService.findByCuestionario(idCuestionario);

        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
    
    @GetMapping("/{matrixId}/id")
    public ResponseEntity<ApiResponse<PuntuacionMatrixResponse>> findById(
            @PathVariable Long idCuestionario,
            @PathVariable Long matrixId) {
        return ResponseEntity.ok(ApiResponse.ok(puntuacionMatrixService.findById(matrixId)));
    }
    
    @PutMapping("/{matrixId}/actualizar")
    public ResponseEntity<ApiResponse<PuntuacionMatrixResponse>> actualizar(
            @PathVariable Long idCuestionario,
            @PathVariable Long matrixId,
            @Valid @RequestBody ActualizarPuntuacionMatrixRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
        		puntuacionMatrixService.actualizar(matrixId, request),
            "Entrada de matriz actualizada"
        ));
    }
    
    @DeleteMapping("/{matrixId}/eliminar")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long idCuestionario,
            @PathVariable Long matrixId) {
    	puntuacionMatrixService.eliminar(matrixId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Entrada de matriz eliminada"));
    }

}
