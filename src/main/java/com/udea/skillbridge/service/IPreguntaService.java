package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.ActualizarPesoOpcionesRequest;
import com.udea.skillbridge.dto.Pregunta;
import com.udea.skillbridge.enums.TipoPregunta;

public interface IPreguntaService {
	
	Pregunta crearPregunta(Pregunta pregunta); 
	
	Pregunta getById (Long idPregunta);
	
	List<Pregunta> listarPorTipo(TipoPregunta tipoPregunta);
	
	List<Pregunta> listarTodo();
	
	void eliminarPregunta(Long preguntaId);
	
	Pregunta actualizarPesosOpciones(Long idPregunta, ActualizarPesoOpcionesRequest request);

}
