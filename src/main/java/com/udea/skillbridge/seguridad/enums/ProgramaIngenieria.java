package com.udea.skillbridge.seguridad.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Programas de ingeniería soportados.
 * El campo 'displayName' se retorna en el response para
 * mostrarlo en el selector del frontend.
 */
@Getter
@RequiredArgsConstructor
public enum ProgramaIngenieria {
	
    SYSTEMS_ENGINEERING("Ingeniería de Sistemas"),
    INDUSTRIAL_ENGINEERING("Ingeniería Industrial"),
    CIVIL_ENGINEERING("Ingeniería Civil"),
    ELECTRONIC_ENGINEERING("Ingeniería Electrónica"),
    ELECTRICAL_ENGINEERING("Ingeniería Eléctrica"),
    MECHANICAL_ENGINEERING("Ingeniería Mecánica"),
    BIOMEDICAL_ENGINEERING("Ingeniería Biomédica"),
    ENVIRONMENTAL_ENGINEERING("Ingeniería Ambiental"),
    CHEMICAL_ENGINEERING("Ingeniería Química"),
    AEROSPACE_ENGINEERING("Ingeniería Aeroespacial"),
    TELECOMMUNICATIONS_ENGINEERING("Ingeniería de Telecomunicaciones"),
    OTHER("Otro programa de ingeniería");

    private final String displayName;

}
