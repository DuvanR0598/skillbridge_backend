package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.request.ActualizarPesoOpcionesRequest;
import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.dto.response.PaginaResponse;
import com.udea.skillbridge.dto.response.PreguntaResponse;
import com.udea.skillbridge.enums.TipoPregunta;

public interface IPreguntaService {

	PreguntaResponse crearPregunta(PreguntaRequest request);

	PreguntaResponse findById (Long idPregunta);

	List<PreguntaResponse> listarTodo();

	List<PreguntaResponse> listarPorTipo(TipoPregunta tipoPregunta);

	/** Banco de preguntas paginado (filtros opcionales: tipo y texto del enunciado). */
	PaginaResponse<PreguntaResponse> listarPaginado(int page, int size, TipoPregunta tipoPregunta, String texto);
	
	PreguntaResponse actualizarPesosOpciones(Long idPregunta, ActualizarPesoOpcionesRequest request);

	/** Asigna o cambia la dimensión de una pregunta (idDimension null = desasignar). */
	PreguntaResponse asignarDimension(Long idPregunta, Long idDimension);

	void eliminarPregunta(Long preguntaId);

}
