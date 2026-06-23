package com.udea.skillbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Opción tal como la ve el estudiante.
 * SIN peso — ese es un dato de evaluación, no de presentación.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionPreguntaResponse {
	
	private Long idOpcion;
    private String texto;
    private Integer ordenVisualizacion;  // El orden en esta entrega específica

}
