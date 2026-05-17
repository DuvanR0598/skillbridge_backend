package com.udea.skillbridge.validation.impl;

import org.springframework.stereotype.Component;

import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.validation.PreguntaValidador;

@Component
public class VerdaderoFalsoValidador implements PreguntaValidador {

	@Override
	public void validador(PreguntaRequest preguntaRequest) {
		var opcion = preguntaRequest.getOpcionPreguntaRequest();
		
		// REGLA 1: Debe tener exactamente 2 opciones (Verdadero y Falso)
        if (opcion == null || opcion.size() != 2) {
            throw new CuestionarioException(
                "Una pregunta de tipo VERDADERO/FALSO debe tener exactamente 2 opciones."
            );
        }
		
        // REGLA 2: Exactamente 1 opción debe estar marcada como correcta
        long correctoCount = opcion.stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsCorrecta()))
                .count();

        if (correctoCount != 1) {
            throw new CuestionarioException(
                "Una pregunta VERDADERO/FALSO debe tener exactamente 1 opción correcta. " +
                "Se encontraron: " + correctoCount
            );
        }
	}

}
