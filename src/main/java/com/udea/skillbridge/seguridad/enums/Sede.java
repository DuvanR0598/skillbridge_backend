package com.udea.skillbridge.seguridad.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Sedes y seccionales regionales de la Universidad de Antioquia en las que se
 * ofertan programas de ingeniería. El estudiante indica a cuál pertenece.
 *
 *  - displayName: texto para mostrar en el selector del frontend.
 */
@Getter
@RequiredArgsConstructor
public enum Sede {

    SECCIONAL_ORIENTE("Seccional Oriente (Carmen de Viboral)"),
    SECCIONAL_URABA("Seccional Urabá (Turbo, Carepa, Apartadó)"),
    SECCIONAL_BAJO_CAUCA("Seccional Bajo Cauca (Caucasia)"),
    SECCIONAL_MAGDALENA_MEDIO("Seccional Magdalena Medio (Puerto Berrío)"),
    SECCIONAL_SUROESTE("Seccional Suroeste (Andes)"),
    SECCIONAL_OCCIDENTE("Seccional Occidente (Santa Fe de Antioquia)"),
    SEDE_AMALFI("Sede Amalfi"),
    SEDE_SEGOVIA("Sede Segovia"),
    SEDE_YARUMAL("Sede Yarumal"),
    SEDE_SONSON("Sede Sonsón");

    private final String displayName;
}
