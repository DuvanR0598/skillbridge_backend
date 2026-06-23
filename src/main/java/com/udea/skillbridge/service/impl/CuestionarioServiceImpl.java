 package com.udea.skillbridge.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.ActualizarCuestionarioRequest;
import com.udea.skillbridge.dto.request.CuestionarioRequest;
import com.udea.skillbridge.dto.request.PreguntaCuestionarioRequest;
import com.udea.skillbridge.dto.response.ActivarCondicionPreguntaResponse;
import com.udea.skillbridge.dto.response.CuestionarioEntregaResponse;
import com.udea.skillbridge.dto.response.CuestionarioResponse;
import com.udea.skillbridge.dto.response.DimensionResponse;
import com.udea.skillbridge.dto.response.OpcionPreguntaAdminResponse;
import com.udea.skillbridge.dto.response.OpcionPreguntaResponse;
import com.udea.skillbridge.dto.response.PreguntaDeCuestionarioResponse;
import com.udea.skillbridge.dto.response.PreguntaEntregaResponse;
import com.udea.skillbridge.entity.CondicionPreguntaEntity;
import com.udea.skillbridge.entity.CuestionarioEntity;
import com.udea.skillbridge.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.entity.PreguntaCuestionarioEntity;
import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.enums.EstadoCuestionario;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.mapper.ICuestionarioMapper;
import com.udea.skillbridge.repository.ICondicionPreguntaRepository;
import com.udea.skillbridge.repository.ICuestionarioRepository;
import com.udea.skillbridge.repository.IPreguntaCuestionarioRepository;
import com.udea.skillbridge.repository.IPreguntaRepository;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.seguridad.repository.IUsuarioPerfilRepository;
import com.udea.skillbridge.service.ICuestionarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuestionarioServiceImpl implements ICuestionarioService{
	
	private final ICuestionarioRepository cuestionarioRepository;
	private final ICuestionarioMapper cuestionarioMapper;
	private final IPreguntaRepository preguntaRepository;
	private final IPreguntaCuestionarioRepository pqRepository;
	private final ICondicionPreguntaRepository condicionPreguntaRepository;
	private final IUsuarioPerfilRepository usuarioPerfilRepository;

	// *****************************************
	// CREAR CUESTIONARIO
	// *****************************************
	
	@Override
	public CuestionarioResponse crearCuestionario(CuestionarioRequest cuestionarioRequest, String creadoPor) {
		log.info("Creando cuestionario: {} (por {})", cuestionarioRequest.getNombre(), creadoPor);
		CuestionarioEntity cuestionarioEnt = cuestionarioMapper.toEntity(cuestionarioRequest);
		cuestionarioEnt.setCreadoPor(creadoPor);
		return cuestionarioMapper.toResponse(cuestionarioRepository.save(cuestionarioEnt));
	}

	// *****************************************
	// DUPLICAR CUESTIONARIO
	// *****************************************

	@Override
	@Transactional
	public CuestionarioResponse duplicarCuestionario(Long idCuestionario, String creadoPor) {
		// 1. Buscar el cuestionario origen (cualquier estado salvo ELIMINADO)
		CuestionarioEntity origen = findActivoById(idCuestionario);

		// 2. Crear la copia con la misma configuración, pero en BORRADOR,
		//    con id nuevo y nombre único con sufijo "COPIA".
		CuestionarioEntity copia = CuestionarioEntity.builder()
				.nombre(generarNombreCopia(origen.getNombre()))
				.objetivo(origen.getObjetivo())
				.instrucciones(origen.getInstrucciones())
				.estadoCuestionario(EstadoCuestionario.BORRADOR)
				.ordenAleatorio(origen.getOrdenAleatorio())
				.fechaInicio(origen.getFechaInicio())
				.fechaFin(origen.getFechaFin())
				.tiempoLimiteMinutos(origen.getTiempoLimiteMinutos())
				.creadoPor(creadoPor)
				.build();

		CuestionarioEntity copiaGuardada = cuestionarioRepository.save(copia);
		Long nuevoId = copiaGuardada.getIdCuestionario();

		// 3. Copiar las preguntas asociadas (tabla intermedia). Las preguntas
		//    del banco se comparten; solo se duplica la relación con el cuestionario.
		List<PreguntaCuestionarioEntity> nuevasRelaciones = origen.getPreguntasCuestionario().stream()
				.map(pq -> PreguntaCuestionarioEntity.builder()
						.id(new PreguntaCuestionarioEntity.IdPreguntaCuestionario(
								nuevoId, pq.getPreguntaEnt().getIdPregunta()))
						.cuestionarioEnt(copiaGuardada)
						.preguntaEnt(pq.getPreguntaEnt())
						.obligatoria(pq.getObligatoria())
						.peso(pq.getPeso())
						.isCondicional(pq.getIsCondicional())
						.build())
				.toList();
		pqRepository.saveAll(nuevasRelaciones);

		// 4. Copiar las condiciones de ramificación. Trigger/target/opción
		//    apuntan a las mismas preguntas/opciones (compartidas); solo cambia
		//    el cuestionario al que pertenece la condición.
		List<CondicionPreguntaEntity> condicionesOrigen =
				condicionPreguntaRepository.findByCuestionarioEntIdCuestionario(idCuestionario);

		if (!condicionesOrigen.isEmpty()) {
			List<CondicionPreguntaEntity> nuevasCondiciones = condicionesOrigen.stream()
					.map(c -> CondicionPreguntaEntity.builder()
							.cuestionarioEnt(copiaGuardada)
							.triggerPregunta(c.getTriggerPregunta())
							.triggerOpcion(c.getTriggerOpcion())
							.targetPregunta(c.getTargetPregunta())
							.build())
					.toList();
			condicionPreguntaRepository.saveAll(nuevasCondiciones);
		}

		log.info("Cuestionario [{}] duplicado como [{}] ({} preguntas, {} condiciones) por {}",
				idCuestionario, nuevoId, nuevasRelaciones.size(), condicionesOrigen.size(), creadoPor);

		// 5. Devolver la copia recién creada (se re-lee para calcular totalPreguntas).
		return findById(nuevoId);
	}

	// *****************************************
	// LISTAR
	// *****************************************
	
	@Override
	public CuestionarioResponse findById(Long idCuestionario) {
		return cuestionarioMapper.toResponse(findActivoById(idCuestionario));
	}
	
	@Override
	public List<CuestionarioResponse> listarAllCuestionarios() {
		return cuestionarioRepository.findAll()
				.stream()
				.map(cuestionarioMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<CuestionarioResponse> listarCuestionariosActivos(UsuarioEntity usuario) {
		List<CuestionarioEntity> activos = cuestionarioRepository.findAllActivos();

		// Para ADMIN/COORDINADOR: ven todos los cuestionarios (los gestionan).
		boolean esGestor = usuario != null
				&& (usuario.hasRole(TipoRol.ROLE_ADMIN) || usuario.hasRole(TipoRol.ROLE_COORDINADOR));

		if (!esGestor && usuario != null) {
			// Para un ESTUDIANTE: solo los generales (programaObjetivo null) o los
			// dirigidos a SU programa académico.
			ProgramaIngenieria programaEstudiante = usuarioPerfilRepository
					.findByUsuarioEntId(usuario.getId())
					.map(p -> p.getProgramaIngenieria())
					.orElse(null);

			activos = activos.stream()
					.filter(c -> c.getProgramaObjetivo() == null
							|| c.getProgramaObjetivo().equals(programaEstudiante))
					.toList();
		}

		return activos.stream()
				.map(cuestionarioMapper::toResponse)
				.toList();
	}
	
	// *****************************************
	// BORRADO LOGICO
	// *****************************************

	@Override
	public void borradoLogico(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

        // REGLA: No se puede borrar si ya tiene respuestas
        if (cuestionarioRepository.hasResponses(idCuestionario)) {
            throw new BusinessException(
                "No se puede eliminar. El cuestionario ya tiene respuestas registradas. " +
                "Considere archivarlo en su lugar.", "HAS_RESPONSES"
            );
        }

        cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.ELIMINADO);
        cuestionarioRepository.save(cuestionarioEnt);
        log.info("Cuestionario con uuid [{}] borrado lógicamente", idCuestionario); 
		
	}
	
	// *****************************************
	// ACTUALIZAR CUESTIONARIO
	// *****************************************
	
	@Override
	public CuestionarioResponse actualizarCuestionario(Long id, ActualizarCuestionarioRequest request) {
		CuestionarioEntity cuestionarioEnt = findActivoById(id);
		// Solo editable en BORRADOR
		validarEditable(cuestionarioEnt, "modificar la configuración de");
		cuestionarioMapper.actualizarCuestionarioRequest(cuestionarioEnt, request);
		// El programa objetivo se aplica siempre (incluido null = general), ya que
		// el mapper de patch ignora los nulls y aquí sí queremos poder limpiarlo.
		cuestionarioEnt.setProgramaObjetivo(request.getProgramaObjetivo());
	    return cuestionarioMapper.toResponse(cuestionarioRepository.save(cuestionarioEnt));
	}
	
	// ***********************************************
	// COMPLETAR CUESTIONARIO (MARCAR COMO COMPLETO)
	// ***********************************************
	
	@Override
	public CuestionarioResponse cuestionarioCompleto(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

        if (!EstadoCuestionario.BORRADOR.equals(cuestionarioEnt.getEstadoCuestionario())) {
            throw new BusinessException("Solo los cuestionarios en BORRADOR pueden completarse. Estado actual: "
                    + cuestionarioEnt.getEstadoCuestionario(), "INVALID_STATUS_TRANSITION");
        }

        // REGLA: Mínimo 2 preguntas
        int count = pqRepository.countByIdIdCuestionario(idCuestionario);
        if (count < 2) {
            throw new BusinessException(
                "El cuestionario debe tener al menos 2 preguntas. Actualmente tiene: " + count, "INSUFFICIENT_QUESTIONS"
            );
        }

        cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.COMPLETO);
        return cuestionarioMapper.toResponse(cuestionarioRepository.save(cuestionarioEnt));
	}
	
	// ***********************************************
	// PUBLICAR CUESTIONARIO (MARCAR COMO PUBLICADO)
	// ***********************************************
	
	/**
	 * Publica el cuestionario: COMPLETO → PUBLICADO.
	 *
	 * REGLA: Solo un cuestionario en estado COMPLETO puede publicarse.
	 * No se puede publicar un DRAFT porque podría tener menos de 2 preguntas
	 * o no tener matriz de valoración configurada.
	 */
	
	@Override
	public CuestionarioResponse cuestionarioPublicado(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

	    if (!EstadoCuestionario.COMPLETO.equals(cuestionarioEnt.getEstadoCuestionario())) {
	        throw new BusinessException(
	        		"Solo los cuestionarios COMPLETADOS pueden publicarse. Estado actual: "
	                        + cuestionarioEnt.getEstadoCuestionario(), "INVALID_STATUS_TRANSITION");
	    }

	    cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.PUBLICADO);
	    return cuestionarioMapper.toResponse(cuestionarioRepository.save(cuestionarioEnt));
	}
	
	// ***********************************************
	// ARCHIVAR CUESTIONARIO (MARCAR COMO ARCHIVADO)
	// ***********************************************
	
	/**
	 * Archiva el cuestionario: PUBLICADO → ARCHIVADO.
	 *
	 * REGLA: Archivar conserva toda la trazabilidad estadística.
	 * Se usa cuando el cuestionario ya no está activo pero tuvo respuestas.
	 * Es la alternativa al borrado lógico cuando ya hay datos de estudiantes.
	 */
	@Override
	public CuestionarioResponse cuestionarioArchivado(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

	    if (!EstadoCuestionario.PUBLICADO.equals(cuestionarioEnt.getEstadoCuestionario())) {
	        throw new BusinessException(
	        		"Solo los cuestionarios PUBLICADOS pueden archivarse. Estado actual: "
	                        + cuestionarioEnt.getEstadoCuestionario(), "INVALID_STATUS_TRANSITION");
	    }

	    cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.ARCHIVADO);
	    return cuestionarioMapper.toResponse(cuestionarioRepository.save(cuestionarioEnt));
	}
	
	// *****************************************
	// AGREGAR PREGUNTA AL CUESTIONARIO
	// *****************************************
	
	@Override
	public void addPretuntaToCuestinario(Long idCuestionario, PreguntaCuestionarioRequest request) {
		
		// 1. Buscar el cuestionario (lanza 404 si no existe o está borrado)
        CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

        // 2. REGLA DE NEGOCIO: solo se pueden agregar preguntas en estado BORRADOR
        validarEditable(cuestionarioEnt, "agregar pregunta a");

        // 3. Verificar que la pregunta existe
        PreguntaEntity preguntaEnt = preguntaRepository.findById(request.getIdpregunta())
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta", request.getIdpregunta()));

        // 4. Verificar que la pregunta no esté ya en el cuestionario
        if (pqRepository.existsByIdIdCuestionarioAndIdIdPregunta(idCuestionario, request.getIdpregunta())) {
            throw new BusinessException("La pregunta " + request.getIdpregunta() + " ya está en el cuestionario.",
                    "QUESTION_ALREADY_ADDED");
        }

        // 5. Crear la relación
        PreguntaCuestionarioEntity.IdPreguntaCuestionario pqId =
                new PreguntaCuestionarioEntity.IdPreguntaCuestionario(idCuestionario, request.getIdpregunta());

        PreguntaCuestionarioEntity pqEnt = PreguntaCuestionarioEntity.builder()
                .id(pqId)
                .cuestionarioEnt(cuestionarioEnt)
                .preguntaEnt(preguntaEnt)
                .obligatoria(request.getObligatoria())
                .peso(request.getPeso())
                .build();

        pqRepository.save(pqEnt);
        log.info("Pregunta [{}] agregada al cuestionario [{}]", preguntaEnt.getIdPregunta(), idCuestionario);
	}

	// *****************************************
	// LISTAR PREGUNTAS DE UN CUESTIONARIO (vista coordinador)
	// *****************************************

	@Override
	@Transactional(readOnly = true)
	public List<PreguntaDeCuestionarioResponse> getPreguntasDeCuestionario(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

		return cuestionarioEnt.getPreguntasCuestionario().stream()
				.map(this::buildPreguntaDeCuestionario)
				.toList();
	}

	private PreguntaDeCuestionarioResponse buildPreguntaDeCuestionario(PreguntaCuestionarioEntity pq) {
		PreguntaEntity p = pq.getPreguntaEnt();

		List<OpcionPreguntaAdminResponse> opciones = p.getOpcionPregunta().stream()
				.map(o -> OpcionPreguntaAdminResponse.builder()
						.idOpcion(o.getId())
						.texto(o.getTexto())
						.peso(o.getPeso())
						.ordenVisualizacion(o.getOrdenVisualizacion())
						.build())
				.toList();

		DimensionResponse dimension = p.getDimension() == null ? null
				: DimensionResponse.builder()
						.id(p.getDimension().getId())
						.nombre(p.getDimension().getNombre())
						.descripcion(p.getDimension().getDescripcion())
						.skill(p.getDimension().getSkill())
						.createdAt(p.getDimension().getCreatedAt())
						.build();

		return PreguntaDeCuestionarioResponse.builder()
				.idPregunta(p.getIdPregunta())
				.texto(p.getTexto())
				.tipoPregunta(p.getTipoPregunta())
				.imagenUrl(p.getImagenUrl())
				.ayuda(p.getAyuda())
				.maxOpciones(p.getMaxOpciones())
				.obligatoria(pq.getObligatoria())
				.peso(pq.getPeso())
				.isCondicional(pq.getIsCondicional())
				.dimension(dimension)
				.opciones(opciones)
				.build();
	}

	// *****************************************
	// QUITAR PREGUNTA DE UN CUESTIONARIO
	// *****************************************

	@Override
	@Transactional
	public void removerPreguntaDeCuestionario(Long idCuestionario, Long idPregunta) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

		// Solo se pueden quitar preguntas en estado BORRADOR
		validarEditable(cuestionarioEnt, "quitar pregunta de");

		PreguntaCuestionarioEntity.IdPreguntaCuestionario pqId =
				new PreguntaCuestionarioEntity.IdPreguntaCuestionario(idCuestionario, idPregunta);

		if (!pqRepository.existsById(pqId)) {
			throw new ResourceNotFoundException("PreguntaCuestionario", idPregunta);
		}

		pqRepository.deleteById(pqId);
		log.info("Pregunta [{}] removida del cuestionario [{}]", idPregunta, idCuestionario);
	}

	// *****************************************
	// ENTREGAR CUESTIONARIO AL ESTUDIANTE
	// *****************************************

	// Aplica aleatorización si el cuestionario la tiene habilitada.
	@Override
	public CuestionarioEntregaResponse entregarCuestionario(Long idCuestionario) {
		// 1. Buscar el cuestionario
        CuestionarioEntity cuestionarioEnt = cuestionarioRepository
                .findActivoById(idCuestionario)
                .orElseThrow(() -> new CuestionarioException(
                    "Cuestionario no encontrado: " + idCuestionario, HttpStatus.NOT_FOUND
                ));

        // 2. Solo se pueden responder cuestionarios en estado PUBLICADO
        if (!EstadoCuestionario.PUBLICADO.equals(cuestionarioEnt.getEstadoCuestionario())) {
            throw new CuestionarioException(
                "El cuestionario no está disponible para responder. " +
                "Estado actual: " + cuestionarioEnt.getEstadoCuestionario()
            );
        }

        // Nota: la ventana de disponibilidad se valida al INICIAR la evaluación,
        // no aquí, para no romper una sesión en curso si la ventana cierra a mitad.

        // 3. Obtener las preguntas del cuestionario
        List<PreguntaCuestionarioEntity> pqListaEnt =
                new ArrayList<>(cuestionarioEnt.getPreguntasCuestionario());

        // 4. Cargar todas las condiciones del cuestionario (fuente de verdad de
        //    la ramificación) y armar el conjunto de preguntas que son TARGET de
        //    alguna condición → esas son las condicionales.
        List<CondicionPreguntaEntity> condiciones =
        		condicionPreguntaRepository.findByCuestionarioEntIdCuestionario(idCuestionario);

        Set<Long> idsPreguntasCondicionales = condiciones.stream()
                .map(c -> c.getTargetPregunta().getIdPregunta())
                .collect(Collectors.toSet());

        // 5. Separar preguntas base (siempre visibles) de las condicionales
        //    (visibles según la respuesta que las dispara).
        List<PreguntaCuestionarioEntity> preguntasBase = pqListaEnt.stream()
                .filter(pq -> !idsPreguntasCondicionales.contains(pq.getPreguntaEnt().getIdPregunta()))
                .collect(Collectors.toList());  // Lista mutable

        List<PreguntaCuestionarioEntity> preguntasCondicionales = pqListaEnt.stream()
                .filter(pq -> idsPreguntasCondicionales.contains(pq.getPreguntaEnt().getIdPregunta()))
                .collect(Collectors.toList());  // Lista mutable

        // 6. Solo las preguntas BASE se aleatorizan
        // Las condicionales no se mezclan porque su visibilidad depende de la respuesta anterior
        if (Boolean.TRUE.equals(cuestionarioEnt.getOrdenAleatorio())) {
            Collections.shuffle(preguntasBase);
        }

        // 7. Construir las preguntas entregadas numeradas desde 1
        // Construir preguntas base 
        AtomicInteger numeroPregunta = new AtomicInteger(1);

        List<PreguntaEntregaResponse> preguntasBaseEntregadas = preguntasBase.stream()
                .map(pq -> buildDeliveredQuestion(
                        pq,
                        numeroPregunta.getAndIncrement(),
                        cuestionarioEnt.getOrdenAleatorio(),
                        condiciones
                ))
                .toList();
        
        // Construir preguntas condicionales (sin número fijo — lo asigna el frontend al activarse)
        List<PreguntaEntregaResponse> preguntasCondicionalesEntregadas = preguntasCondicionales.stream()
                .map(pq -> buildDeliveredQuestion(
                        pq,
                        null,   // sin número: el frontend lo calcula cuando se activa
                        false,  // las condicionales nunca se re-aleatorizan
                        condiciones
                ))
                .toList();

        return CuestionarioEntregaResponse.builder()
                .idCuestionario(cuestionarioEnt.getIdCuestionario())
                .nombre(cuestionarioEnt.getNombre())
                .objetivo(cuestionarioEnt.getObjetivo())
                .instrucciones(cuestionarioEnt.getInstrucciones())
                .ordenAleatorio(cuestionarioEnt.getOrdenAleatorio())
                .tiempoLimiteMinutos(cuestionarioEnt.getTiempoLimiteMinutos())
                .totalPreguntas(preguntasBaseEntregadas.size())      // solo las base son "totales" para el progreso
                .preguntas(preguntasBaseEntregadas)
                .preguntasCondicionales(preguntasCondicionalesEntregadas)
                .build();
	}
	
	// *****************************************
	// METODOS PRIVADOS
	// *****************************************
	
	public CuestionarioEntity findActivoById(Long idCuestionario) {
		return cuestionarioRepository.findActivoById(idCuestionario)
				.orElseThrow(() -> new ResourceNotFoundException("Cuestionario", idCuestionario));
	}

	/**
	 * Genera un nombre único para la copia. Como 'nombre' es único en BD,
	 * si "X COPIA" ya existe se prueba "X COPIA (2)", "X COPIA (3)", etc.
	 */
	private String generarNombreCopia(String nombreOriginal) {
		String base = nombreOriginal + " COPIA";
		if (!cuestionarioRepository.existsByNombre(base)) {
			return base;
		}
		int n = 2;
		while (cuestionarioRepository.existsByNombre(base + " (" + n + ")")) {
			n++;
		}
		return base + " (" + n + ")";
	}
	
	private void validarEditable(CuestionarioEntity cuestionario, String accion) {
        if (!cuestionario.isEditable()) {
            throw new BusinessException(
                "No se puede " + accion + " el cuestionario. " +
                "Solo es posible en estado BORRADOR. Estado actual: " + cuestionario.getEstadoCuestionario(),
                "NOT_EDITABLE"
            );
        }
    }
	
	
	private PreguntaEntregaResponse buildDeliveredQuestion(
            PreguntaCuestionarioEntity pqEnt,
            Integer numeroPregunta,
            Boolean randomOrder,
            List<CondicionPreguntaEntity> allCondiciones) {

        PreguntaEntity preguntaEnt = pqEnt.getPreguntaEnt();

        // Las opciones también se aleatorizan si el flag está activo
        // Evita el sesgo cognitivo de "siempre marco la primera opción".
        // Excepción: LIKERT conserva su orden (escala con significado intrínseco).
        List<OpcionPreguntaResponse> opciones = buildDeliveredOptions(
                preguntaEnt.getOpcionPregunta(), randomOrder, preguntaEnt.getTipoPregunta()
        );
        
        // Filtrar las condiciones que activan ESTA pregunta específica
        List<ActivarCondicionPreguntaResponse> activaciones = allCondiciones.stream()
                .filter(c -> c.getTargetPregunta().getIdPregunta().equals(preguntaEnt.getIdPregunta()))
                .map(c -> ActivarCondicionPreguntaResponse.builder()
                        .idCondicion(c.getId())
                        .triggerIdPregunta(c.getTriggerPregunta().getIdPregunta())
                        .triggerIdOpcion(c.getTriggerOpcion().getId())
                        .build())
                .toList();

        return PreguntaEntregaResponse.builder()
                .idPregunta(preguntaEnt.getIdPregunta())
                .numeroPregunta(numeroPregunta)
                .tipoPregunta(preguntaEnt.getTipoPregunta())
                .texto(preguntaEnt.getTexto())
                .imagenUrl(preguntaEnt.getImagenUrl())
                .ayuda(preguntaEnt.getAyuda())
                .obligatoria(pqEnt.getObligatoria())
                .maxOpciones(preguntaEnt.getMaxOpciones())
                .opciones(opciones)
                .activarCondiciones(activaciones)
                .build();
    }
	
	private List<OpcionPreguntaResponse> buildDeliveredOptions(
            List<OpcionPreguntaEntity> opciones,
            Boolean ordenAleatorio,
            TipoPregunta tipoPregunta) {

        if (opciones == null || opciones.isEmpty()) {
            return Collections.emptyList();
        }

        // Copiamos para no mutar la lista de la entidad
        List<OpcionPreguntaEntity> copiaOpciones = new ArrayList<>(opciones);

        // LIKERT nunca se aleatoriza: la escala tiene un orden con significado
        // (p. ej. "Muy en desacuerdo" → "Muy de acuerdo").
        if (Boolean.TRUE.equals(ordenAleatorio) && !TipoPregunta.LIKERT.equals(tipoPregunta)) {
            Collections.shuffle(copiaOpciones);
        }

        // Reasignamos displayOrder según el orden resultante (1, 2, 3...)
        // para que el frontend renderice consistentemente
        AtomicInteger orden = new AtomicInteger(1);

        return copiaOpciones.stream()
                .map(opt -> OpcionPreguntaResponse.builder()
                        .idOpcion(opt.getId())
                        .texto(opt.getTexto())
                        .ordenVisualizacion(orden.getAndIncrement()) // orden en ESTA entrega
                        .build()
                )
                .toList();
    }

}