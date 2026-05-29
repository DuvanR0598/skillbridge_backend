package com.udea.skillbridge.dto.response.analytics;

import java.math.BigDecimal;

import com.udea.skillbridge.enums.EvaluacionFase;
import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resumen de nivel de un estudiante para tablas del reporte de grupo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelEstudianteResumenResponse {
	
    private Long idEstudiante;
    private SkillTipo skill;
    private SkillDimension dimension;
    private EvaluacionFase fase;
    private Integer totalPuntaje;
    private Integer maxPosiblePuntaje;
    private BigDecimal puntajePorcentaje;
    private SkillNivel nivel;

}
