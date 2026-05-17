package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.request.ActualizarCuestionarioRequest;
import com.udea.skillbridge.dto.request.CuestionarioRequest;
import com.udea.skillbridge.dto.request.PreguntaCuestionarioRequest;
import com.udea.skillbridge.dto.response.CuestionarioEntregaResponse;
import com.udea.skillbridge.dto.response.CuestionarioResponse;

public interface ICuestionarioService {
	
	CuestionarioResponse crearCuestionario (CuestionarioRequest cuestionarioRequest);
	
	CuestionarioResponse findById (Long idCuestionario);
	
	List<CuestionarioResponse> listarAllCuestionarios();
	
	List<CuestionarioResponse> listarCuestionariosActivos();
	
	CuestionarioResponse actualizarCuestionario(Long id, ActualizarCuestionarioRequest request);
	
	void addPretuntaToCuestinario(Long idCuestionario, PreguntaCuestionarioRequest request);
	
	CuestionarioResponse cuestionarioCompleto(Long idCuestionario);
	
	CuestionarioResponse cuestionarioPublicado(Long idCuestionario);
	
	CuestionarioResponse cuestionarioArchivado(Long idCuestionario);
	
	void borradoLogico(Long idCuestionario);
	
	CuestionarioEntregaResponse entregarCuestionario (Long idCuestionario);

}
