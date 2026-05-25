package com.udea.skillbridge.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.ActualizarPlanFortalecimientoRequest;
import com.udea.skillbridge.dto.request.PlanFortalecimientoRequest;
import com.udea.skillbridge.dto.response.PlanFortalecimientoResponse;
import com.udea.skillbridge.entity.PlanFortalecimientoEntity;
import com.udea.skillbridge.entity.PuntuacionMatrixEntity;
import com.udea.skillbridge.enums.PlanAxis;
import com.udea.skillbridge.mapper.IPlanFortalecimientoMapper;
import com.udea.skillbridge.repository.IPlanFortalecimientoRepository;
import com.udea.skillbridge.service.IPlanFortalecimientoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanFortalecimientoServiceImpl implements IPlanFortalecimientoService {
	
	private final IPlanFortalecimientoRepository planRepository;
	private final PuntuacionMatrixServiceImpl puntuacionMatrixService;
	private final IPlanFortalecimientoMapper planMapper;
	
	// *****************************************
	// CREAR PLAN PARA UN EJE
	// *****************************************
	
	@Override
	public PlanFortalecimientoResponse crear(Long idMatrix, PlanFortalecimientoRequest request) {
		PuntuacionMatrixEntity matrix = puntuacionMatrixService.findEntityById(idMatrix);

        // REGLA: un solo plan por eje dentro de la misma entrada de la matriz
        if (planRepository.existsByPuntuacionMatrixEntIdAndPlanAxis(idMatrix, request.getPlanAxis())) {
            throw new BusinessException(
                "La entrada de la matriz " + idMatrix +
                " ya tiene un plan para el eje " + request.getPlanAxis() + ". " +
                "Use el endpoint de actualización.",
                "PLAN_AXIS_ALREADY_EXISTS"
            );
        }

        // REGLA: máximo 3 planes por entrada (uno por eje)
        int existingPlans = planRepository.countByPuntuacionMatrixEntId(idMatrix);
        if (existingPlans >= 3) {
            throw new BusinessException(
                "La entrada de la matriz ya tiene los 3 planes configurados " +
                "(ACADEMIC, EXPERIENTIAL, PERSONAL).",
                "MAX_PLANS_REACHED"
            );
        }

        PlanFortalecimientoEntity plan = planMapper.toEntity(request);
        plan.setPuntuacionMatrixEnt(matrix);

        PlanFortalecimientoEntity guardar = planRepository.save(plan);
        log.info("Plan [{}] creado para matriz [{}] eje [{}]",
                guardar.getId(), idMatrix, request.getPlanAxis());

        return planMapper.toResponse(guardar);
	}
	
	// *******************************************
	// LISTAR PLANES DE UNA ENTRADA DE LA MATRIZ
	// *******************************************

	@Override
	public List<PlanFortalecimientoResponse> findByMatrix(Long idMatrix) {
		puntuacionMatrixService.findEntityById(idMatrix);
        return planRepository.findBypuntuacionMatrixEntId(idMatrix)
                .stream()
                .map(planMapper::toResponse)
                .toList();
	}
	
	// *******************************************
	// OBTENER PLAN POR EJE
	// *******************************************
	
	@Override
	public PlanFortalecimientoResponse findByMatrixAndAxis(Long idMatrix, PlanAxis axis) {
		return planRepository.findBypuntuacionMatrixEntIdAndPlanAxis(idMatrix, axis)
                .map(planMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No existe plan para el eje " + axis +
                    " en la entrada de la matriz " + idMatrix + "."
                ));
	}
	
	// *******************************************
	// OBTENER PLAN POR ID
	// *******************************************

	@Override
	public PlanFortalecimientoResponse findById(Long planId) {
		return planMapper.toResponse(findEntityById(planId));
	}
	
	// *******************************************
	// ACTUALIZAR
	// *******************************************

	@Override
	public PlanFortalecimientoResponse actualizar(Long planId, ActualizarPlanFortalecimientoRequest request) {
		PlanFortalecimientoEntity plan = findEntityById(planId);

        // Actualizar RECURSOS si vienen explícitamente (reemplaza la lista completa)
        if (request.getRecursos() != null) {
            plan.getRecursos().clear();
            plan.getRecursos().addAll(request.getRecursos());
        }

        planMapper.updateFromRequest(plan, request);
        return planMapper.toResponse(planRepository.save(plan));
	}
	
	// *******************************************
	// ELIMINAR
	// *******************************************
	
	@Override
	public void eliminar(Long planId) {
		PlanFortalecimientoEntity plan = findEntityById(planId);
        planRepository.delete(plan);
        log.info("Plan [{}] eliminado", planId);
	}
	
	public PlanFortalecimientoEntity findEntityById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StrengtheningPlan", id));
    }

}
