package com.udea.skillbridge.dto.response;

import java.util.List;

import com.udea.skillbridge.enums.TipoPregunta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pregunta tal como la ve el estudiante.
 * NO incluye campos internos como peso, isCorrect, etc.
 * El estudiante no debe saber cuál opción es la correcta.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaEntregaResponse {
	
	private Long idPregunta;
	private Integer numeroPregunta;       // Posición en esta entrega (1, 2, 3...)
    private TipoPregunta tipoPregunta;
    private String texto;
    private String imagenUrl;
    private String descripcion;
    private String ayuda;
    private Boolean obligatoria;
    private Integer maxOpciones;
    private List<OpcionPreguntaResponse> opciones;
    /**
     * Condiciones de activación de esta pregunta.
     * Vacío → siempre visible.
     * Con datos → visible solo si se cumple la condición.
     */
    private List<ActivarCondicionPreguntaResponse> activarCondiciones;

}
