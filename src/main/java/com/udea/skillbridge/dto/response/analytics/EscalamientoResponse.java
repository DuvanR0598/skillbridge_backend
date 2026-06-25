package com.udea.skillbridge.dto.response.analytics;

import java.util.List;

import com.udea.skillbridge.enums.analytics.DecisionEscala;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Decisión de escalamiento para un estudiante: CERTIFICAR → logró
 * AVANZADO en todas → emitir certificación REINICIAR → no logró AVANZADO →
 * reiniciar en Fase 2 con plan ajustado PENDIENTE → no completó POST_TEST
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscalamientoResponse {

	private Long idEstudiante;
	private Long idCuestionario;
	private DecisionEscala decision;
	private String razon;

	// Dimensiones en las que aún no alcanzó AVANZADO
	private List<SkillProgresoResponse> dimensionesPendientes;

	// Dimensiones en las que sí alcanzó AVANZADO
	private List<SkillProgresoResponse> dimensionesAlcanzadas;

}
