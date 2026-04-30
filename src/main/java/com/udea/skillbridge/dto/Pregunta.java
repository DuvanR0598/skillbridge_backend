package com.udea.skillbridge.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.udea.skillbridge.enums.TipoPregunta;

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
public class Pregunta {

	@NotNull(message = "El tipo de pregunta es obligatorio")
    private TipoPregunta tipoPregunta;

    @NotBlank(message = "El texto de la pregunta es obligatorio")
    private String texto;

    private String imagenUrl;
    private String descripcion;
    private String ayuda;
    private Integer maxOpciones;
    private LocalDateTime createdAt;

    // Lista de opciones de respuesta (opcional para DESCRIPTION)
    private List<OpcionPregunta> opcionPregunta;
}
