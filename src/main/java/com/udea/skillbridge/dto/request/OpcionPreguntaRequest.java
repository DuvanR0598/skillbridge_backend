package com.udea.skillbridge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class OpcionPreguntaRequest {
	
	@NotBlank(message = "El texto de la opción es obligatorio")
	@Size(max = 500, message = "El texto de la opción no puede superar 500 caracteres")
    private String texto;

	@NotNull(message = "Debe indicar si la opción es correcta")
    private Boolean isCorrecta;

    @NotNull(message = "El peso de la opción es obligatorio")
    @Min(value = 0, message = "El peso no puede ser negativo")
    private Integer peso;

    @NotNull(message = "El orden de visualización es obligatorio")
    @Min(value = 1, message = "El orden mínimo es 1")
    private Integer ordenVisualizacion;
}
