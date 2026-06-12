package com.udea.skillbridge.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.ActualizarPesoOpcionesRequest;
import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.dto.response.PreguntaResponse;
import com.udea.skillbridge.entity.DimensionEntity;
import com.udea.skillbridge.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.mapper.IPreguntaMapper;
import com.udea.skillbridge.repository.IDimensionRepository;
import com.udea.skillbridge.repository.IPreguntaRepository;
import com.udea.skillbridge.service.IPreguntaService;
import com.udea.skillbridge.validation.OpcionOrdenValidador;
import com.udea.skillbridge.validation.PreguntaValidadorFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreguntaServiceImpl implements IPreguntaService {
	
	private final IPreguntaRepository preguntaRepository;
	private final PreguntaValidadorFactory validadorFactory;
	private final OpcionOrdenValidador opcionOrdenValidador;
	private final IPreguntaMapper preguntaMapper;
	private final IDimensionRepository dimensionRepository;
	
	// *****************************************
    //  CREAR PREGUNTA
    // *****************************************

	@Override
	public PreguntaResponse crearPregunta(PreguntaRequest request) {
		log.info("Creando pregunta tipo: {}", request.getTipoPregunta());
		
		// PASO 1: Validación específica del tipo (Strategy Pattern)
        // El factory elige el validador correcto según el tipo de pregunta.
		validadorFactory.getValidador(request.getTipoPregunta()).validador(request);

        // PASO 2: Validación transversal de las opciones (orden, texto vacío)
        // Solo aplica si hay opciones (DESCRIPCION no las tiene)
		opcionOrdenValidador.validate(request.getOpcionPreguntaRequest());
		
		// PASO 3: Construir la entidad
		PreguntaEntity preguntaEnt = preguntaMapper.toEntity(request);

		// PASO 3.b: Asignar dimensión si viene en el request
		if (request.getIdDimension() != null) {
			preguntaEnt.setDimension(buscarDimension(request.getIdDimension()));
		}

		// PASO 4: Construir y asociar las opciones de respuesta
        // Cada opción conoce a su pregunta (relación bidireccional)
        if (request.getOpcionPreguntaRequest() != null && !request.getOpcionPreguntaRequest().isEmpty()) {
        	List<OpcionPreguntaEntity> opciones = request.getOpcionPreguntaRequest().stream()
        			.map(optReq -> {
        				OpcionPreguntaEntity opcion = preguntaMapper.toOpcionPreguntaEntity(optReq);
        				opcion.setPreguntaEnt(preguntaEnt);  // Establecer relación bidireccional
        				return opcion;
        			})
        			.toList();
        	
        	preguntaEnt.getOpcionPregunta().addAll(opciones);
        }
        // PASO 5: Persistir (cascade guarda las opciones automáticamente)
        PreguntaEntity guardar = preguntaRepository.save(preguntaEnt);
        log.info("Pregunta creada con ID: {}", guardar.getIdPregunta());

        return preguntaMapper.toResponse(guardar);
	}
	
	// *****************************************
    //  OBTENER POR ID
    // *****************************************
	
	@Override
	public PreguntaResponse findById(Long idPregunta) {
		return preguntaMapper.toResponse(findEntityById(idPregunta));
	}
	
	// *****************************************
    //  LISTAR TODAS
    // *****************************************
	
	@Override
	public List<PreguntaResponse> listarTodo() {
		return preguntaRepository.findAll()
                .stream()
                .map(preguntaMapper::toResponse)
                .toList();
	}
	
	// *****************************************
    //  LISTAR POR TIPO
    // *****************************************
	
	@Override
	public List<PreguntaResponse> listarPorTipo(TipoPregunta tipoPregunta) {
		return preguntaRepository.findByTipoPregunta(tipoPregunta)
				.stream()
				.map(preguntaMapper::toResponse)
				.toList();
	}
	
    // **************************************************
    //  ACTUALIZAR PESOS DE OPCIONES
    //  (permitido aunque el cuestionario esté COMPLETO)
    // **************************************************

	@Override
	public PreguntaResponse actualizarPesosOpciones(Long idPregunta, ActualizarPesoOpcionesRequest request) {
		PreguntaEntity preguntaEnt = findEntityById(idPregunta);

		request.getPesos().forEach((idOpcion, newPeso) -> {
            if (newPeso < 0) {
                throw new BusinessException(
                    "El peso no puede ser negativo. Opción: " + idOpcion,
                    "INVALID_WEIGHT"
                );
            }
            preguntaEnt.getOpcionPregunta().stream()
                    .filter(o -> o.getId().equals(idOpcion))
                    .findFirst()
                    .ifPresentOrElse(
                        o -> o.setPeso(newPeso),
                        () -> { throw new ResourceNotFoundException("Opción", idOpcion); }
                    );
        });

        return preguntaMapper.toResponse(preguntaRepository.save(preguntaEnt));
	}
	
	// **************************************************
    //  ELIMINAR PREGUNTA
    //  Solo si NO está asociada a ningún cuestionario
    // **************************************************
	
	@Override
	public void eliminarPregunta(Long preguntaId) {
		PreguntaEntity preguntaEnt = findEntityById(preguntaId);
		
		// Verificar que la pregunta no esté en uso en ningún cuestionario
        // (la relación inversa en PreguntaCuestionario nos lo indica)
        boolean isEnUso = !preguntaEnt.getPreguntaCuestionarioEnt().isEmpty();

        if (isEnUso) {
        	throw new BusinessException(
                    "No se puede eliminar. La pregunta está asociada a " +
                    preguntaEnt.getPreguntaCuestionarioEnt().size() + " cuestionario(s).",
                    "QUESTION_IN_USE"
                );
        }

        preguntaRepository.delete(preguntaEnt);
        log.info("Pregunta {} eliminada", preguntaId);
		
	}
	
	// **************************************************
    //  ASIGNAR / CAMBIAR DIMENSIÓN DE UNA PREGUNTA
    // **************************************************

	@Override
	public PreguntaResponse asignarDimension(Long idPregunta, Long idDimension) {
		PreguntaEntity pregunta = findEntityById(idPregunta);
		// idDimension null = desasignar
		pregunta.setDimension(idDimension == null ? null : buscarDimension(idDimension));
		log.info("Pregunta {} -> dimensión {}", idPregunta, idDimension);
		return preguntaMapper.toResponse(preguntaRepository.save(pregunta));
	}

	// **************************************************
    //  METODOS PRIVADOS
    // **************************************************

	public PreguntaEntity findEntityById(Long id) {
        return preguntaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta", id));
    }

	private DimensionEntity buscarDimension(Long idDimension) {
        return dimensionRepository.findById(idDimension)
                .orElseThrow(() -> new ResourceNotFoundException("Dimensión", idDimension));
    }
}
