package com.udea.skillbridge.seguridad.enums;

/**
 * Roles del sistema. Se almacenan en BD con prefijo ROLE_
 * para cumplir la convención de Spring Security.
 */
public enum TipoRol {
	
    ROLE_ADMIN,        // Administrador — control total
    ROLE_COORDINADOR,  // Coordinador — gestión de cuestionarios y reportes
    ROLE_ESTUDIANTE    // Estudiante — responder evaluaciones y ver su progreso

}
