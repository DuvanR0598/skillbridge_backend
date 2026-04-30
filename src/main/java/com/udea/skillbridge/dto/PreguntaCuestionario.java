package com.udea.skillbridge.dto;

import jakarta.validation.constraints.Min;
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
public class PreguntaCuestionario {
	
	@NotNull
    private Long idpreguntaCuestionario;

    @NotNull
    private Boolean obligatoria;

    @NotNull
    @Min(value = 1, message = "El peso debe ser al menos 1")
    private Integer peso;

}
