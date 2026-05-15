package com.udea.skillbridge.service.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.udea.skillbridge.dto.request.CrearCondicionPreguntaRequest;
import com.udea.skillbridge.dto.response.CondicionPreguntaResponse;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.persistence.entity.CondicionPreguntaEntity;
import com.udea.skillbridge.persistence.entity.CuestionarioEntity;
import com.udea.skillbridge.persistence.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.persistence.entity.PreguntaEntity;
import com.udea.skillbridge.persistence.repository.ICondicionPreguntaRepository;
import com.udea.skillbridge.persistence.repository.ICuestionarioRepository;
import com.udea.skillbridge.persistence.repository.IPreguntaCuestionarioRepository;
import com.udea.skillbridge.persistence.repository.IPreguntaRepository;
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
	
	// Tipos que NO pueden ser trigger (no tienen opciones seleccionables)
    private static final List<TipoPregunta> INVALIDAR_TIPO_TRIGGER =
            List.of(TipoPregunta.DESCRIPCION);
    
	// *****************************************
    //  CREAR CONDICION
    // *****************************************

	@Override
	public CondicionPreguntaResponse crearCondicion(Long idCuestionario, CrearCondicionPreguntaRequest request) {
		// 1. Cuestionario existe y está en BORRADOR
        CuestionarioEntity cuestionarioEnt = cuestionarioRepository
                .findActivoById(idCuestionario)
                .orElseThrow(() -> new CuestionarioException(
                    "Cuestionario no encontrado: " + idCuestionario, HttpStatus.NOT_FOUND
                ));

        if (!cuestionarioEnt.isEditable()) {
            throw new CuestionarioException(
                "Solo se pueden agregar condiciones a cuestionarios en estado BORRADOR."
            );
        }

        // 2. Cargar la pregunta trigger
        PreguntaEntity triggerPregunta = buscarPregunta(request.getTriggerIdPregunta());

        // 3. REGLA: DESCRIPTION no puede ser trigger
        if (INVALIDAR_TIPO_TRIGGER.contains(triggerPregunta.getTipoPregunta())) {
            throw new CuestionarioException(
                "Las preguntas de tipo DESCRIPCION no pueden ser disparadoras " +
                "porque no tienen opciones seleccionables."
            );
        }

        // 4. REGLA: trigger y target no pueden ser la misma pregunta
        if (request.getTriggerIdPregunta().equals(request.getTargetIdPreguta())) {
            throw new CuestionarioException(
                "La pregunta disparadora y la pregunta destino no pueden ser la misma."
            );
        }

        // 5. La opción trigger debe pertenecer a la pregunta trigger
        OpcionPreguntaEntity triggerOpcion = triggerPregunta.getOpcionPregunta().stream()
                .filter(o -> o.getIdOpcPregunta().equals(request.getTriggerIdOpcion()))
                .findFirst()
                .orElseThrow(() -> new CuestionarioException(
                    "La opción " + request.getTriggerIdOpcion() +
                    " no pertenece a la pregunta " + request.getTriggerIdPregunta()
                ));

        // 6. Cargar la pregunta destino
        PreguntaEntity targetPregunta = buscarPregunta(request.getTargetIdPreguta());

        // 7. REGLA: ambas preguntas deben estar en el cuestionario
        validarPreguntaInCuestionario(request.getTriggerIdPregunta(), idCuestionario);
        validarPreguntaInCuestionario(request.getTargetIdPreguta(), idCuestionario);

        // 8. REGLA: la condición no puede existir ya (unicidad opción → pregunta)
        if (condicionRepository.existsByTriggerOpcionIdOpcPreguntaAndTargetPreguntaIdPregunta (
                request.getTriggerIdOpcion(), request.getTargetIdPreguta())) {
            throw new CuestionarioException(
                "Ya existe una condición que conecta esta opción con la pregunta destino."
            );
        }

        // 9. REGLA: una pregunta hija tiene máximo UNA condición de entrada
        int condicionEntrada = condicionRepository.countByTargetPreguntaIdPreguntaAndCuestionarioEntIdCuestionario(
                request.getTargetIdPreguta(), idCuestionario);

        if (condicionEntrada >= 1) {
            throw new CuestionarioException(
                "La pregunta destino ya tiene una condición de entrada. " +
                "Una pregunta condicional solo puede tener un disparador."
            );
        }

        // 10. REGLA: detectar ciclo directo A→B→A
        if (condicionRepository.existeCicloDirecto(
                request.getTriggerIdPregunta(),
                request.getTargetIdPreguta(),
                idCuestionario)) {
            throw new CuestionarioException(
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
                triggerOpcion.getIdOpcPregunta(), targetPregunta.getIdPregunta(), idCuestionario);

        return toResponse(guardar);
	}
	
	// *****************************************
    //  LISTAR CONDICIONES DE UN CUESTIONARIO 
    // *****************************************
	
	@Override
	public List<CondicionPreguntaResponse> listarCondiciones(Long idCuestionario) {
        return condicionRepository.findByCuestionarioEntIdCuestionario(idCuestionario)
                .stream()
                .map(this::toResponse)
                .toList();
	}
	
	// *****************************************
    //  ELIMINAR CONDICION
    // *****************************************

	@Override
	public void eliminarCondicion(Long idCuestionario, Long idCondicion) {
		CondicionPreguntaEntity condicion = condicionRepository.findById(idCondicion)
                .orElseThrow(() -> new CuestionarioException(
                    "Condición no encontrada: " + idCondicion, HttpStatus.NOT_FOUND
                ));

        // Solo en cuestionarios con estado BORRADOR
        if (!condicion.getCuestionarioEnt().isEditable()) {
            throw new CuestionarioException(
                "Solo se pueden eliminar condiciones de cuestionarios en estado BORRADOR."
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
                .orElseThrow(() -> new CuestionarioException(
                    "Pregunta no encontrada: " + idPregunta, HttpStatus.NOT_FOUND
                ));
    }
	
	private void validarPreguntaInCuestionario(Long idPregunta, Long idCuestionario) {
        boolean existe = pqRepository.existsByIdIdCuestionarioAndIdIdPregunta(
                idCuestionario, idPregunta);
        if (!existe) {
            throw new CuestionarioException(
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
	
	private CondicionPreguntaResponse toResponse(CondicionPreguntaEntity c) {
        return CondicionPreguntaResponse.builder()
                .id(c.getId())
                .idCuestionario(c.getCuestionarioEnt().getIdCuestionario())
                .triggerIdPregunta(c.getTriggerPregunta().getIdPregunta())
                .triggerTextoPregunta(c.getTriggerPregunta().getTexto())
                .triggerIdOpcion(c.getTriggerOpcion().getIdOpcPregunta())
                .triggerTextoOpcion(c.getTriggerOpcion().getTexto())
                .targetIdPregunta(c.getTargetPregunta().getIdPregunta())
                .targetTextopregunta(c.getTargetPregunta().getTexto())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
