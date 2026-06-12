package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.request.ActualizarPesoOpcionesRequest;
import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.dto.response.PreguntaResponse;
import com.udea.skillbridge.enums.TipoPregunta;

public interface IPreguntaService {
	
	PreguntaResponse crearPregunta(PreguntaRequest request); 
	
	PreguntaResponse findById (Long idPregunta);
	
	List<PreguntaResponse> listarTodo();
	
	List<PreguntaResponse> listarPorTipo(TipoPregunta tipoPregunta);
	
	PreguntaResponse actualizarPesosOpciones(Long idPregunta, ActualizarPesoOpcionesRequest request);

	/** Asigna o cambia la dimensión de una pregunta (idDimension null = desasignar). */
	PreguntaResponse asignarDimension(Long idPregunta, Long idDimension);

	void eliminarPregunta(Long preguntaId);

}
