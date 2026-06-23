package com.udea.skillbridge.seguridad.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipo de documento de identificación del usuario.
 * El campo 'displayName' se usa para mostrarlo en el frontend.
 */
@Getter
@RequiredArgsConstructor
public enum TipoIdentificacion {

    CC("Cédula de Ciudadanía"),
    TI("Tarjeta de Identidad"),
    CE("Cédula de Extranjería"),
    PA("Pasaporte");

    private final String displayName;

}
