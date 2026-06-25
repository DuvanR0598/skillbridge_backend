package com.udea.skillbridge.validation.impl;

import org.springframework.stereotype.Component;

import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.validation.PreguntaValidador;

@Component
public class OpcionUnicaValidador implements PreguntaValidador {

	@Override
	public void validador(PreguntaRequest preguntaRequest) {
		var opciones = preguntaRequest.getOpcionPreguntaRequest();
		
		// REGLA ÚNICA: Mínimo 2 opciones (si solo hay 1 no tiene sentido)
        // En soft skills no hay opción "correcta": cada opción aporta su peso.
        if (opciones == null || opciones.size() < 2) {
            throw new CuestionarioException(
                "Una pregunta de SELECCIÓN ÚNICA debe tener al menos 2 opciones. " +
                "Se recibieron: " + (opciones == null ? 0 : opciones.size())
            );
        }

	}
	

}
