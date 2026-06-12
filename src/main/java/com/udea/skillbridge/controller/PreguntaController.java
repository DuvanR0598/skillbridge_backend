package com.udea.skillbridge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.dto.request.ActualizarPesoOpcionesRequest;
import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.dto.response.PreguntaResponse;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.service.IPreguntaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/preguntas")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PreguntaController {
	
	private final IPreguntaService preguntaService;
	
	/**
     * Crear una pregunta nueva.
     * Las validaciones de opciones según tipo se ejecutan en el Service.
     */
    @PostMapping("/crear_pregunta")
    public ResponseEntity<ApiResponse<PreguntaResponse>> crearPregunta
    (@Valid @RequestBody PreguntaRequest preguntaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
        		preguntaService.crearPregunta(preguntaRequest),
        		"Pregunta creada exitosamente"));
    }
    
    /**
     * Obtener una pregunta por ID.
     */
    @GetMapping("/buscar_pregunta_id/{id}")
    public ResponseEntity<ApiResponse<PreguntaResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(preguntaService.findById(id)));
    }
    
    /**
     * Listar todas las preguntas.
     * Opcional: filtrar por tipo con query param.
     * Ejemplo: GET /preguntas/listar?tipoPregunta=VERDADERO_FALSO
     */
    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<PreguntaResponse>>> listarTodo(
    		@RequestParam(required = false) TipoPregunta tipoPregunta){
    	List<PreguntaResponse> resultado = (tipoPregunta != null)
                ? preguntaService.listarPorTipo(tipoPregunta)
                : preguntaService.listarTodo();
    	
    	return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
    
    /**
     * Actualizar SOLO los pesos de las opciones.
     * Permitido aunque el cuestionario que use esta pregunta esté COMPLETE,
     * porque los pesos son un ajuste de calibración, no un cambio estructural.
     *
     * PATCH es más semántico que PUT aquí porque solo tocamos un campo parcial.
     */
    @PatchMapping("/{id}/opcion_peso")
    public ResponseEntity<ApiResponse<PreguntaResponse>> actualizarPesosOpciones(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPesoOpcionesRequest request) {
    	return ResponseEntity.ok(ApiResponse.ok(preguntaService.actualizarPesosOpciones(id, request),
                "Pesos actualizados"
            ));
    }
    
    /**
     * Asignar o cambiar la dimensión de una pregunta.
     * ?idDimension=5  → asigna; omitir el parámetro → desasigna (null).
     */
    @PatchMapping("/{id}/dimension")
    public ResponseEntity<ApiResponse<PreguntaResponse>> asignarDimension(
            @PathVariable Long id,
            @RequestParam(required = false) Long idDimension) {
        return ResponseEntity.ok(ApiResponse.ok(
                preguntaService.asignarDimension(id, idDimension),
                "Dimensión de la pregunta actualizada"));
    }

    /**
     * Eliminar pregunta.
     * Solo funciona si la pregunta NO está asociada a ningún cuestionario.
     */
    @DeleteMapping("/eliminar_pregunta/{id}")
    public ResponseEntity<ApiResponse<Void>> borrarPregunta(@PathVariable Long id) {
        preguntaService.eliminarPregunta(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Pregunta eliminada"));
    }

}
