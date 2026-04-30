package com.udea.skillbridge.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.dto.Pregunta;
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

}
