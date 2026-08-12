package com.udea.skillbridge.seguridad.dto.response;

import com.udea.skillbridge.seguridad.enums.Sede;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta del endpoint público /perfil/sedes.
 * El frontend la usa para poblar el selector de sede.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SedeResponse {

    private Sede value;         // valor (enum) para el request
    private String displayName; // texto para mostrar al usuario

}
