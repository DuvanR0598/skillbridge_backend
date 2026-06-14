package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.request.EnviarRespuestaRequest;
import com.udea.skillbridge.dto.request.IniciarEvaluacionRequest;
import com.udea.skillbridge.dto.response.DetalleRespuestaResponse;
import com.udea.skillbridge.dto.response.EvaluacionEstudianteResponse;
import com.udea.skillbridge.dto.response.InformeEvaluacionResponse;
import com.udea.skillbridge.dto.response.TiempoConteoResponse;

public interface IEvaluacionEstudianteService {

	EvaluacionEstudianteResponse iniciar(Long idCuestionario, IniciarEvaluacionRequest request);

	TiempoConteoResponse iniciarConteo(Long idEvaluacion);
	
	DetalleRespuestaResponse enviarRespuesta(Long idEvaluacion, EnviarRespuestaRequest request);
	
	DetalleRespuestaResponse actualizarRespuesta(Long idEvaluacion, Long idPregunta, EnviarRespuestaRequest request);
	
	InformeEvaluacionResponse completo(Long idEvaluacion);
	
	EvaluacionEstudianteResponse findById(Long idEvaluacion);
	
	List<EvaluacionEstudianteResponse> findByEstudiante(Long idEstudiante, Long idCuestionario);
	
	InformeEvaluacionResponse getReporte(Long idEvaluacion);

}
