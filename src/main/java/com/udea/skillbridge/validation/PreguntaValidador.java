package com.udea.skillbridge.validation;

import com.udea.skillbridge.dto.Pregunta;

/**
 * Interfaz Strategy para validar preguntas según su tipo.
 * Cada implementación conoce las reglas de UN solo tipo.
 *
 * ¿Por qué una interfaz y no un switch en el Service?
 * -> Principio Open/Closed: si mañana aparece un nuevo tipo de pregunta,
 *   solo se agrega una clase nueva. No se toca el Service ni el Factory.
 */
public interface PreguntaValidador {
	
	void validador (Pregunta pregunta);

}
