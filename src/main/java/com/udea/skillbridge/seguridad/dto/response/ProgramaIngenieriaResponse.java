package com.udea.skillbridge.seguridad.dto.response;

import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta del endpoint público /perfil/programas.
 * El frontend la usa para poblar el selector de programas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramaIngenieriaResponse {
	
    private ProgramaIngenieria valor;   // valor para el request
    private String montrarNombre;         // texto para mostrar al usuario

}
