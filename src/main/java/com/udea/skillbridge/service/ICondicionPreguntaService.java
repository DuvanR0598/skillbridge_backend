package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.request.CondicionPreguntaRequest;
import com.udea.skillbridge.dto.response.CondicionPreguntaResponse;

public interface ICondicionPreguntaService {
	
	CondicionPreguntaResponse crearCondicion(Long idCuestionario, CondicionPreguntaRequest request);
	
	List<CondicionPreguntaResponse> listarCondiciones(Long idCuestionario);
	
	void eliminarCondicion(Long idCuestionario, Long idCondicion);

}
