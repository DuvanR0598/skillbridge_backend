package com.udea.skillbridge.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lo que recibe el estudiante cuando va a responder un cuestionario.
 * Las preguntas ya vienen en el orden que corresponde (aleatorio o fijo).
 *
 * IMPORTANTE: para preguntas de tipo SINGLE_CHOICE, MULTIPLE_CHOICE
 * y TRUE_FALSE, las opciones también se entregan en orden aleatorio
 * si ordenAleatorio = true. Evita el sesgo de "siempre elijo la primera".
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuestionarioEntregaResponse {
	
	private Long idCuestinario;
	private String nombre;
	private String objetivo;
	private Boolean ordenAleatorio;
	private Integer totalPreguntas;
	private List<PreguntaEntregaResponse> preguntas;
	private List<PreguntaEntregaResponse> preguntasCondicionales;

}
