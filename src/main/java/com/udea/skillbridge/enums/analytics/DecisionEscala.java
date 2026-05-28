package com.udea.skillbridge.enums.analytics;

/**
 * Decisión de escalamiento al finalizar la Fase 4 (Post-test).
 * Según el modelo pedagógico:
 *   CERTIFICAR       → Logró nivel AVANZADO en todas las dimensiones → emitir certificación
 *   REINICIAR        → No logró AVANZADO → volver a Fase 2 con plan ajustado
 *   PENDIENTE        → No ha completado el POST_TEST aún
 */
public enum DecisionEscala {
	
	CERTIFICAR,
	REINICIAR,
	PENDIENTE

}
