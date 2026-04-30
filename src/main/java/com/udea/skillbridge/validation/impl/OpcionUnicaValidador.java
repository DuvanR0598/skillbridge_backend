package com.udea.skillbridge.validation.impl;

import org.springframework.stereotype.Component;

import com.udea.skillbridge.dto.Pregunta;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.validation.PreguntaValidador;

@Component
public class OpcionUnicaValidador implements PreguntaValidador {

	@Override
	public void validador(Pregunta pregunta) {
		var opciones = pregunta.getOpcionPregunta();
		
		// REGLA 1: Mínimo 2 opciones (si solo hay 1 no tiene sentido)
        if (opciones == null || opciones.size() < 2) {
            throw new CuestionarioException(
                "Una pregunta de SELECCIÓN ÚNICA debe tener al menos 2 opciones. " +
                "Se recibieron: " + (opciones == null ? 0 : opciones.size())
            );
        }
        
        // REGLA 2: EXACTAMENTE 1 opción correcta
        // Diferencia con OPCION MULTIPLE
        long correctaCount = opciones.stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsCorrecta()))
                .count();

        if (correctaCount != 1) {
            throw new CuestionarioException(
                "Una pregunta de SELECCIÓN ÚNICA debe tener exactamente 1 opción correcta. " +
                "Se encontraron: " + correctaCount
            );
        }
		
	}
	

}
