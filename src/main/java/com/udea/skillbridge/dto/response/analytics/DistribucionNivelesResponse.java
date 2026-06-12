package com.udea.skillbridge.dto.response.analytics;

import com.udea.skillbridge.enums.EvaluacionFase;
import com.udea.skillbridge.enums.SkillTipo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Distribución de estudiantes por nivel en un skill+dimensión.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistribucionNivelesResponse {
	
    private SkillTipo skill;
    private Long idDimension;          // dimensión gestionada (tabla); null = global
    private String dimensionNombre;    // nombre legible; null = global
    private EvaluacionFase fase;

    private Long recuentoBasico;
    private Long recuentoIntermedio;
    private Long recuentoAvanzado;
    private Long totalEstudiantes;

    private Double porcentajeBasico;
    private Double porcentajeIntermedio;
    private Double porcentajeAvanzado;

}
