package com.udea.skillbridge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.dto.request.CondicionPreguntaRequest;
import com.udea.skillbridge.dto.response.CondicionPreguntaResponse;
import com.udea.skillbridge.service.ICondicionPreguntaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/condicion_pregunta/{idCuestionario}")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CondicionPreguntaController {
	
	private final ICondicionPreguntaService condicionPreguntaService;
	
	@PostMapping("/crear_condicion")
    public ResponseEntity<ApiResponse<CondicionPreguntaResponse>> crearCondicion(
            @PathVariable Long idCuestionario,
            @Valid @RequestBody CondicionPreguntaRequest request) {
		CondicionPreguntaResponse response = condicionPreguntaService.crearCondicion(idCuestionario, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Condición creada exitosamente"));
    }
	
	@GetMapping("/listar_condicion")
    public ResponseEntity<ApiResponse<List<CondicionPreguntaResponse>>> listarCondiciones(
            @PathVariable Long idCuestionario) {
		List<CondicionPreguntaResponse> condiciones = condicionPreguntaService.listarCondiciones(idCuestionario);
        return ResponseEntity.ok(ApiResponse.ok(condiciones, "Condiciones listadas exitosamente"));
    }
	
	@DeleteMapping("/{idCondicion}")
    public ResponseEntity<ApiResponse<Void>> eliminarCondicion(
            @PathVariable Long idCuestionario,
            @PathVariable Long idCondicion) {
		condicionPreguntaService.eliminarCondicion(idCuestionario, idCondicion);
        return ResponseEntity.ok(ApiResponse.ok(null, "Condición eliminada exitosamente"));
    }

}
