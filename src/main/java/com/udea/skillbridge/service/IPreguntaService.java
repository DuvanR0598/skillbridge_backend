package com.udea.skillbridge.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.udea.skillbridge.dto.request.ActualizarPesoOpcionesRequest;
import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.dto.response.PaginaResponse;
import com.udea.skillbridge.dto.response.PreguntaResponse;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.enums.TipoPregunta;

public interface IPreguntaService {

	PreguntaResponse crearPregunta(PreguntaRequest request);

	/** Sube una imagen para una pregunta y devuelve su URL (ej. /uploads/preguntas/xxx.jpg). */
	String subirImagen(MultipartFile file);

	/** Actualiza (o quita, con null) la imagen de una pregunta existente. */
	PreguntaResponse actualizarImagen(Long idPregunta, String imagenUrl);

	PreguntaResponse findById (Long idPregunta);

	List<PreguntaResponse> listarTodo();

	List<PreguntaResponse> listarPorTipo(TipoPregunta tipoPregunta);

	/** Banco de preguntas paginado (filtros opcionales: tipo, texto, skill y dimensión). */
	PaginaResponse<PreguntaResponse> listarPaginado(int page, int size, TipoPregunta tipoPregunta, String texto, SkillTipo skill, Long idDimension);
	
	PreguntaResponse actualizarPesosOpciones(Long idPregunta, ActualizarPesoOpcionesRequest request);

	/** Asigna o cambia la dimensión de una pregunta (idDimension null = desasignar). */
	PreguntaResponse asignarDimension(Long idPregunta, Long idDimension);

	void eliminarPregunta(Long preguntaId);

}
