package com.udea.skillbridge.enums;

/**
	 * Tipos de pregunta soportados por el sistema.
	 * Cada tipo define cómo se muestran las opciones y cómo se evalúan.
 */

public enum TipoPregunta {
	
	VERDADERO_FALSO,   // Solo 2 opciones: Verdadero / Falso
	LIKERT,           // Escala de acuerdo (ej: 1-5)
	DESCRIPCION,      // Respuesta abierta, sin opciones
	OPCION_MULTIPLE,  // Varias opciones, varias respuestas correctas
	OPCION_UNICA      // Varias opciones, UNA sola respuesta correcta
}
