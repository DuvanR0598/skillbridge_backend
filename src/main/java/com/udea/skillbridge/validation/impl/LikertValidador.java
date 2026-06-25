package com.udea.skillbridge.validation.impl;

import org.springframework.stereotype.Component;

import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.validation.PreguntaValidador;

@Component
public class LikertValidador implements PreguntaValidador {
	
	private static final int MIN_OPCIONES = 2;
    private static final int MAX_OPCIONES = 5;

	@Override
	public void validador(PreguntaRequest preguntaRequest) {
		var opciones = preguntaRequest.getOpcionPreguntaRequest();
		
		// REGLA 1: Entre 2 y 5 opciones (escala Likert estándar)
        if (opciones == null || opciones.size() < MIN_OPCIONES || opciones.size() > MAX_OPCIONES) {
            throw new CuestionarioException(
                "Una pregunta LIKERT debe tener entre " + MIN_OPCIONES +
                " y " + MAX_OPCIONES + " opciones. Se recibieron: " +
                (opciones == null ? 0 : opciones.size())
            );
        }
		
	}

}
