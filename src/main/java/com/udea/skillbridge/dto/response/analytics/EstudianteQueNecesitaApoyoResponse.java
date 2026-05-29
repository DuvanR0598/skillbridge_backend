package com.udea.skillbridge.dto.response.analytics;

import java.math.BigDecimal;
import java.util.List;

import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillTipo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estudiante en nivel BAJO — necesita atención prioritaria del docente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteQueNecesitaApoyoResponse {

	private Long idEstudiante;
	private Long idPreTestEvaluacion;

	// Dimensiones en BAJO
	private List<DimensionBajaResponse> dimensionBaja;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DimensionBajaResponse {
		private SkillTipo skill;
		private SkillDimension dimension;
		private Integer puntaje;
		private BigDecimal porcentaje;
	}

}
