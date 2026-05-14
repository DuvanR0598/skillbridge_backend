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

import com.udea.skillbridge.dto.ActualizarPesoOpcionesRequest;
import com.udea.skillbridge.dto.Pregunta;
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
    public ResponseEntity<Pregunta> create(@Valid @RequestBody Pregunta pregunta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(preguntaService.crearPregunta(pregunta));
    }
    
    /**
     * Obtener una pregunta por ID.
     */
    @GetMapping("/buscar_pregunta_id/{id}")
    public ResponseEntity<Pregunta> getById(@PathVariable Long id) {
        return ResponseEntity.ok(preguntaService.getById(id));
    }
    
    /**
     * Listar todas las preguntas.
     * Opcional: filtrar por tipo con query param.
     * Ejemplo: GET /preguntas/listar?tipoPregunta=VERDADERO_FALSO
     */
    @GetMapping("/listar")
    public ResponseEntity<List<Pregunta>> listarTodo(@RequestParam(required = false) TipoPregunta tipoPregunta){
    	if(tipoPregunta != null) {
    		return ResponseEntity.ok(preguntaService.listarPorTipo(tipoPregunta));
    	}
    	return ResponseEntity.ok(preguntaService.listarTodo());
    }
    
    /**
     * Eliminar pregunta.
     * Solo funciona si la pregunta NO está asociada a ningún cuestionario.
     */
    @DeleteMapping("/eliminar_pregunta/{id}")
    public ResponseEntity<Void> borrarPregunta(@PathVariable Long id) {
        preguntaService.eliminarPregunta(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Actualizar SOLO los pesos de las opciones.
     * Permitido aunque el cuestionario que use esta pregunta esté COMPLETE,
     * porque los pesos son un ajuste de calibración, no un cambio estructural.
     *
     * PATCH es más semántico que PUT aquí porque solo tocamos un campo parcial.
     */
    @PatchMapping("/{id}/opcion_peso")
    public ResponseEntity<Pregunta> actualizarPesosOpciones(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPesoOpcionesRequest request) {
        return ResponseEntity.ok(preguntaService.actualizarPesosOpciones(id, request));
    }

}
