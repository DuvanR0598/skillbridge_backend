package com.udea.skillbridge.service;

import java.util.List;
import java.util.Map;

import com.udea.skillbridge.dto.response.analytics.AnalisisDimensionalResponse;
import com.udea.skillbridge.dto.response.analytics.DistribucionNivelesResponse;
import com.udea.skillbridge.dto.response.analytics.EscalamientoResponse;
import com.udea.skillbridge.dto.response.analytics.EstudianteQueNecesitaApoyoResponse;
import com.udea.skillbridge.dto.response.analytics.HistorialIntentosResponse;
import com.udea.skillbridge.dto.response.analytics.InformeProgresoEstudianteResponse;
import com.udea.skillbridge.dto.response.analytics.ReporteGrupoResponse;
import com.udea.skillbridge.dto.response.analytics.ResumenCuestionarioResponse;

public interface IAnalyticsService {
	
	InformeProgresoEstudianteResponse getProgresoEstudiante(Long idEstudiante, Long idCuestionario);
	
	ReporteGrupoResponse getReporteGrupo(Long idCuestionario);
	
	List<AnalisisDimensionalResponse> getAnalisisDimensional(Long idCuestionario);
	
	EscalamientoResponse getEscalamiento(Long idEstudiante, Long idCuestionario);
	
	ResumenCuestionarioResponse getResumen(Long idCuestionario);
	
	List<HistorialIntentosResponse> getHistorialIntentos(Long idEstudiante, Long idCuestionario);
	
	List<DistribucionNivelesResponse> getDistribucionNiveles(Long idCuestionario);
	
	List<EstudianteQueNecesitaApoyoResponse> getEstudiantesNecesitanApoyo(Long idCuestionario);
	
	List<Long> getEstudiantesSinPreTest(Long idCuestionario);
	
	Map<String, Object> getEstadisticaFinalizacion(Long idCuestionario);

}
