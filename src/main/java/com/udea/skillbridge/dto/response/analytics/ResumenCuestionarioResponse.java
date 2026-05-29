package com.udea.skillbridge.dto.response.analytics;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resumen ejecutivo del cuestionario para el panel del docente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenCuestionarioResponse {

	private Long idCuestionario;
	private String nombreCuestionario;
	private String estado;
	private LocalDateTime generatedAt;

	// Participación
	private Long totalEstudiantesConPreTest;
	private Long totalEstudiantesConPostTest;
	private Long TotalCompletadoAmbasFases;
	private Double tasaFinalizacion;

	// Resultados globales
	private Double avgPrePorcentaje;
	private Double avgPostPorcentaje;
	private Double avgDelta;
	private Double pctMejorado;

	// Certificación
	private Long EstudiantesElegiblesParaCertificacion;
	private Long estudiantesQueNecesitanSerReadmitidos;

}
