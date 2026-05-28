package com.udea.skillbridge.dto.request;

import com.udea.skillbridge.enums.EvaluacionFase;

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
public class IniciarEvaluacionRequest {
	
    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long idEstudiante;

    @NotNull(message = "La fase es obligatoria (PRE_TEST o POST_TEST)")
    private EvaluacionFase evaluacionFase; 

}
