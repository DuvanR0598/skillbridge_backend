package com.udea.skillbridge.dto.request;

import java.util.List;

import com.udea.skillbridge.enums.NivelBloom;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Solo permite modificar rango, descriptores y Bloom. skill, dimension y level
 * son inmutables para preservar trazabilidad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarPuntuacionMatrixRequest {
	
    @Min(value = 0)
    private Integer minPuntaje;

    @Min(value = 1)
    private Integer maxPuntaje;

    @Size(max = 1000)
    private String descripcion;

    @Size(max = 1000)
    private String caracteristicasObservables;

    @Size(max = 6)
    private List<NivelBloom> nivelesBloom;

}
