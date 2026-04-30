package com.udea.skillbridge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.dto.Cuestionario;
import com.udea.skillbridge.service.ICuestionarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cuestionario")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CuestionarioController {

	private final ICuestionarioService cuestionarioService;
	
	@PostMapping("/crear_cuestionario")
	public ResponseEntity<Cuestionario> crearCuestionario (@Valid @RequestBody Cuestionario cuestionario) {
		return ResponseEntity.status(HttpStatus.CREATED).body(cuestionarioService.crearCuestionario(cuestionario));
	}
	
	@GetMapping("/buscar_cuestionario_id/{idCuestionario}")
	public ResponseEntity<Cuestionario> getById(@PathVariable Long idCuestionario) {
		return ResponseEntity.ok().body(cuestionarioService.getById(idCuestionario));
	}
	
	@GetMapping("/listar_cuestionarios")
    public ResponseEntity<List<Cuestionario>> listarAllCuestionarios() {
        return ResponseEntity.ok(cuestionarioService.listarAllCuestionarios());
    }
	
	@GetMapping("/listar_cuestionarios_activos")
    public ResponseEntity<List<Cuestionario>> listaCuestionariosActivos() {
        return ResponseEntity.ok(cuestionarioService.listarCuestionariosActivos());
    }

}
