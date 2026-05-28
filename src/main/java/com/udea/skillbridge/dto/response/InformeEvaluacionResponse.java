package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.udea.skillbridge.enums.EvaluacionFase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformeEvaluacionResponse {
	
    private Long idEvaluacion;
    private Long idEstudiante;
    private String nombreCuestionario;
    private EvaluacionFase evaluacionFase;
    private Integer numeroIntento;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    // Resultados por skill y dimensión
    private List<PuntuacionResultadoResponse> resultados;

    // Resumen ejecutivo
    private String resumenGeneral;

}
