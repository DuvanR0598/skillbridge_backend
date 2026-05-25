package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.request.ActualizarPlanFortalecimientoRequest;
import com.udea.skillbridge.dto.request.PlanFortalecimientoRequest;
import com.udea.skillbridge.dto.response.PlanFortalecimientoResponse;
import com.udea.skillbridge.enums.PlanAxis;

public interface IPlanFortalecimientoService {
	
	PlanFortalecimientoResponse crear(Long idMatrix, PlanFortalecimientoRequest request);
	
	List<PlanFortalecimientoResponse> findByMatrix(Long idMatrix);
	
	PlanFortalecimientoResponse findByMatrixAndAxis(Long idMatrix, PlanAxis axis);
	
	PlanFortalecimientoResponse findById(Long planId);
	
	PlanFortalecimientoResponse actualizar(Long planId, ActualizarPlanFortalecimientoRequest request);
	
	void eliminar(Long planId);

}
