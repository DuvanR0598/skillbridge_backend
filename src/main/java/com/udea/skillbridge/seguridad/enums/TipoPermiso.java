package com.udea.skillbridge.seguridad.enums;

/**
 * Permisos granulares del sistema.
 * Cada rol agrupa un conjunto de permisos.
 *
 * Convención: MÓDULO_ACCIÓN
 */
public enum TipoPermiso {
	
	// ── Cuestionarios ───────────────────────────────────────────────
    QUESTIONNAIRE_CREATE,
    QUESTIONNAIRE_READ,
    QUESTIONNAIRE_UPDATE,
    QUESTIONNAIRE_DELETE,
    QUESTIONNAIRE_PUBLISH,
    QUESTIONNAIRE_DELIVER,   // Entregar al estudiante

    // ── Preguntas ───────────────────────────────────────────────────
    QUESTION_CREATE,
    QUESTION_READ,
    QUESTION_UPDATE,
    QUESTION_DELETE,

    // ── Condiciones ─────────────────────────────────────────────────
    CONDITION_MANAGE,

    // ── Matriz de valoración ────────────────────────────────────────
    SCORE_MATRIX_CREATE,
    SCORE_MATRIX_READ,
    SCORE_MATRIX_UPDATE,
    SCORE_MATRIX_DELETE,

    // ── Plan de fortalecimiento ─────────────────────────────────────
    PLAN_CREATE,
    PLAN_READ,
    PLAN_UPDATE,
    PLAN_DELETE,

    // ── Evaluaciones (respuestas) ───────────────────────────────────
    ASSESSMENT_START,        // Solo estudiante
    ASSESSMENT_SUBMIT,       // Solo estudiante
    ASSESSMENT_READ_OWN,     // Estudiante ve sus propias 
    ASSESSMENT_READ_ALL,     // Docente/Admin ve todas

    // ── Analytics ──────────────────────────────────────────────────
    ANALYTICS_READ_OWN,      // Estudiante ve su propio progreso
    ANALYTICS_READ_GROUP,    // Docente ve reportes de grupo

    // ── Usuarios ───────────────────────────────────────────────────
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,
    USER_MANAGE_ROLES

}
