package com.udea.skillbridge.enums;

/**
	 * Estados del ciclo de vida de un cuestionario.
	 *
	 * BORRADOR   → En construcción, se pueden agregar/quitar preguntas.
	 * COMPLETO   → Tiene mínimo 2 preguntas y está listo para publicar. Ya NO se pueden agregar/quitar preguntas.
	 * PUBLICADO  → Activo para ser respondido por usuarios/estudiantes.
	 * ARCHIVADO  → Fuera de uso pero conservado por trazabilidad estadística.
	 * ELIMINADO  → Borrado lógico (isDeleted = true). Solo aplica si NO tiene respuestas.
 */

public enum EstadoCuestionario {
	
	BORRADOR,
	COMPLETO,
	PUBLICADO,
	ARCHIVADO,
	ELIMINADO
}
