package com.udea.skillbridge.dto.request;

import java.util.List;

import com.udea.skillbridge.enums.TipoPregunta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PreguntaRequest {

	@NotNull(message = "El tipo de pregunta es obligatorio")
    private TipoPregunta tipoPregunta;

    @NotBlank(message = "El texto de la pregunta es obligatorio")
    private String texto;

    private String imagenUrl;
    private String ayuda;
    private Integer maxOpciones;  // Para MULTIPLE_CHOICE: cuántas puede elegir

    // Dimensión a la que se carga la pregunta (opcional). La define el coordinador.
    private Long idDimension;

    // Lista de opciones de respuesta (opcional para DESCRIPTION).
    // @Valid hace que se validen también los campos de cada opción.
    @Valid
    private List<OpcionPreguntaRequest> opcionPreguntaRequest;
}
