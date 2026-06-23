package com.udea.skillbridge.seguridad.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Programas de ingeniería de la Facultad de Ingeniería (UdeA).
 *
 * Cada constante lleva DOS datos asociados:
 *  - codigo:      código oficial del programa (columna "Código" del listado de la Facultad).
 *  - displayName: nombre del programa para mostrar en el selector del frontend.
 */
@Getter
@RequiredArgsConstructor
public enum ProgramaIngenieria {

    INGENIERIA_DE_MATERIALES("501", "Ingeniería de Materiales"),
    INGENIERIA_DE_SISTEMAS("504", "Ingeniería de Sistemas"),
    INGENIERIA_DE_SISTEMAS_VIRTUAL_REGION("506", "Ingeniería de Sistemas Virtual Región"),
    INGENIERIA_ELECTRICA("507", "Ingeniería Eléctrica"),
    INGENIERIA_ELECTRONICA("510", "Ingeniería Electrónica"),
    INGENIERIA_INDUSTRIAL_PRESENCIAL("513", "Ingeniería Industrial Presencial"),
    INGENIERIA_INDUSTRIAL_VIRTUAL_REGION("514", "Ingeniería Industrial Virtual Región"),
    INGENIERIA_MECANICA("516", "Ingeniería Mecánica"),
    INGENIERIA_QUIMICA("522", "Ingeniería Química"),
    INGENIERIA_BIOQUIMICA_URABA("523", "Ingeniería Bioquímica Urabá"),
    INGENIERIA_BIOQUIMICA_CARMEN("524", "Ingeniería Bioquímica Carmen"),
    INGENIERIA_SANITARIA("525", "Ingeniería Sanitaria"),
    INGENIERIA_AGROINDUSTRIAL_URABA("526", "Ingeniería Agroindustrial Urabá"),
    BIOINGENIERIA("527", "Bioingeniería"),
    INGENIERIA_AGROINDUSTRIAL_CARMEN("528", "Ingeniería Agroindustrial Carmen"),
    TEC_BIOMEDICA_CARMEN("529", "Tec. Biomédica Carmen"),
    TEC_AGROINDUSTRIAL_CARMEN("530", "Tec. Agroindustrial Carmen"),
    INGENIERIA_AMBIENTAL_PRESENCIAL("531", "Ingeniería Ambiental Presencial"),
    INGENIERIA_CIVIL("533", "Ingeniería Civil"),
    INGENIERIA_DE_TELECOMUNICACIONES_VIRTUAL_REGION("536", "Ingeniería de Telecomunicaciones Virtual Región"),
    INGENIERIA_AMBIENTAL_VIRTUAL_REGION("537", "Ingeniería Ambiental Virtual Región"),
    INGENIERIA_DE_TELECOMUNICACIONES_PRESENCIAL("539", "Ingeniería de Telecomunicaciones Presencial"),
    INGENIERIA_OCEANOGRAFICA_TURBO("540", "Ingeniería Oceanográfica Turbo"),
    INGENIERIA_URBANA_CARMEN("541", "Ingeniería Urbana Carmen"),
    TEC_AGROINDUSTRIAL_URABA("543", "Tec. Agroindustrial Urabá"),
    INGENIERIA_SANITARIA_APARTADO("544", "Ingeniería Sanitaria Apartadó"),
    INGENIERIA_CIVIL_APARTADO("545", "Ingeniería Civil Apartadó"),
    INGENIERIA_AEROESPACIAL_CARMEN("546", "Ingeniería Aeroespacial Carmen"),
    INGENIERIA_ENERGETICA_CARMEN("547", "Ingeniería Energética Carmen"),
    INGENIERIA_INDUSTRIAL_VIRTUAL_MEDELLIN("549", "Ingeniería Industrial Virtual - Medellín"),
    INGENIERIA_DE_TELECOMUNICACIONES_VIRTUAL_MEDELLIN("550", "Ingeniería de Telecomunicaciones Virtual - Medellín"),
    INGENIERIA_AMBIENTAL_VIRTUAL_MEDELLIN("551", "Ingeniería Ambiental Virtual - Medellín"),
    INGENIERIA_DE_SISTEMAS_VIRTUAL_MEDELLIN("552", "Ingeniería de Sistemas Virtual - Medellín");

    private final String codigo;
    private final String displayName;

}
