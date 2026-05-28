package com.udea.skillbridge.dto.response.analytics;

import java.time.LocalDateTime;
import java.util.List;

import com.udea.skillbridge.dto.response.PlanFortalecimientoResponse;
import com.udea.skillbridge.enums.analytics.DecisionEscala;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reporte completo de progreso de un estudiante en un cuestionario. Cubre las 4
 * fases del modelo pedagógico.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformeProgresoEstudianteResponse {
	
	private Long idEstudiante;
    private Long idCuestionario;
    private String nombreCuestionario;

    // Metadata de las sesiones
    private Long idPreTestEvaluacion;
    private Long idPostTestEvaluacion;
    private LocalDateTime preTestDate;
    private LocalDateTime postTestDate;

    // Progreso por dimensión
    private List<SkillProgresoResponse> skillProgreso;

    // Decisión de escalamiento (Fase 4)
    private DecisionEscala decisionEscala;
    private String razonEscala;

    // Planes asignados por nivel actual
    private List<PlanFortalecimientoResponse> planActual;

    // Resumen narrativo

}
