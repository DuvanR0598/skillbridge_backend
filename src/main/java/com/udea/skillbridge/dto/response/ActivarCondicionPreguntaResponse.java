package com.udea.skillbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Le dice al frontend: "muéstrame si en la pregunta [triggerIdPregunta]
 * el usuario eligió la opción [triggerIdOpcion]".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivarCondicionPreguntaResponse {
	
	private Long idCondicion;
    private Long triggerIdPregunta;
    private Long triggerIdOpcion;

}
