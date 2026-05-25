package com.udea.skillbridge.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.CondicionPreguntaRequest;
import com.udea.skillbridge.dto.response.CondicionPreguntaResponse;
import com.udea.skillbridge.entity.CondicionPreguntaEntity;
import com.udea.skillbridge.entity.CuestionarioEntity;
import com.udea.skillbridge.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.mapper.ICondicionPreguntaMapper;
import com.udea.skillbridge.repository.ICondicionPreguntaRepository;
import com.udea.skillbridge.repository.ICuestionarioRepository;
import com.udea.skillbridge.repository.IPreguntaCuestionarioRepository;
import com.udea.skillbridge.repository.IPreguntaRepository;
import com.udea.skillbridge.service.ICondicionPreguntaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CondicionPreguntaServiceImpl implements ICondicionPreguntaService {
	
	private final ICuestionarioRepository cuestionarioRepository;
	private final IPreguntaRepository preguntaRepository;
	private final IPreguntaCuestionarioRepository pqRepository;
	private final ICondicionPreguntaRepository condicionRepository;
	private final ICondicionPreguntaMapper condicionMapper;
	
	// Tipos que NO pueden ser trigger (no tienen opciones seleccionables)
    private static final List<TipoPregunta> INVALIDAR_TIPO_TRIGGER =
            List.of(TipoPregunta.DESCRIPCION);
    
	// *****************************************
    //  CREAR CONDICION
    // *****************************************

    @Override
	public CondicionPreguntaResponse crearCondicion(Long idCuestionario, CondicionPreguntaRequest request) {
		log.info("Creando condición para el cuestionario: {}", idCuestionario);
		
		// 1. Cuestionario existe y está en BORRADOR
        CuestionarioEntity cuestionarioEnt = cuestionarioRepository
                .findActivoById(idCuestionario)
                .orElseThrow(() -> new ResourceNotFoundException("Cuestionario", idCuestionario));
        
        // Validar trigger pregunta
        PreguntaEntity triggerPregunta = buscarPregunta(request.getTriggerIdPregunta());

        if (!cuestionarioEnt.isEditable()) {
            throw new BusinessException(
                "Solo se pueden agregar condiciones a cuestionarios en estado BORRADOR."
            );
        }

        // 3. REGLA: DESCRIPTION no puede ser trigger
        if (INVALIDAR_TIPO_TRIGGER.contains(triggerPregunta.getTipoPregunta())) {
            throw new BusinessException(
                "Las preguntas de tipo DESCRIPCION no pueden ser disparadoras " +
                "porque no tienen opciones seleccionables."
            );
        }

        // 4. REGLA: trigger y target no pueden ser la misma pregunta
        if (request.getTriggerIdPregunta().equals(request.getTargetIdPreguta())) {
            throw new BusinessException(
                "La pregunta disparadora y la pregunta destino no pueden ser la misma."
            );
        }

        // 5. La opción trigger debe pertenecer a la pregunta trigger
        OpcionPreguntaEntity triggerOpcion = triggerPregunta.getOpcionPregunta().stream()
                .filter(o -> o.getId().equals(request.getTriggerIdOpcion()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                    "La opción " + request.getTriggerIdOpcion() +
                    " no pertenece a la pregunta " + request.getTriggerIdPregunta()
                ));
        
        // Validar que no exista duplicado (misma opción → misma pregunta)
        if (condicionRepository.existsByTriggerOpcionIdAndTargetPreguntaIdPregunta(
                request.getTriggerIdOpcion(), request.getTargetIdPreguta())) {
            throw new BusinessException(
                "Ya existe una condición con esta opción para esta pregunta destino",
                "DUPLICATE_CONDITION"
            );
        }

        // 6. Cargar la pregunta destino
        PreguntaEntity targetPregunta = buscarPregunta(request.getTargetIdPreguta());

        // 7. REGLA: ambas preguntas deben estar en el cuestionario
        validarPreguntaInCuestionario(request.getTriggerIdPregunta(), idCuestionario);
        validarPreguntaInCuestionario(request.getTargetIdPreguta(), idCuestionario);

        // 8. REGLA: la condición no puede existir ya (unicidad opción → pregunta)
        if (condicionRepository.existsByTriggerOpcionIdAndTargetPreguntaIdPregunta (
                request.getTriggerIdOpcion(), request.getTargetIdPreguta())) {
            throw new BusinessException(
                "Ya existe una condición que conecta esta opción con la pregunta destino."
            );
        }

        // 9. REGLA: una pregunta hija tiene máximo UNA condición de entrada
        int condicionEntrada = condicionRepository.countByTargetPreguntaIdPreguntaAndCuestionarioEntIdCuestionario(
                request.getTargetIdPreguta(), idCuestionario);

        if (condicionEntrada >= 1) {
            throw new BusinessException(
                "La pregunta destino ya tiene una condición de entrada. " +
                "Una pregunta condicional solo puede tener un disparador."
            );
        }

        // 10. REGLA: detectar ciclo directo A→B→A
        if (condicionRepository.existeCicloDirecto(
                request.getTriggerIdPregunta(),
                request.getTargetIdPreguta(),
                idCuestionario)) {
            throw new BusinessException(
                "Se detectó un ciclo: la pregunta destino ya es disparadora " +
                "de la pregunta origen. Los ciclos no están permitidos."
            );
        }

        // 11. Todo valido — crear la condición
        CondicionPreguntaEntity condicion = CondicionPreguntaEntity.builder()
                .cuestionarioEnt(cuestionarioEnt)
                .triggerPregunta(triggerPregunta)
                .triggerOpcion(triggerOpcion)
                .targetPregunta(targetPregunta)
                .build();

        CondicionPreguntaEntity guardar = condicionRepository.save(condicion);

        // 12. Marcar la pregunta target como condicional en la tabla intermedia
        marcarAsCondicional(request.getTargetIdPreguta(), idCuestionario);

        log.info("Condición creada: opción [{}] → pregunta [{}] en cuestionario [{}]",
                triggerOpcion.getId(), targetPregunta.getIdPregunta(), idCuestionario);

        return condicionMapper.toResponse(guardar);
	}
	
	// *****************************************
    //  LISTAR CONDICIONES DE UN CUESTIONARIO 
    // *****************************************
	
	@Override
	public List<CondicionPreguntaResponse> listarCondiciones(Long idCuestionario) {
		// Validar que el cuestionario existe
        if (!cuestionarioRepository.existsById(idCuestionario)) {
            throw new ResourceNotFoundException("Cuestionario", idCuestionario);
        }
        
        List<CondicionPreguntaEntity> condiciones = condicionRepository
                .findByCuestionarioEntIdCuestionario(idCuestionario);
        
        return condiciones.stream()
                .map(condicionMapper::toResponse)
                .toList();
	}
	
	// *****************************************
    //  ELIMINAR CONDICION
    // *****************************************

	@Override
	public void eliminarCondicion(Long idCuestionario, Long idCondicion) {
		log.info("Eliminando condición [{}] del cuestionario [{}]", idCondicion, idCuestionario);
		
		// Validar que la condición existe
		CondicionPreguntaEntity condicion = condicionRepository.findById(idCondicion)
                .orElseThrow(() -> new ResourceNotFoundException("Condición", idCondicion));

        // Solo en cuestionarios con estado BORRADOR
        if (!condicion.getCuestionarioEnt().isEditable()) {
            throw new BusinessException(
                "Solo se pueden eliminar condiciones de cuestionarios en estado BORRADOR."
            );
        }
        
        // Validar que pertenezca al cuestionario
        if (!condicion.getCuestionarioEnt().getIdCuestionario().equals(idCuestionario)) {
            throw new BusinessException(
                String.format("La condición %d no pertenece al cuestionario %d", idCondicion, idCuestionario),
                "CONDICION_NOT_IN_CUESTIONARIO"
            );
        }

        Long targetIdPregunta = condicion.getTargetPregunta().getIdPregunta();
        condicionRepository.delete(condicion);

        // Si la pregunta ya no tiene condiciones de entrada, quitarle el flag
        int restante = condicionRepository.countByTargetPreguntaIdPreguntaAndCuestionarioEntIdCuestionario(
                targetIdPregunta, idCuestionario);

        if (restante == 0) {
        	demarcarAsCondicional(targetIdPregunta, idCuestionario);
        }

        log.info("Condición [{}] eliminada del cuestionario [{}]", idCondicion, idCuestionario);
		
	}
	
	// *****************************************
    //  MÉTODOS PRIVADOS
    // *****************************************
	
	private PreguntaEntity buscarPregunta(Long idPregunta) {
        return preguntaRepository.findById(idPregunta)
        		.orElseThrow(() -> new ResourceNotFoundException("Pregunta", idPregunta));
    }
	
	private void validarPreguntaInCuestionario(Long idPregunta, Long idCuestionario) {
        boolean existe = pqRepository.existsByIdIdCuestionarioAndIdIdPregunta(
                idCuestionario, idPregunta);
        if (!existe) {
            throw new BusinessException(
                "La pregunta " + idPregunta +
                " no está asociada al cuestionario " + idCuestionario +
                ". Agrégala primero."
            );
        }
    }
	
	private void marcarAsCondicional(Long idPregunta, Long idCuestionario) {
        pqRepository.findByIdIdCuestionarioAndIdIdPregunta(idCuestionario, idPregunta)
                .ifPresent(pq -> {
                    pq.setIsCondicional(true);
                    pq.setObligatoria(false); // condicional nunca es obligatoria
                    pqRepository.save(pq);
                });
    }
	
	private void demarcarAsCondicional(Long idPregunta, Long idCuestionario) {
        pqRepository.findByIdIdCuestionarioAndIdIdPregunta(idCuestionario, idPregunta)
                .ifPresent(pq -> {
                    pq.setIsCondicional(false);
                    pqRepository.save(pq);
                });
    }
}
