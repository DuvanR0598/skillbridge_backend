package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.request.ActualizarPuntuacionMatrixRequest;
import com.udea.skillbridge.dto.request.PuntuacionMatrixRequest;
import com.udea.skillbridge.dto.response.PuntuacionMatrixResponse;
import com.udea.skillbridge.enums.SkillTipo;

public interface IPuntuacionMatrixService {
	
	PuntuacionMatrixResponse crear(Long idcuestionario, PuntuacionMatrixRequest request);
	
	List<PuntuacionMatrixResponse> findByCuestionario(Long isCuestionario);
	
	List<PuntuacionMatrixResponse> findByCuestionarioAndSkill(Long idcuestionario, SkillTipo skill);
	
	PuntuacionMatrixResponse findById(Long idMatrix);
	
	PuntuacionMatrixResponse actualizar(Long idMatrix, ActualizarPuntuacionMatrixRequest request);
	
	void eliminar(Long idMatrix);

}
