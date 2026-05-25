package com.udea.skillbridge.enums;

/**
 * Dimensiones específicas de cada Power Skill según el modelo pedagógico.
 *
 * PENSAMIENTO CRÍTICO 
 *   INTERPRETACION    → Comprende y expresa el significado de experiencias o datos
 *   INFERENCIA        → Identifica elementos para sacar conclusiones razonables
 *   ANALISIS          → Identifica relaciones entre afirmaciones y preguntas
 *   EVALUACION        → Valora la credibilidad de afirmaciones y argumentos
 *   EXPLICACION       → Enuncia y justifica razonamientos propios
 *   AUTORREGULACION   → Monitorea y corrige el propio razonamiento
 *
 * ADAPTABILIDAD 
 *   CHANGE_MANAGEMENT      → Gestión del cambio ante nuevas situaciones
 *   UNCERTAINTY_MANAGEMENT → Gestión de la incertidumbre y toma de decisiones sin info completa
 *
 * NULL en la entidad = evaluación global del skill sin desglose por dimensión
 */
public enum SkillDimension {
	
    // --- Pensamiento Crítico ---
    INTERPRETACION,
    INFERENCIA,
    ANALISIS,
    EVALUACION,
    EXPLICACION,
    AUTORREGULACION,

    // --- Adaptabilidad ---
    GESTION_DEL_CAMBIO,
    GESTION_DE_INCERTIDUMBRE

}
