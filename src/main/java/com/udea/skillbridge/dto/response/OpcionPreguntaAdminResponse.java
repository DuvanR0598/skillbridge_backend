package com.udea.skillbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Opción tal como la ve un ADMIN/COORDINADOR.
 * A diferencia de {@link OpcionPreguntaResponse} (vista del estudiante),
 * SÍ incluye isCorrecta y peso, necesarios para gestionar el banco.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionPreguntaAdminResponse {

    private Long idOpcion;
    private String texto;
    private Boolean isCorrecta;
    private Integer peso;
    private Integer ordenVisualizacion;
}
