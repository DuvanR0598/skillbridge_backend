package com.udea.skillbridge.dto.response.analytics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.udea.skillbridge.enums.EvaluacionEstado;
import com.udea.skillbridge.enums.EvaluacionFase;
import com.udea.skillbridge.enums.SkillTipo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Historial de intentos de un estudiante (tendencia a lo largo del tiempo).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialIntentosResponse {

	private Long idEvaluacion;
	private Integer numeroIntentos;
	private EvaluacionFase fase;
	private EvaluacionEstado estado;
	private LocalDateTime startedAt;
	private LocalDateTime finishedAt;
	private List<PuntuacionIntentosResponse> puntaje;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class PuntuacionIntentosResponse {
		private SkillTipo skill;
		private Long idDimension;
		private String dimensionNombre;
		private Integer totalPuntuacion;
		private BigDecimal porcentajePuntuacion;
		private String nivel;
	}

}
