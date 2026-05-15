package com.udea.skillbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * El docente define: "En el cuestionario X,
 * si la pregunta Y recibe la opción Z → mostrar pregunta W"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearCondicionPreguntaRequest {
	
	@NotNull(message = "La pregunta disparadora es obligatoria")
    private Long triggerIdPregunta;

    @NotNull(message = "La opción disparadora es obligatoria")
    private Long triggerIdOpcion;

    @NotNull(message = "La pregunta destino es obligatoria")
    private Long targetIdPreguta;

}
