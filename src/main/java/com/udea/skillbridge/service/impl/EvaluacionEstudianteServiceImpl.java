package com.udea.skillbridge.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.EnviarRespuestaRequest;
import com.udea.skillbridge.dto.request.IniciarEvaluacionRequest;
import com.udea.skillbridge.dto.response.DetalleRespuestaResponse;
import com.udea.skillbridge.dto.response.EvaluacionEstudianteResponse;
import com.udea.skillbridge.dto.response.InformeEvaluacionResponse;
import com.udea.skillbridge.dto.response.PuntuacionResultadoResponse;
import com.udea.skillbridge.entity.CuestionarioEntity;
import com.udea.skillbridge.entity.DetalleRespuestaEntity;
import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;
import com.udea.skillbridge.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.entity.PuntuacionResultadoEntity;
import com.udea.skillbridge.enums.EstadoCuestionario;
import com.udea.skillbridge.enums.EvaluacionEstado;
import com.udea.skillbridge.enums.EvaluacionFase;
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.mapper.IDetalleRespuestaMapper;
import com.udea.skillbridge.mapper.IEvaluacionEstudianteMapper;
import com.udea.skillbridge.mapper.IPlanFortalecimientoMapper;
import com.udea.skillbridge.repository.ICuestionarioRepository;
import com.udea.skillbridge.repository.IDetalleRespuestaRepository;
import com.udea.skillbridge.repository.IEvaluacionEstudianteRepository;
import com.udea.skillbridge.repository.IPreguntaRepository;
import com.udea.skillbridge.repository.IPuntuacionResultadoRepository;
import com.udea.skillbridge.service.IEvaluacionEstudianteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluacionEstudianteServiceImpl implements IEvaluacionEstudianteService {
	
	private final ICuestionarioRepository cuestionarioRepository;
	private final IEvaluacionEstudianteRepository evaluacionRepository;
	private final IEvaluacionEstudianteMapper evaluacionMapper;
	private final IPreguntaRepository preguntaRepository;
	private final IDetalleRespuestaRepository detalleRespuestaRepository;
	private final IDetalleRespuestaMapper detalleRespuestaMapper;
	private final MotorDePuntuacion motorPuntuacion;
	private final IPuntuacionResultadoRepository puntuacionResultadoRepository;
	private final IPlanFortalecimientoMapper planMapper;

	// *****************************************
    //  INICIAR SESIÓN
    // *****************************************
	
	@Override
	public EvaluacionEstudianteResponse iniciar(Long idCuestionario, IniciarEvaluacionRequest request) {
		// 1. Cuestionario debe estar PUBLISHED
        CuestionarioEntity cuestionarioEnt = cuestionarioRepository
                .findActivoById(idCuestionario)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cuestionario", idCuestionario
                ));

        if (!EstadoCuestionario.PUBLICADO.equals(cuestionarioEnt.getEstadoCuestionario())) {
            throw new BusinessException(
                "Solo se puede responder un cuestionario PUBLICADO. " +
                "Estado actual: " + cuestionarioEnt.getEstadoCuestionario(),
                "QUESTIONNAIRE_NOT_PUBLISHED"
            );
        }

        // 1.b Validar la ventana de disponibilidad (fecha/hora)
        LocalDateTime ahora = LocalDateTime.now();
        if (!cuestionarioEnt.ventanaVigente(ahora)) {
            boolean aunNoAbre = cuestionarioEnt.getFechaInicio() != null
                    && ahora.isBefore(cuestionarioEnt.getFechaInicio());
            throw new BusinessException(
                aunNoAbre
                    ? "El cuestionario aún no está disponible para responder."
                    : "El período para responder este cuestionario ya finalizó.",
                "OUT_OF_WINDOW"
            );
        }

        // 2. Verificar que no haya sesión IN_PROGRESS para este estudiante
        evaluacionRepository.findByIdEstudianteAndCuestionarioEntIdCuestionarioAndEstado(
                request.getIdEstudiante(),
                idCuestionario,
                EvaluacionEstado.EN_PROGRESO).ifPresent(existing -> {
            throw new BusinessException(
                "El/La estudiante ya tiene una sesión en progreso para este cuestionario. " +
                "Complétala antes de iniciar una nueva. Sesión activa id=" + existing.getId(),
                "SESSION_ALREADY_IN_PROGRESS"
            );
        });

        // 3. Para POST_TEST: debe existir un PRE_TEST completado
        if (EvaluacionFase.POST_TEST.equals(request.getEvaluacionFase())) {
            boolean preTestHecho = evaluacionRepository
                .existsByIdEstudianteAndCuestionarioEntIdCuestionarioAndEvaluacionFaseAndEstado(
                    request.getIdEstudiante(),
                    idCuestionario,
                    EvaluacionFase.PRE_TEST,
                    EvaluacionEstado.COMPLETADO
                );

            if (!preTestHecho) {
                throw new BusinessException(
                    "Debe completar el PRE_TEST antes de iniciar el POST_TEST.",
                    "PRE_TEST_NOT_COMPLETED"
                );
            }
        }

        // 4. Calcular número de intento
        int numeroIntentos = evaluacionRepository
                .findTopByIdEstudianteAndCuestionarioEntIdCuestionarioAndEvaluacionFaseOrderByNumeroIntentoDesc(
                    request.getIdEstudiante(), idCuestionario, request.getEvaluacionFase())
                .map(prev -> prev.getNumeroIntento() + 1)
                .orElse(1);

        // 5. Crear la sesión
        EvaluacionEstudianteEntity evaluacion = EvaluacionEstudianteEntity.builder()
                .idEstudiante(request.getIdEstudiante())
                .cuestionarioEnt(cuestionarioEnt)
                .evaluacionFase(request.getEvaluacionFase())
                .estado(EvaluacionEstado.EN_PROGRESO)
                .numeroIntento(numeroIntentos)
                .build();

        EvaluacionEstudianteEntity guardar = evaluacionRepository.save(evaluacion);
        log.info("Sesión iniciada → estudiante={} cuestionario={} fase={} intento={}",
                request.getIdEstudiante(), 
                idCuestionario,
                request.getEvaluacionFase(), numeroIntentos);

        return evaluacionMapper.toResponse(guardar);
	}
	
	// *****************************************
    //  GUARDAR RESPUESTA
    // *****************************************
	
	@Override
	public DetalleRespuestaResponse enviarRespuesta(Long idEvaluacion, EnviarRespuestaRequest request) {
		EvaluacionEstudianteEntity evaluacion = findActivoById(idEvaluacion);
		validarEditable(evaluacion);

        PreguntaEntity pregunta = preguntaRepository.findById(request.getIdPregunta())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Pregunta", request.getIdPregunta()
                ));

        // REGLA: la pregunta debe pertenecer al cuestionario de la sesión
        validarPreguntaPerteneceToCuestionario(pregunta, evaluacion.getCuestionarioEnt());

        // REGLA: no se puede responder dos veces la misma pregunta
        if (detalleRespuestaRepository.existsByEvaluacionEntIdAndPreguntaEntIdPregunta(
        		idEvaluacion, request.getIdPregunta())) {
            throw new BusinessException(
                "La pregunta " + request.getIdPregunta() +
                " ya fue respondida en esta sesión. Use el endpoint de actualización.",
                "ANSWER_ALREADY_EXISTS"
            );
        }

        // REGLA: validar opciones según tipo de pregunta
        validarRespuestaByTipo(pregunta, request);

        // Construir la respuesta
        DetalleRespuestaEntity respuesta = DetalleRespuestaEntity.builder()
                .evaluacionEnt(evaluacion)
                .preguntaEnt(pregunta)
                .respuestaAbierta(request.getRespuestaAbierta())
                .build();

        if (request.getIdsOpcionesSeleccionadas() != null) {
            respuesta.getIdsOpcionesSeleccionadas().addAll(request.getIdsOpcionesSeleccionadas());
        }

        DetalleRespuestaEntity guardar = detalleRespuestaRepository.save(respuesta);
        log.info("Respuesta guardada → sesión={} pregunta={}",
                idEvaluacion, pregunta.getIdPregunta());

        return detalleRespuestaMapper.toResponse(guardar);
	}
	
	// *********************************************
    //  ACTUALIZAR RESPUESTA (MIENTRAS EN_PROGRESO)
    // *********************************************
	
	@Override
	public DetalleRespuestaResponse actualizarRespuesta(Long idEvaluacion, Long idPregunta,
			EnviarRespuestaRequest request) {
		
		EvaluacionEstudianteEntity evaluacion = findActivoById(idEvaluacion);
        validarEditable(evaluacion);

        DetalleRespuestaEntity respuesta = detalleRespuestaRepository
                .findByEvaluacionEntIdAndPreguntaEntIdPregunta(idEvaluacion, idPregunta)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Respuesta para pregunta " + idPregunta + " en sesión " + idEvaluacion
                ));

        validarRespuestaByTipo(respuesta.getPreguntaEnt(), request);

        respuesta.getIdsOpcionesSeleccionadas().clear();
        if (request.getIdsOpcionesSeleccionadas() != null) {
            respuesta.getIdsOpcionesSeleccionadas().addAll(request.getIdsOpcionesSeleccionadas());
        }
        respuesta.setRespuestaAbierta(request.getRespuestaAbierta());

        return detalleRespuestaMapper.toResponse(detalleRespuestaRepository.save(respuesta));
	}
	
	// *********************************************
    //  COPLETAR SESIÓN Y CALCULAR PUNTAJES
    // *********************************************
	
	@Override
	public InformeEvaluacionResponse completo(Long idEvaluacion) {
		EvaluacionEstudianteEntity evaluacion = findActivoById(idEvaluacion);
        validarEditable(evaluacion);

        // REGLA: todas las preguntas obligatorias deben estar respondidas
        validarPreguntasObligatoriasRespondidas(evaluacion);

        // Obtener todas las respuestas de la sesión
        List<DetalleRespuestaEntity> respuesta = detalleRespuestaRepository.findByEvaluacionEntId(idEvaluacion);

        // Calcular scores con el motor
        List<PuntuacionResultadoEntity> resultados = motorPuntuacion.calcular(evaluacion, respuesta);

        // Persistir los ResultScore
        List<PuntuacionResultadoEntity> guardarResultados = puntuacionResultadoRepository.saveAll(resultados);

        // Actualizar estado de la sesión
        evaluacion.setEstado(EvaluacionEstado.COMPLETADO);
        evaluacion.setFinishedAt(LocalDateTime.now());
        evaluacionRepository.save(evaluacion);

        log.info("Sesión [{}] completada. [{}] resultados calculados",
                idEvaluacion, guardarResultados.size());

        // Construir el reporte completo
        return construirReporte(evaluacion, guardarResultados);
	}
	
	// *****************************************
    //  CONSULTAR SESIÓN
    // *****************************************
	
	@Override
	public EvaluacionEstudianteResponse findById(Long idEvaluacion) {
		return evaluacionMapper.toResponse(findActivoById(idEvaluacion));
	}
	
	// *****************************************
    //  HISTORIAL DE SESIONES DE UN ESTUDIANTE
    // *****************************************
	
	@Override
	public List<EvaluacionEstudianteResponse> findByEstudiante(Long idEstudiante, Long idCuestionario) {
        return evaluacionRepository.findByIdEstudianteAndCuestionarioEntIdCuestionario(idEstudiante, idCuestionario)
                .stream()
                .map(evaluacionMapper::toResponse)
                .toList();
	}
	
	// *****************************************
    //  OBTENER REPORTE DE SESION COMPLETADA
    // *****************************************
	
	@Override
	public InformeEvaluacionResponse getReporte(Long idEvaluacion) {
        EvaluacionEstudianteEntity evaluacionEnt = findActivoById(idEvaluacion);

        if (!EvaluacionEstado.COMPLETADO.equals(evaluacionEnt.getEstado())) {
            throw new BusinessException(
                "El reporte solo está disponible para sesiones COMPLETADAS. " +
                "Estado actual: " + evaluacionEnt.getEstado(),
                "ASSESSMENT_NOT_COMPLETED"
            );
        }

        List<PuntuacionResultadoEntity> resultadoEnt = puntuacionResultadoRepository.findByEvaluacionEntId(idEvaluacion);
        return construirReporte(evaluacionEnt, resultadoEnt);
	}
	
	// *****************************************
    //  METODOS PRIVADOS
    // *****************************************
	
    private EvaluacionEstudianteEntity findActivoById(Long id) {
        return evaluacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvaluacionEstudianteEntity", id));
    }
    
    private void validarEditable(EvaluacionEstudianteEntity evaluacion) {
        if (!evaluacion.isEditable()) {
            throw new BusinessException(
                "La sesión no está activa. Estado: " + evaluacion.getEstado(),
                "ASSESSMENT_NOT_EDITABLE"
            );
        }
    }
    
    private void validarPreguntaPerteneceToCuestionario(PreguntaEntity pregunta,
    		CuestionarioEntity cuestionario) {
    	boolean pertenece = cuestionario.getPreguntasCuestionario().stream()
    			.anyMatch(qq -> qq.getPreguntaEnt().getIdPregunta().equals(pregunta.getIdPregunta()));

    	if (!pertenece) {
    		throw new BusinessException(
    				"La pregunta " + pregunta.getIdPregunta() +
    				" no pertenece al cuestionario de esta sesión.",
    				"QUESTION_NOT_IN_QUESTIONNAIRE");
    	}
    }
    
    private void validarRespuestaByTipo(PreguntaEntity pregunta, EnviarRespuestaRequest request) {
        switch (pregunta.getTipoPregunta()) {
            case DESCRIPCION -> {
                // DESCRIPCION: solo acepta texto abierto, sin opciones
                if (request.getIdsOpcionesSeleccionadas() != null &&
                        !request.getIdsOpcionesSeleccionadas().isEmpty()) {
                    throw new BusinessException(
                        "Las preguntas DESCRIPTION no aceptan opciones seleccionadas.",
                        "INVALID_ANSWER_FOR_TYPE"
                    );
                }
            }
            case OPCION_UNICA, VERDADERO_FALSO -> {
                // Solo 1 opción
                if (request.getIdsOpcionesSeleccionadas() == null ||
                        request.getIdsOpcionesSeleccionadas().size() != 1) {
                    throw new BusinessException(
                        "Las preguntas " + pregunta.getTipoPregunta() +
                        " requieren exactamente 1 opción seleccionada.",
                        "INVALID_ANSWER_FOR_TYPE"
                    );
                }
                validarExistenOpciones(pregunta, request.getIdsOpcionesSeleccionadas());
            }
            case LIKERT -> {
                // Exactamente 1 opción
                if (request.getIdsOpcionesSeleccionadas() == null ||
                        request.getIdsOpcionesSeleccionadas().size() != 1) {
                    throw new BusinessException(
                        "Las preguntas LIKERT requieren exactamente 1 opción.",
                        "INVALID_ANSWER_FOR_TYPE"
                    );
                }
                validarExistenOpciones(pregunta, request.getIdsOpcionesSeleccionadas());
            }
            case OPCION_MULTIPLE -> {
                if (request.getIdsOpcionesSeleccionadas() == null ||
                        request.getIdsOpcionesSeleccionadas().isEmpty()) {
                    throw new BusinessException(
                        "Debe seleccionar al menos 1 opción para preguntas MULTIPLE_CHOICE.",
                        "INVALID_ANSWER_FOR_TYPE"
                    );
                }
                // Respetar el límite maxOptions si está definido
                if (pregunta.getMaxOpciones() != null &&
                        request.getIdsOpcionesSeleccionadas().size() > pregunta.getMaxOpciones()) {
                    throw new BusinessException(
                        "Máximo " + pregunta.getMaxOpciones() + " opciones permitidas. " +
                        "Se enviaron: " + request.getIdsOpcionesSeleccionadas().size(),
                        "MAX_OPTIONS_EXCEEDED"
                    );
                }
                validarExistenOpciones(pregunta, request.getIdsOpcionesSeleccionadas());
            }
        }
    }
    
    private void validarExistenOpciones(PreguntaEntity pregunta, List<Long> idsSeleccionado) {
        List<Long> idsOpcionesValidas = pregunta.getOpcionPregunta().stream()
                .map(OpcionPreguntaEntity::getId)
                .toList();

        List<Long> invalidIds = idsSeleccionado.stream()
                .filter(id -> !idsOpcionesValidas.contains(id))
                .toList();

        if (!invalidIds.isEmpty()) {
            throw new BusinessException(
                "Las siguientes opciones no pertenecen a la pregunta: " + invalidIds,
                "INVALID_OPTION_IDS"
            );
        }
    }
    
    private void validarPreguntasObligatoriasRespondidas(EvaluacionEstudianteEntity evaluacionEnt) {
        List<Long> idsPreguntaObligatoria = evaluacionEnt.getCuestionarioEnt()
                .getPreguntasCuestionario().stream()
                .filter(qq -> Boolean.TRUE.equals(qq.getObligatoria())
                           && !Boolean.TRUE.equals(qq.getIsCondicional()))
                .map(qq -> qq.getPreguntaEnt().getIdPregunta())
                .toList();

        List<Long> idsPreguntaRespondida = detalleRespuestaRepository
                .findByEvaluacionEntId(evaluacionEnt.getId())
                .stream()
                .map(a -> a.getPreguntaEnt().getIdPregunta())
                .toList();

        List<Long> sinRespuesta = idsPreguntaObligatoria.stream()
                .filter(id -> !idsPreguntaRespondida.contains(id))
                .toList();

        if (!sinRespuesta.isEmpty()) {
            throw new BusinessException(
                "Hay " + sinRespuesta.size() + " pregunta(s) obligatoria(s) sin responder: "
                + sinRespuesta,
                "MANDATORY_QUESTIONS_UNANSWERED"
            );
        }
    }
    
    private InformeEvaluacionResponse construirReporte(EvaluacionEstudianteEntity evaluacionEnt,
    		List<PuntuacionResultadoEntity> resultados) {
    	
    	List<PuntuacionResultadoResponse> resultadoResponses = resultados.stream()
    			.map(r -> PuntuacionResultadoResponse.builder()
    					.id(r.getId())
    					.skill(r.getSkill())
    					.dimension(r.getDimension())
    					.totalPuntaje(r.getTotalPuntaje())
    					.maxPuntuacionPosible(r.getMaxPuntuacionPosible())
    					.porcentajePuntuacion(r.getPorcentajePuntuacion())
    					.nivel(r.getNivel())
    					.descripcionNivel(r.getPuntuacionMatrizEnt() != null 
    						? r.getPuntuacionMatrizEnt().getDescripcion() : null)
    					.caracteristicasObservables(r.getPuntuacionMatrizEnt() != null
							? r.getPuntuacionMatrizEnt().getCaracteristicasObservables() : null)
    					.planesAsignados(r.getPuntuacionMatrizEnt() != null 
    						? r.getPuntuacionMatrizEnt().getPlanFortalecimientoEnt().stream()
    								.map(planMapper::toResponse)
    								.toList() : List.of())
    					.calculatedAt(r.getCalculatedAt())
    					.build()
    				)
    				.toList();

    	return InformeEvaluacionResponse.builder()
    			.idEvaluacion(evaluacionEnt.getId())
    			.idEstudiante(evaluacionEnt.getIdEstudiante())
    			.nombreCuestionario(evaluacionEnt.getCuestionarioEnt().getNombre())
    			.evaluacionFase(evaluacionEnt.getEvaluacionFase())
    			.numeroIntento(evaluacionEnt.getNumeroIntento())
    			.startedAt(evaluacionEnt.getStartedAt())
    			.finishedAt(evaluacionEnt.getFinishedAt())
    			.resultados(resultadoResponses)
    			.resumenGeneral(construirResumen(resultadoResponses))
    			.build();
    }
    
    private String construirResumen(List<PuntuacionResultadoResponse> resultados) {
        if (resultados.isEmpty()) return "No se calcularon resultados para esta sesión.";

        long avanzado = resultados.stream()
                .filter(r -> SkillNivel.AVANZADO.equals(r.getNivel())).count();
        long intermedio = resultados.stream()
                .filter(r -> SkillNivel.INTERMEDIO.equals(r.getNivel())).count();
        long bajo = resultados.stream()
                .filter(r -> SkillNivel.BAJO.equals(r.getNivel())).count();

        return String.format(
            "Resultados: %d dimensión(es) en nivel AVANZADO, " +
            "%d en INTERMEDIO, %d en BAJO.",
            avanzado, intermedio, bajo
        );
    }
}
