package com.udea.skillbridge.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.udea.skillbridge.dto.Cuestionario;
import com.udea.skillbridge.dto.CuestionarioEntregaResponse;
import com.udea.skillbridge.dto.OpcionPreguntaResponse;
import com.udea.skillbridge.dto.PreguntaCuestionario;
import com.udea.skillbridge.dto.PreguntaEntregaResponse;
import com.udea.skillbridge.enums.EstadoCuestionario;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.mapper.ICuestionarioMapper;
import com.udea.skillbridge.persistence.entity.CuestionarioEntity;
import com.udea.skillbridge.persistence.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.persistence.entity.PreguntaCuestionarioEntity;
import com.udea.skillbridge.persistence.entity.PreguntaEntity;
import com.udea.skillbridge.persistence.repository.ICuestionarioRepository;
import com.udea.skillbridge.persistence.repository.IPreguntaCuestionarioRepository;
import com.udea.skillbridge.persistence.repository.IPreguntaRepository;
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

	// *****************************************
	// CREAR CUESTIONARIO
	// *****************************************
	
	@Override
	public Cuestionario crearCuestionario(Cuestionario cuestionario) {
		log.info("Creando cuestionario: {}", cuestionario.getNombre());
		CuestionarioEntity cuestionarioEnt = cuestionarioMapper.toEntity(cuestionario);
		CuestionarioEntity guardarEntity = cuestionarioRepository.save(cuestionarioEnt);
		log.info("Cuestionario creado con ID: {}", guardarEntity.getIdCuestionario());
		return cuestionarioMapper.toDto(guardarEntity);
	}
	
	// *****************************************
	// AGREGAR PREGUNTA AL CUESTIONARIO
	// *****************************************
	
	@Override
	public void addPretuntaToCuestinario(Long idCuestionario, PreguntaCuestionario preguntaCuestionario) {
		
		// 1. Buscar el cuestionario (lanza 404 si no existe o está borrado)
        CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

        // 2. REGLA DE NEGOCIO: solo se pueden agregar preguntas en estado DRAFT
        if (!cuestionarioEnt.isEditable()) {
            throw new CuestionarioException(
                "No se pueden agregar preguntas. El cuestionario no está en estado DRAFT."
            );
        }

        // 3. Verificar que la pregunta existe
        PreguntaEntity preguntaEnt = preguntaRepository.findById(preguntaCuestionario.getIdpregunta())
                .orElseThrow(() -> new CuestionarioException(
                    "Pregunta no encontrada: " + preguntaCuestionario.getIdpregunta(),
                    HttpStatus.NOT_FOUND
                ));

        // 4. Verificar que la pregunta no esté ya en el cuestionario
        if (pqRepository.existsByIdIdCuestionarioAndIdIdPregunta(idCuestionario, preguntaCuestionario.getIdpregunta())) {
            throw new CuestionarioException("La pregunta ya está asociada a este cuestionario.");
        }

        // 5. Crear la relación
        PreguntaCuestionarioEntity.IdPreguntaCuestionario pqId =
                new PreguntaCuestionarioEntity.IdPreguntaCuestionario(idCuestionario, preguntaCuestionario.getIdpregunta());

        PreguntaCuestionarioEntity pqEnt = PreguntaCuestionarioEntity.builder()
                .id(pqId)
                .cuestionarioEnt(cuestionarioEnt)
                .preguntaEnt(preguntaEnt)
                .obligatoria(preguntaCuestionario.getObligatoria())
                .peso(preguntaCuestionario.getPeso())
                .build();

        pqRepository.save(pqEnt);
        log.info("Pregunta {} agregada al cuestionario {}", preguntaEnt.getIdPregunta(), idCuestionario);
	}
	
	// ***********************************************
	// COMPLETAR CUESTIONARIO (MARCAR COMO COMPLETO)
	// ***********************************************
	
	@Override
	public Cuestionario cuestionarioCompleto(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

        if (!EstadoCuestionario.BORRADOR.equals(cuestionarioEnt.getEstadoCuestionario())) {
            throw new CuestionarioException("Solo los cuestionarios en estado BORRADOR pueden completarse.");
        }

        // REGLA: Mínimo 2 preguntas
        int count = pqRepository.countByIdIdCuestionario(idCuestionario);
        if (count < 2) {
            throw new CuestionarioException(
                "El cuestionario debe tener al menos 2 preguntas. Actualmente tiene: " + count + " pregunta"
            );
        }

        cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.COMPLETO);
        CuestionarioEntity guardar = cuestionarioRepository.save(cuestionarioEnt);
        log.info("Cuestionario {} marcado como COMPLETO", idCuestionario);
        return cuestionarioMapper.toDto(guardar);
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
	public Cuestionario cuestionarioPublicado(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

	    if (!EstadoCuestionario.COMPLETO.equals(cuestionarioEnt.getEstadoCuestionario())) {
	        throw new CuestionarioException(
	            "Solo los cuestionarios en estado COMPLETO pueden publicarse. " +
	            "Estado actual: " + cuestionarioEnt.getEstadoCuestionario());
	    }

	    cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.PUBLICADO);
	    CuestionarioEntity guardar = cuestionarioRepository.save(cuestionarioEnt);
	    log.info("Cuestionario [{}] publicado correctamente", idCuestionario);
	    return cuestionarioMapper.toDto(guardar);
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
	public Cuestionario cuestionarioArchivado(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

	    if (!EstadoCuestionario.PUBLICADO.equals(cuestionarioEnt.getEstadoCuestionario())) {
	        throw new CuestionarioException(
	            "Solo los cuestionarios en estado PUBLICADO pueden archivarse. " +
	            "Estado actual: " + cuestionarioEnt.getEstadoCuestionario());
	    }

	    cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.ARCHIVADO);
	    CuestionarioEntity guardar = cuestionarioRepository.save(cuestionarioEnt);
	    log.info("Cuestionario [{}] archivado correctamente", idCuestionario);
	    return cuestionarioMapper.toDto(guardar);
	}
	
	// *****************************************
	// LISTAR
	// *****************************************
	
	@Override
	public Cuestionario getById(Long idCuestionario) {
		Optional<CuestionarioEntity> cuestionarioOptEnt = Optional.of(cuestionarioRepository.findById(idCuestionario).
				orElseThrow(() -> new CuestionarioException(
						"Cuestionario no encontrado: " + idCuestionario, HttpStatus.NOT_FOUND)));
		
		return cuestionarioMapper.toDto(cuestionarioOptEnt.get());
	}
	
	@Override
	public List<Cuestionario> listarAllCuestionarios() {
		return cuestionarioRepository.findAll()
				.stream()
				.map(cuestionarioMapper::toDto)
				.toList();
	}

	@Override
	public List<Cuestionario> listarCuestionariosActivos() {
		return cuestionarioRepository.findAllActivos()
				.stream()
				.map(cuestionarioMapper::toDto)
				.toList();
	}
	
	private CuestionarioEntity findActivoById(Long idCuestionario) {
		return cuestionarioRepository.findActivoById(idCuestionario)
				.orElseThrow(() -> new CuestionarioException(
						"Cuestionario no encontrado: " + idCuestionario, HttpStatus.NOT_FOUND));
	}
	
	// *****************************************
	// BORRADO LOGICO
	// *****************************************

	@Override
	public void borradoLogico(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActivoById(idCuestionario);

        // REGLA: No se puede borrar si ya tiene respuestas
        if (cuestionarioRepository.hasResponses(idCuestionario)) {
            throw new CuestionarioException(
                "No se puede eliminar. El cuestionario ya tiene respuestas registradas. " +
                "Considere archivarlo en su lugar."
            );
        }

        cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.ELIMINADO);
        cuestionarioRepository.save(cuestionarioEnt);
        log.info("Cuestionario con uuid [{}] borrado lógicamente", idCuestionario); 
		
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
                    "Cuestionario no encontrado: " + idCuestionario,
                    HttpStatus.NOT_FOUND
                ));

        // 2. Solo se pueden responder cuestionarios en estado PUBLICADO
        if (!EstadoCuestionario.PUBLICADO.equals(cuestionarioEnt.getEstadoCuestionario())) {
            throw new CuestionarioException(
                "El cuestionario no está disponible para responder. " +
                "Estado actual: " + cuestionarioEnt.getEstadoCuestionario()
            );
        }

        // 3. Obtener las preguntas del cuestionario
        List<PreguntaCuestionarioEntity> pqListaEnt =
                new ArrayList<>(cuestionarioEnt.getPreguntasCuestionario());

        // 4. Aplicar aleatorización si está habilitada
        //    Collections.shuffle() usa SecureRandom internamente — suficiente para este caso
        if (Boolean.TRUE.equals(cuestionarioEnt.getOrdenAleatorio())) {
            Collections.shuffle(pqListaEnt);
            log.debug("Cuestionario {} entregado en orden aleatorio", idCuestionario);
        } else {
            log.debug("Cuestionario {} entregado en orden fijo", idCuestionario);
        }

        // 5. Construir las preguntas entregadas numeradas desde 1
        AtomicInteger numeroPregunta = new AtomicInteger(1);

        List<PreguntaEntregaResponse> preguntasEntregadas = pqListaEnt.stream()
                .map(qq -> buildDeliveredQuestion(
                        qq,
                        numeroPregunta.getAndIncrement(),
                        cuestionarioEnt.getOrdenAleatorio()
                ))
                .toList();

        return CuestionarioEntregaResponse.builder()
                .idCuestinario(cuestionarioEnt.getIdCuestionario())
                .nombre(cuestionarioEnt.getNombre())
                .objetivo(cuestionarioEnt.getObjetivo())
                .ordenAleatorio(cuestionarioEnt.getOrdenAleatorio())
                .totalPreguntas(preguntasEntregadas.size())
                .preguntas(preguntasEntregadas)
                .build();
	}
	
	private PreguntaEntregaResponse buildDeliveredQuestion(
            PreguntaCuestionarioEntity pqEnt,
            int numeroPregunta,
            Boolean randomOrder) {

        PreguntaEntity preguntaEnt = pqEnt.getPreguntaEnt();

        // Las opciones también se aleatorizan si el flag está activo
        // Evita el sesgo cognitivo de "siempre marco la primera opción"
        List<OpcionPreguntaResponse> opciones = buildDeliveredOptions(
                preguntaEnt.getOpcionPregunta(), randomOrder
        );

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
                .build();
    }
	
	private List<OpcionPreguntaResponse> buildDeliveredOptions(
            List<OpcionPreguntaEntity> opciones, 
            Boolean ordenAleatorio) {

        if (opciones == null || opciones.isEmpty()) {
            return Collections.emptyList();
        }

        // Copiamos para no mutar la lista de la entidad
        List<OpcionPreguntaEntity> copiaOpciones = new ArrayList<>(opciones);

        if (Boolean.TRUE.equals(ordenAleatorio)) {
            Collections.shuffle(copiaOpciones);
        }

        // Reasignamos displayOrder según el orden resultante (1, 2, 3...)
        // para que el frontend renderice consistentemente
        AtomicInteger orden = new AtomicInteger(1);

        return copiaOpciones.stream()
                .map(opt -> OpcionPreguntaResponse.builder()
                        .idOpcion(opt.getIdOpcPregunta())
                        .texto(opt.getTexto())
                        .ordenVisualizacion(orden.getAndIncrement()) // orden en ESTA entrega
                        .build()
                )
                .toList();
    }
}