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
		
		// REGLA ÚNICA: Debe tener exactamente 2 opciones (Verdadero y Falso)
        // En soft skills no hay opción "correcta": cada opción aporta su peso.
        if (opcion == null || opcion.size() != 2) {
            throw new CuestionarioException(
                "Una pregunta de tipo VERDADERO/FALSO debe tener exactamente 2 opciones."
            );
        }
	}

}
