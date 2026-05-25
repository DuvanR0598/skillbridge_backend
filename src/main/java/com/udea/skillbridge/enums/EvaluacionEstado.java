package com.udea.skillbridge.enums;

/**
 * Estado de la sesión del estudiante.
 *
 * EN_PROGRESO  → El estudiante inició pero no ha completado
 * COMPLETADO   → Completó, el puntaje fue calculado
 * ABANDONADO   → No completó en el período de aplicación
 */
public enum EvaluacionEstado {
	
	EN_PROGRESO,
	COMPLETADO,
	ABANDONADO

}
