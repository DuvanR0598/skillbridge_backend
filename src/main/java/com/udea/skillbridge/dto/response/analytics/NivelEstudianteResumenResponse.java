package com.udea.skillbridge.dto.response.analytics;

import java.math.BigDecimal;

import com.udea.skillbridge.enums.EvaluacionFase;
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
    private String nombreCompleto;   // nombre + apellido
    private String email;
    private Long idEvaluacion;       // id de la sesión (para consultar sus respuestas)
    private SkillTipo skill;
    private Long idDimension;
    private String dimensionNombre;
    private EvaluacionFase fase;
    private Integer totalPuntaje;
    private Integer maxPosiblePuntaje;
    private BigDecimal puntajePorcentaje;
    private SkillNivel nivel;

}
