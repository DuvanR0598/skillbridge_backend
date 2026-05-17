package com.udea.skillbridge.validation.impl;

import org.springframework.stereotype.Component;

import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.validation.PreguntaValidador;

@Component
public class DescripcionValidador implements PreguntaValidador {

	@Override
	public void validador(PreguntaRequest preguntaRequest) {
		var opciones = preguntaRequest.getOpcionPreguntaRequest();
		
		// REGLA: Las preguntas de descripción son abiertas, no tienen opciones.
        // Si el usuario envía opciones de todas formas, lo rechazamos con un mensaje claro.
        if (opciones != null && !opciones.isEmpty()) {
            throw new CuestionarioException(
                "Las preguntas de tipo DESCRIPCIÓN son respuesta abierta y no admiten " +
                "opciones de respuesta. Recibidas: " + opciones.size()
            );
        }
        // Si opciones es null o vacío, todo bien, no hay nada que validar.
		
	}

}
