package com.udea.skillbridge.dto.request;

import jakarta.validation.constraints.Min;
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
public class OpcionPreguntaRequest {
	
	@NotNull
	@NotBlank
    private String texto;

	@NotNull
	@NotBlank
    private Boolean isCorrecta;

    @NotNull
    @NotBlank
    @Min(0)
    private Integer peso;

    @NotNull
    @NotBlank
    @Min(1)
    private Integer ordenVisualizacion;
}
