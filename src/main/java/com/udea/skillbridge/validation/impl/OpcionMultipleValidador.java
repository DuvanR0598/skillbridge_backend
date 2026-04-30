package com.udea.skillbridge.validation.impl;

import org.springframework.stereotype.Component;

import com.udea.skillbridge.dto.Pregunta;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.validation.PreguntaValidador;

@Component
public class OpcionMultipleValidador implements PreguntaValidador {

	@Override
	public void validador(Pregunta pregunta) {
		var opciones = pregunta.getOpcionPregunta();
		
		// REGLA 1: Mínimo 2 opciones
        if (opciones == null || opciones.size() < 2) {
            throw new CuestionarioException(
                "Una pregunta de MÚLTIPLE OPCIÓN debe tener al menos 2 opciones. " +
                "Se recibieron: " + (opciones == null ? 0 : opciones.size())
            );
        }
        
        // REGLA 2: Al menos 1 opción debe ser correcta
        // (puede haber varias correctas, eso es lo que distingue MULTIPLE de UNICA)
        long correctaCount = opciones.stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsCorrecta()))
                .count();

        if (correctaCount < 1) {
            throw new CuestionarioException(
                "Una pregunta de MÚLTIPLE OPCIÓN debe tener al menos 1 opción correcta."
            );
        }
        
        // No todas pueden ser correctas
        if (correctaCount == opciones.size()) {
            throw new CuestionarioException(
                "No es válido que todas las opciones sean correctas en MÚLTIPLE OPCIÓN. " +
                "Debe haber al menos una opción incorrecta."
            );
        }
		
	}

}
