package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.udea.skillbridge.enums.EvaluacionEstado;
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
public class EvaluacionEstudianteResponse {
	
    private Long id;
    private Long idEstudiante;
    private Long idCuestionario;
    private String nombreCuestionario;
    private EvaluacionFase evaluacionFase;
    private EvaluacionEstado estado;
    private Integer numeroIntento;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer totalRespuestas;
    private List<DetalleRespuestaResponse> respuestas;
    private List<PuntuacionResultadoResponse> resultados;

}
