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
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.dto.request.ActualizarPlanFortalecimientoRequest;
import com.udea.skillbridge.dto.request.PlanFortalecimientoRequest;
import com.udea.skillbridge.dto.response.PlanFortalecimientoResponse;
import com.udea.skillbridge.enums.PlanAxis;
import com.udea.skillbridge.service.IPlanFortalecimientoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/puntuacion-matrix/{idMatrix}/plan-fortalecimiento")
@RequiredArgsConstructor
public class PlanFortalecimientoController {
	
	private final IPlanFortalecimientoService planService;
	
    @PostMapping("/crear")
    public ResponseEntity<ApiResponse<PlanFortalecimientoResponse>> crear(
            @PathVariable Long idMatrix,
            @Valid @RequestBody PlanFortalecimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                    planService.crear(idMatrix, request),
                    "Plan de fortalecimiento creado"
                ));
    }
    
    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<PlanFortalecimientoResponse>>> findAll(
            @PathVariable Long idMatrix) {
        return ResponseEntity.ok(ApiResponse.ok(planService.findByMatrix(idMatrix)));
    }
    
    @GetMapping("/axis/{axis}")
    public ResponseEntity<ApiResponse<PlanFortalecimientoResponse>> findByAxis(
            @PathVariable Long idMatrix,
            @PathVariable PlanAxis axis) {
        return ResponseEntity.ok(
            ApiResponse.ok(planService.findByMatrixAndAxis(idMatrix, axis))
        );
    }
    
    @GetMapping("/listar-id/{idPlan}")
    public ResponseEntity<ApiResponse<PlanFortalecimientoResponse>> findById(
            @PathVariable Long idMatrix,
            @PathVariable Long idPlan) {
        return ResponseEntity.ok(ApiResponse.ok(planService.findById(idPlan)));
    }
    
    @PutMapping("/actualizar/{idPlan}")
    public ResponseEntity<ApiResponse<PlanFortalecimientoResponse>> actualizar(
            @PathVariable Long idMatrix,
            @PathVariable Long idPlan,
            @Valid @RequestBody ActualizarPlanFortalecimientoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
            planService.actualizar(idPlan, request),
            "Plan actualizado"
        ));
    }
    
    @DeleteMapping("/eliminar/{idPlan}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long idMatrix,
            @PathVariable Long idPlan) {
        planService.eliminar(idPlan);
        return ResponseEntity.ok(ApiResponse.ok(null, "Plan eliminado"));
    }

}
