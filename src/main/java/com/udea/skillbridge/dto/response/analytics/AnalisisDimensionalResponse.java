package com.udea.skillbridge.dto.response.analytics;

import java.math.BigDecimal;

import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillTipo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Análisis de una dimensión específica en el grupo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalisisDimensionalResponse {
	
    private SkillTipo skill;
    private SkillDimension dimension;

    // Promedios del grupo
    private BigDecimal avgPrePorcentaje;
    private BigDecimal avgPostPorcentaje;
    private BigDecimal avgDelta;

    // Distribución PRE
    private DistribucionNivelesResponse preDistribucion;

    // Distribución POST
    private DistribucionNivelesResponse postDistribucion;

    // Estadísticas de mejora
    private Long estudiantesMejorados;       // subieron al menos un nivel
    private Long estudiantesEstancados;      // mantuvieron el mismo nivel
    private Long estudiantesRetrocedieron;   // bajaron de nivel
    private Long totalEstudiantes;

    private boolean masCritico;      // dimensión con peor promedio
    private boolean masMejoro;       // dimensión con mayor delta

}
