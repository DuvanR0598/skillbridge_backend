package com.udea.skillbridge.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.ActualizarPuntuacionMatrixRequest;
import com.udea.skillbridge.dto.request.PuntuacionMatrixRequest;
import com.udea.skillbridge.dto.response.PuntuacionMatrixResponse;
import com.udea.skillbridge.entity.CuestionarioEntity;
import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.entity.PuntuacionMatrixEntity;
import com.udea.skillbridge.enums.EstadoCuestionario;
import com.udea.skillbridge.enums.NivelBloom;
import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.mapper.IPuntuacionMatrixMapper;
import com.udea.skillbridge.repository.IPuntuacionMatrixRepository;
import com.udea.skillbridge.service.IPuntuacionMatrixService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PuntuacionMatrixServiceImpl implements IPuntuacionMatrixService {

	private final CuestionarioServiceImpl cuestionarioService;
	private final PreguntaServiceImpl preguntaService;
	private final IPuntuacionMatrixRepository puntuacionMatrixRepository;
	private final IPuntuacionMatrixMapper puntuacionMatrixMapper;

	// *****************************************
	// CREAR ENTRADA
	// *****************************************

	@Override
	public PuntuacionMatrixResponse crear(Long idcuestionario, PuntuacionMatrixRequest request) {
		CuestionarioEntity cuestionarioEnt = cuestionarioService.findActivoById(idcuestionario);
		validarMatrixEditable(cuestionarioEnt);
		validarRangoPuntuacion(request.getMinPuntaje(), request.getMaxPuntaje());

		// Resolver pregunta si viene especificada
		PreguntaEntity preguntaEnt = null;
		if (request.getIdPregunta() != null) {
			preguntaEnt = preguntaService.findEntityById(request.getIdPregunta());
			validarTipoPregunta(preguntaEnt);
			validarPreguntaPerteneceToCuestionario(preguntaEnt, cuestionarioEnt);
		}

		// Verificar unicidad de la combinación
		validarEntradaUnica(idcuestionario, request.getSkill(), request.getDimension(), request.getNivel(),
				request.getIdPregunta());

		// Detectar solapamiento de rangos
		validarSinSuperposiciónRango(idcuestionario, request.getSkill(), request.getDimension(), request.getNivel(),
				request.getIdPregunta(), request.getMinPuntaje(), request.getMaxPuntaje(), -1L);

		// Aplicar niveles de Bloom por defecto si no vienen en el request
		List<NivelBloom> nivelesBloom = resolverBloomLevels(request.getNivelBloom(), request.getNivel());

		PuntuacionMatrixEntity matrix = puntuacionMatrixMapper.toEntity(request);
		matrix.setCuestionarioEnt(cuestionarioEnt);
		matrix.setPreguntaEnt(preguntaEnt);
		matrix.getNivelesBloom().clear();
		matrix.getNivelesBloom().addAll(nivelesBloom);

		PuntuacionMatrixEntity guardar = puntuacionMatrixRepository.save(matrix);
		log.info("PuntuacionMatrix creada → cuestionario={} skill={} dimension={} nivel={}", idcuestionario,
				request.getSkill(), request.getDimension(), request.getNivel());

		return puntuacionMatrixMapper.toResponse(guardar);
	}
	
	// *****************************************
	// LISTAR POR CUESTIONARIO
	// *****************************************
	
	@Override
	public List<PuntuacionMatrixResponse> findByCuestionario(Long idCuestionario) {
		cuestionarioService.findActivoById(idCuestionario);
        return puntuacionMatrixRepository.findByCuestionarioEntIdCuestionario(idCuestionario)
                .stream()
                .map(puntuacionMatrixMapper::toResponse)
                .toList();
	}
	
	// *****************************************
	// LISTAR POR CUESTIONARIO + SKILL
	// *****************************************
	
	@Override
	public List<PuntuacionMatrixResponse> findByCuestionarioAndSkill(Long idCuestionario, SkillTipo skill) {
		cuestionarioService.findActivoById(idCuestionario);
        return puntuacionMatrixRepository
                .findByCuestionarioEntIdCuestionarioAndSkill(idCuestionario, skill)
                .stream()
                .map(puntuacionMatrixMapper::toResponse)
                .toList();
	}
	
	// *****************************************
	// OBTENER POR ID
	// *****************************************
	
	@Override
    public PuntuacionMatrixResponse findById(Long idMatrix) {
        return puntuacionMatrixMapper.toResponse(findEntityById(idMatrix));
    }

	// *****************************************
	// ACTUALIZAR
	// *****************************************
	
	@Override
	public PuntuacionMatrixResponse actualizar(Long idMatrix, ActualizarPuntuacionMatrixRequest request) {
		PuntuacionMatrixEntity matrix = findEntityById(idMatrix);
		validarMatrixEditable(matrix.getCuestionarioEnt());

        Integer finalMin = request.getMinPuntaje() != null
                ? request.getMinPuntaje() : matrix.getMinPuntaje();
        Integer finalMax = request.getMaxPuntaje() != null
                ? request.getMaxPuntaje() : matrix.getMaxPuntaje();

        validarRangoPuntuacion(finalMin, finalMax);

        validarSinSuperposiciónRango(
            matrix.getCuestionarioEnt().getIdCuestionario(),
            matrix.getSkill(),
            matrix.getDimension(),
            matrix.getNivel(),
            matrix.getPreguntaEnt() != null ? matrix.getPreguntaEnt().getIdPregunta() : null,
            finalMin,
            finalMax,
            matrix.getId()
        );

        puntuacionMatrixMapper.updateFromRequest(matrix, request);

        // Actualizar bloomLevels si vienen en el request
        if (request.getNivelesBloom() != null && !request.getNivelesBloom().isEmpty()) {
            matrix.getNivelesBloom().clear();
            matrix.getNivelesBloom().addAll(request.getNivelesBloom());
        }

        return puntuacionMatrixMapper.toResponse(puntuacionMatrixRepository.save(matrix));
	}
	
	// *****************************************
	// ELIMINAR
	// *****************************************
	
	@Override
	public void eliminar(Long idMatrix) {
		PuntuacionMatrixEntity matrix = findEntityById(idMatrix);
        validarMatrixEditable(matrix.getCuestionarioEnt());
        // cascade = ALL elimina los planes de fortalecimiento automáticamente
        puntuacionMatrixRepository.delete(matrix);
        log.info("PuntuacionMatrix [{}] eliminada (cascade elimina sus planes)", idMatrix);
		
	}
	
	// *****************************************
	// METODOS PRIVADOS
	// *****************************************

	private void validarMatrixEditable(CuestionarioEntity cuestionarioEnt) {
		boolean editable = EstadoCuestionario.COMPLETO.equals(cuestionarioEnt.getEstadoCuestionario())
				|| EstadoCuestionario.PUBLICADO.equals(cuestionarioEnt.getEstadoCuestionario());
		if (!editable) {
			throw new BusinessException("La matriz de valoración solo se puede configurar cuando el cuestionario "
					+ "está en estado COMPLETADO o PUBLICADO. Estado actual: "
					+ cuestionarioEnt.getEstadoCuestionario(), "MATRIX_NOT_EDITABLE");
		}
	}

	private void validarRangoPuntuacion(Integer min, Integer max) {
		if (min >= max) {
			throw new BusinessException("El puntaje mínimo (" + min + ") debe ser menor al máximo (" + max + ").",
					"INVALID_SCORE_RANGE");
		}
	}

	private void validarTipoPregunta(PreguntaEntity preguntaEnt) {
		/*
		 * DESCRIPCION no genera puntaje porque no tiene opciones seleccionables. No
		 * tiene sentido asociarla a un rango de scoring.
		 */
		if (TipoPregunta.DESCRIPCION.equals(preguntaEnt.getTipoPregunta())) {
			throw new BusinessException("Las preguntas de tipo DESCRIPCION no pueden asociarse a la matriz de "
					+ "valoración porque no generan puntaje.", "INVALID_QUESTION_TYPE_FOR_MATRIX");
		}
	}

	private void validarPreguntaPerteneceToCuestionario(PreguntaEntity preguntaEnt,
			CuestionarioEntity cuestionarioEnt) {
		boolean pertenece = cuestionarioEnt.getPreguntasCuestionario().stream()
				.anyMatch(qq -> qq.getPreguntaEnt().getIdPregunta().equals(preguntaEnt.getIdPregunta()));

		if (!pertenece) {
			throw new BusinessException("La pregunta " + preguntaEnt.getIdPregunta() + " no pertenece al cuestionario "
					+ cuestionarioEnt.getIdCuestionario() + ".", "QUESTION_NOT_IN_QUESTIONNAIRE");
		}
	}

	private void validarEntradaUnica(Long idCuestionario, SkillTipo skill, SkillDimension dimension, SkillNivel nivel,
			Long idPregunta) {
		puntuacionMatrixRepository
				.findByCuestionarioEntIdCuestionarioAndSkillAndDimensionAndNivelAndPreguntaEntIdPregunta(idCuestionario,
						skill, dimension, nivel, idPregunta)
				.ifPresent(existing -> {
					throw new BusinessException(
							"Ya existe una entrada en la matriz para: skill=" + skill
									+ (dimension != null ? " dimensión=" + dimension : "") + " nivel=" + nivel
									+ (idPregunta != null ? " pregunta=" + idPregunta : " (global)") + ".",
							"MATRIX_ENTRY_ALREADY_EXISTS");
				});
	}

	private void validarSinSuperposiciónRango(Long idCuestionario, SkillTipo skill, SkillDimension dimension,
			SkillNivel nivel, Long idPregunta, Integer min, Integer max, Long excludeId) {
		boolean overlaps = puntuacionMatrixRepository.existeRangoSuperpuesto(idCuestionario, skill, dimension, nivel,
				idPregunta, min, max, excludeId);
		if (overlaps) {
			throw new BusinessException(
					"El rango [" + min + " - " + max + "] se solapa con una entrada " + "existente para skill=" + skill
							+ (dimension != null ? " dimensión=" + dimension : "") + " nivel=" + nivel + ".",
					"SCORE_RANGE_OVERLAP");
		}
	}
	
    /**
     * Si el request no incluye nivelesBloom, aplica el mapa por defecto
     */
    private List<NivelBloom> resolverBloomLevels(List<NivelBloom> solicitado, SkillNivel nivel) {
        if (solicitado != null && !solicitado.isEmpty()) return solicitado;
        return switch (nivel) {
            case BAJO        -> List.of(NivelBloom.RECORDAR, NivelBloom.COMPRENDER);
            case INTERMEDIO  -> List.of(NivelBloom.APLICAR,    NivelBloom.ANALIZAR);
            case AVANZADO    -> List.of(NivelBloom.EVALUAR,  NivelBloom.CREAR);
        };
    }
    
 // --- Acceso a entidad para otros services ---

    public PuntuacionMatrixEntity findEntityById(Long id) {
        return puntuacionMatrixRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PuntuacionMatrix", id));
    }


}
