package com.udea.skillbridge.dto.response;

import java.util.List;

import com.udea.skillbridge.enums.TipoPregunta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pregunta tal como se ve DENTRO de un cuestionario, para el panel del
 * coordinador. Combina los datos de la pregunta con los de la relación
 * (peso, obligatoria, condicional) e incluye las opciones con su detalle admin.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaDeCuestionarioResponse {

    private Long idPregunta;
    private String texto;
    private TipoPregunta tipoPregunta;
    private String imagenUrl;
    private String ayuda;
    private Integer maxOpciones;

    // Datos de la relación pregunta-cuestionario
    private Boolean obligatoria;
    private Integer peso;
    private Boolean isCondicional;

    private List<OpcionPreguntaAdminResponse> opciones;
}
