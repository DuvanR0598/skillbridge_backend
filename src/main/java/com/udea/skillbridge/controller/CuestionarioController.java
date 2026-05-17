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

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.dto.request.ActualizarCuestionarioRequest;
import com.udea.skillbridge.dto.request.CuestionarioRequest;
import com.udea.skillbridge.dto.request.PreguntaCuestionarioRequest;
import com.udea.skillbridge.dto.response.CuestionarioEntregaResponse;
import com.udea.skillbridge.dto.response.CuestionarioResponse;
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
	public ResponseEntity<ApiResponse<CuestionarioResponse>> crearCuestionario (
			@Valid @RequestBody CuestionarioRequest cuestionarioRequest) { 
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(
						cuestionarioService.crearCuestionario(cuestionarioRequest),
						"Cuestionario creado exitosamente"
				));
	}
	
	// Listar todos los cuestionarios 
	@GetMapping("/listar_cuestionarios")
    public ResponseEntity<ApiResponse<List<CuestionarioResponse>>> listarAllCuestionarios() {
        return ResponseEntity.ok(ApiResponse.ok(cuestionarioService.listarAllCuestionarios()));
    }
	
	// Obtener cuestionario por ID
	@GetMapping("/buscar_cuestionario_id/{idCuestionario}")
	public ResponseEntity<ApiResponse<CuestionarioResponse>> findById(@PathVariable Long idCuestionario) {
		return ResponseEntity.ok(ApiResponse.ok(cuestionarioService.findById(idCuestionario)));
	}
	
	//Listar cuestionarios activos
	@GetMapping("/listar_cuestionarios_activos")
    public ResponseEntity<ApiResponse<List<CuestionarioResponse>>> listaCuestionariosActivos() {
        return ResponseEntity.ok(ApiResponse.ok(cuestionarioService.listarCuestionariosActivos()));
    }
	
	// Actualizar cuestionario, solo permitido en estado BORRADOR.
    @PatchMapping("/{id}/actualizar")
    public ResponseEntity<ApiResponse<CuestionarioResponse>> actualizarCuestionario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarCuestionarioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cuestionarioService.actualizarCuestionario(id, request),
        		"Configuracion actualizada"));
    }
	
	// Borrado Logico
	@DeleteMapping("/borrado_logico/{idCuestionario}")
    public ResponseEntity<ApiResponse<Void>> borradoLogico(@PathVariable Long idCuestionario) {
        cuestionarioService.borradoLogico(idCuestionario);
        return ResponseEntity.ok(ApiResponse.ok(null, "Cuestionario eliminado"));
    }
	
	// Marcar un cuestionario como COMPLETO
	@PatchMapping("/{id}/completo")
    public ResponseEntity<ApiResponse<CuestionarioResponse>> cuestionarioCompleto(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(cuestionarioService.cuestionarioCompleto(id),
        		"Cuestionario completado"));
    }
	
	// Marcar un cuestionario como PUBLICADO
	@PatchMapping("/{id}/publicar")
	public ResponseEntity<ApiResponse<CuestionarioResponse>> cuestionarioPublicado(@PathVariable Long id) {
	    return ResponseEntity.ok(ApiResponse.ok(cuestionarioService.cuestionarioPublicado(id),
	    		"Cuestionario publicado"));
	}
	
	// Marcar un cuestionario como ARCHIVADO
	@PatchMapping("/{id}/archivar")
	public ResponseEntity<ApiResponse<CuestionarioResponse>> cuestionarioArchivado(@PathVariable Long id) {
	    return ResponseEntity.ok(ApiResponse.ok(cuestionarioService.cuestionarioArchivado(id),
	    		"Cuestionario archivado"));
	}
	
	// Añadir preguntas al cuestionario
	@PostMapping("/{idCuestionario}/pregunta")
    public ResponseEntity<ApiResponse<Void>> addPreguntaToCuestionario(
            @PathVariable Long idCuestionario,
            @Valid @RequestBody PreguntaCuestionarioRequest pq) {
        cuestionarioService.addPretuntaToCuestinario(idCuestionario, pq);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
        		null, "Pregunta " + pq.getIdpregunta() + " agregada al cuestionario " + idCuestionario ));
    }
	
	/**
     * Endpoint que consume el estudiante para obtener el cuestionario
     * listo para responder. Cada llamada puede producir un orden diferente
     * si randomOrder = true.
     *
     */
    @GetMapping("/{id}/entregar_cuestionario")
    public ResponseEntity<ApiResponse<CuestionarioEntregaResponse>> entregarCuestionario(
    		@PathVariable Long id) {
    	CuestionarioEntregaResponse response = cuestionarioService.entregarCuestionario(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cuestionario listo para responder"));
    }

}
