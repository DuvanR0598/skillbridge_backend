package com.udea.skillbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Opción tal como la ve un ADMIN/COORDINADOR.
 * A diferencia de {@link OpcionPreguntaResponse} (vista del estudiante),
 * SÍ incluye el peso, necesario para gestionar el banco.
 * Las soft skills se miden por peso; no existe opción correcta/incorrecta.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionPreguntaAdminResponse {

    private Long idOpcion;
    private String texto;
    private Integer peso;
    private Integer ordenVisualizacion;
}
