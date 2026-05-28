package com.udea.skillbridge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.dto.request.EnviarRespuestaRequest;
import com.udea.skillbridge.dto.request.IniciarEvaluacionRequest;
import com.udea.skillbridge.dto.response.DetalleRespuestaResponse;
import com.udea.skillbridge.dto.response.EvaluacionEstudianteResponse;
import com.udea.skillbridge.dto.response.InformeEvaluacionResponse;
import com.udea.skillbridge.service.IEvaluacionEstudianteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/evaluacion")
@RequiredArgsConstructor
public class EvaluacionEstudianteController {
	
	private final IEvaluacionEstudianteService evaluacionService;
	
    /**
     * Inicia una sesión de evaluación.
     * POST /evaluacion/cuestionario/{idCuestionario}/iniciar
     */
    @PostMapping("/cuestionario/{idCuestionario}/iniciar")
    public ResponseEntity<ApiResponse<EvaluacionEstudianteResponse>> iniciar(
            @PathVariable Long idCuestionario,
            @Valid @RequestBody IniciarEvaluacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                		evaluacionService.iniciar(idCuestionario, request),
                    "Sesión iniciada exitosamente"
                ));
    }
    
    /**
     * Envía la respuesta a una pregunta.
     */
    @PostMapping("/{idEvaluacion}/respuestas")
    public ResponseEntity<ApiResponse<DetalleRespuestaResponse>> enviarRespuesta(
            @PathVariable Long idEvaluacion,
            @Valid @RequestBody EnviarRespuestaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                		evaluacionService.enviarRespuesta(idEvaluacion, request),
                    "Respuesta guardada"
                ));
    }
    
    /**
     * Actualiza una respuesta ya enviada (solo si la sesión está EN_PROGRESO).
     */
    @PutMapping("/{idEvaluacion}/respuesta/{idPregunta}")
    public ResponseEntity<ApiResponse<DetalleRespuestaResponse>> actualizarRespuesta(
            @PathVariable Long idEvaluacion,
            @PathVariable Long idPregunta,
            @Valid @RequestBody EnviarRespuestaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
        		evaluacionService.actualizarRespuesta(idEvaluacion, idPregunta, request),
            "Respuesta actualizada"
        ));
    }
    
    /**
     * Completa la sesión, calcula scores y retorna el reporte.
     */
    @PatchMapping("/{idEvaluacion}/completo")
    public ResponseEntity<ApiResponse<InformeEvaluacionResponse>> completo(
            @PathVariable Long idEvaluacion) {
        return ResponseEntity.ok(ApiResponse.ok(
        		evaluacionService.completo(idEvaluacion),
            "Evaluación completada. Reporte generado."
        ));
    }
    
    /**
     * Consulta el estado actual de una sesión.
     */
    @GetMapping("/consultar-id/{idEvaluacion}")
    public ResponseEntity<ApiResponse<EvaluacionEstudianteResponse>> findById(
            @PathVariable Long idEvaluacion) {
        return ResponseEntity.ok(ApiResponse.ok(
        		evaluacionService.findById(idEvaluacion)
        ));
    }
    
    /**
     * Historial de sesiones de un estudiante en un cuestionario.
     */
    @GetMapping("/estudiante/{idEstudiante}/cuestionario/{idCuestionario}")
    public ResponseEntity<ApiResponse<List<EvaluacionEstudianteResponse>>> findByEstudiante(
            @PathVariable Long idEstudiante,
            @PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(
        		evaluacionService.findByEstudiante(idEstudiante, idCuestionario)
        ));
    }
    
    /**
     * Obtiene el reporte completo de una sesión COMPLETADA.
     */
    @GetMapping("/{idEvaluacion}/reporte")
    public ResponseEntity<ApiResponse<InformeEvaluacionResponse>> getReporte(
            @PathVariable Long idEvaluacion) {
        return ResponseEntity.ok(ApiResponse.ok(
        		evaluacionService.getReporte(idEvaluacion)
        ));
    }

}
