package com.udea.skillbridge.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnviarRespuestaRequest {
	
    @NotNull(message = "El ID de la pregunta es obligatorio")
    private Long idPregunta;

    /**
     * IDs de las opciones seleccionadas.
     * Obligatorio para: FALSO_VERDADERO, OPCION_UNICA, OPCION_MULTIPLE, LIKERT.
     * Debe estar vacío o null para: DESCRIPTION.
     */
    @Size(max = 10, message = "Máximo 10 opciones seleccionadas")
    private List<Long> idsOpcionesSeleccionadas;

    /**
     * Texto libre. Solo para preguntas DESCRIPTION.
     * Null para todos los demás tipos.
     */
    private String respuestaAbierta;

}
