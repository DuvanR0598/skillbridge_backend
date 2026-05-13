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
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.dto.Cuestionario;
import com.udea.skillbridge.dto.CuestionarioEntregaResponse;
import com.udea.skillbridge.dto.PreguntaCuestionario;
import com.udea.skillbridge.service.ICuestionarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cuestionario")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CuestionarioController {

	private final ICuestionarioService cuestionarioService;
	
	// Crear un cuestionario
	@PostMapping("/crear_cuestionario")
	public ResponseEntity<Cuestionario> crearCuestionario (@Valid @RequestBody Cuestionario cuestionario) { 
		return ResponseEntity.status(HttpStatus.CREATED).body(cuestionarioService.crearCuestionario(cuestionario));
	}
	
	// Añadir preguntas al cuestionario
	@PostMapping("/{idCuestionario}/pregunta")
    public ResponseEntity<Void> addPreguntaToCuestionario(
            @PathVariable Long idCuestionario,
            @Valid @RequestBody PreguntaCuestionario pq) {
        cuestionarioService.addPretuntaToCuestinario(idCuestionario, pq);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
	
	// Obtener cuestionario por ID
	@GetMapping("/buscar_cuestionario_id/{idCuestionario}")
	public ResponseEntity<Cuestionario> getById(@PathVariable Long idCuestionario) {
		return ResponseEntity.ok().body(cuestionarioService.getById(idCuestionario));
	}
	
	// Listar todos los cuestionarios
	@GetMapping("/listar_cuestionarios")
    public ResponseEntity<List<Cuestionario>> listarAllCuestionarios() {
        return ResponseEntity.ok(cuestionarioService.listarAllCuestionarios());
    }
	
	//Listar cuestionarios activos
	@GetMapping("/listar_cuestionarios_activos")
    public ResponseEntity<List<Cuestionario>> listaCuestionariosActivos() {
        return ResponseEntity.ok(cuestionarioService.listarCuestionariosActivos());
    }
	
	// Marcar un cuestionario como COMPLETO
	@PatchMapping("/{id}/completo")
    public ResponseEntity<Cuestionario> cuestionarioCompleto(@PathVariable Long id) {
        return ResponseEntity.ok(cuestionarioService.cuestionarioCompleto(id));
    }
	
	// Marcar un cuestionario como PUBLICADO
	@PatchMapping("/{id}/publicar")
	public ResponseEntity<Cuestionario> cuestionarioPublicado(@PathVariable Long id) {
	    return ResponseEntity.ok(cuestionarioService.cuestionarioPublicado(id));
	}
	
	// Marcar un cuestionario como ARCHIVADO
	@PatchMapping("/{id}/archivar")
	public ResponseEntity<Cuestionario> cuestionarioArchivado(@PathVariable Long id) {
	    return ResponseEntity.ok(cuestionarioService.cuestionarioArchivado(id));
	}
	
	// Borrado Logico
	@DeleteMapping("/borrado_logico/{idCuestionario}")
    public ResponseEntity<Void> borradoLogico(@PathVariable Long idCuestionario) {
        cuestionarioService.borradoLogico(idCuestionario);
        return ResponseEntity.noContent().build();
    }
	
	/**
     * Endpoint que consume el estudiante para obtener el cuestionario
     * listo para responder. Cada llamada puede producir un orden diferente
     * si randomOrder = true.
     *
     */
    @GetMapping("/{id}/entregar_cuestionario")
    public ResponseEntity<CuestionarioEntregaResponse> entregarCuestionario(@PathVariable Long id) {
        return ResponseEntity.ok(cuestionarioService.entregarCuestionario(id));
    }

}
