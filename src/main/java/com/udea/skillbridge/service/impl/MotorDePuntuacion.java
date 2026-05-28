package com.udea.skillbridge.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.udea.skillbridge.entity.DetalleRespuestaEntity;
import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;
import com.udea.skillbridge.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.entity.PuntuacionMatrixEntity;
import com.udea.skillbridge.entity.PuntuacionResultadoEntity;
import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.repository.IPuntuacionMatrixRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MotorDePuntuacion {

	private final IPuntuacionMatrixRepository puntuacionMatrixrepository;

	/**
	 * Calcula los PuntuacionResultadoEntity a partir de las respuestas de una
	 * sesión.
	 *
	 * Flujo: 1. Para cada DetalleRespuestaEntity, calcular el puntaje obtenido. 2.
	 * Agrupar puntajes por (skill + dimension) según la PuntuacionMatrixEntity del
	 * cuestionario. 3. Para cada grupo, buscar la entrada de la matriz que cubre el
	 * puntaje total. 4. Construir el PuntuacionResultadoEntity con nivel,
	 * porcentaje y planes.
	 */
	public List<PuntuacionResultadoEntity> calcular(EvaluacionEstudianteEntity evaluacion, 
			List<DetalleRespuestaEntity> respuesta) {

		Long idCuestionario = evaluacion.getCuestionarioEnt().getIdCuestionario();

		// 1. Calcular puntaje por pregunta
		Map<Long, Integer> puntajeByPregunta = new HashMap<>();
		for (DetalleRespuestaEntity respuestaEnt : respuesta) {
			int puntaje = calcularPuntuaciónDeLaPregunta(respuestaEnt);
			respuestaEnt.setPuntajeObtenido(puntaje);
			puntajeByPregunta.put(respuestaEnt.getPreguntaEnt().getIdPregunta(), puntaje);
		}

		// 2. Cargar todas las entradas de la matriz del cuestionario
		List<PuntuacionMatrixEntity> matrices = puntuacionMatrixrepository.findByCuestionarioEntIdCuestionario(idCuestionario);

		if (matrices.isEmpty()) {
			log.warn("Cuestionario [{}] no tiene matriz configurada. " + "PuntuacionResultadoEntity no se calculará.", idCuestionario);
			return Collections.emptyList();
		}

		// 3. Agrupar matrices por (skill + dimension)
        Map<String, List<PuntuacionMatrixEntity>> grupoMatrices = matrices.stream()
                .collect(Collectors.groupingBy(
                    m -> buildGroupKey(m.getSkill(), m.getDimension())
                ));
        
        // 4. Calcular un PuntuacionMatrixEntity por grupo
		List<PuntuacionResultadoEntity> resultados = new ArrayList<>();

		for (Map.Entry<String, List<PuntuacionMatrixEntity>> entry : grupoMatrices.entrySet()) {
			List<PuntuacionMatrixEntity> groupMatrices = entry.getValue();

			// Determinar el skill y dimension del grupo
			PuntuacionMatrixEntity muestra = groupMatrices.get(0);
			SkillTipo skill = muestra.getSkill();
			SkillDimension dimension = muestra.getDimension();

			// Sumar puntajes de las preguntas asociadas a este grupo
			int totalPuntaje = 0;
			int maxPuntajePosible = 0;

			for (PuntuacionMatrixEntity matrix : groupMatrices) {
				if (matrix.getPreguntaEnt() != null) {
					// Evaluación por pregunta específica
					Long idPregunta = matrix.getPreguntaEnt().getIdPregunta();
					totalPuntaje += puntajeByPregunta.getOrDefault(idPregunta, 0);
					maxPuntajePosible += calcularPuntuacionMaxima(matrix.getPreguntaEnt());
				} else {
					// Evaluación global: suma todas las preguntas respondidas
					for (DetalleRespuestaEntity respuestaEnt : respuesta) {
						totalPuntaje += puntajeByPregunta.getOrDefault(respuestaEnt.getPreguntaEnt().getIdPregunta(), 0);
						maxPuntajePosible += calcularPuntuacionMaxima(respuestaEnt.getPreguntaEnt());
					}
					break; // evitar duplicar para evaluación global
				}
			}

			// Calcular porcentaje
			BigDecimal porcentaje = maxPuntajePosible > 0
					? BigDecimal.valueOf(totalPuntaje).multiply(BigDecimal.valueOf(100)).divide(
							BigDecimal.valueOf(maxPuntajePosible), 2, RoundingMode.HALF_UP)
					: BigDecimal.ZERO;

			// Buscar la entrada de la matriz que cubre el puntaje
			int finalTotalScore = totalPuntaje;
			PuntuacionMatrixEntity matrizCoincidencia = groupMatrices.stream()
					.filter(m -> finalTotalScore >= m.getMinPuntaje() 
								&& finalTotalScore <= m.getMaxPuntaje())
					.findFirst()
					.orElse(null);

			PuntuacionResultadoEntity resultado = PuntuacionResultadoEntity.builder()
					.evaluacionEnt(evaluacion)
					.skill(skill)
					.dimension(dimension)
					.totalPuntaje(totalPuntaje)
					.maxPuntuacionPosible(maxPuntajePosible)
					.porcentajePuntuacion(porcentaje)
					.nivel(matrizCoincidencia != null ? matrizCoincidencia.getNivel() : null)
					.puntuacionMatrizEnt(matrizCoincidencia)
					.build();

			resultados.add(resultado);
			log.info("PuntuacionResultadoEntity → skill={} dimension={} score={}/{} ({}%) nivel={}", skill, dimension, totalPuntaje,
					maxPuntajePosible, porcentaje, matrizCoincidencia != null ? matrizCoincidencia.getNivel() : "N/A");
		}

		return resultados;
	}
	
	// *****************************************
	// METODOS PRIVADOS
	// *****************************************
	
	private int calcularPuntuaciónDeLaPregunta(DetalleRespuestaEntity respuesta) {
        PreguntaEntity pregunta = respuesta.getPreguntaEnt();

        // DESCRIPTION no genera puntaje
        if (TipoPregunta.DESCRIPCION.equals(pregunta.getTipoPregunta())) {
            return 0;
        }

        // Si no seleccionó opciones, puntaje 0
        if (respuesta.getIdsOpcionesSeleccionadas() == null ||
                respuesta.getIdsOpcionesSeleccionadas().isEmpty()) {
            return 0;
        }

        // Sumar pesos de las opciones seleccionadas
        return pregunta.getOpcionPregunta().stream()
                .filter(opt -> respuesta.getIdsOpcionesSeleccionadas().contains(opt.getId()))
                .mapToInt(OpcionPreguntaEntity::getPeso)
                .sum();
    }
	
    private String buildGroupKey(SkillTipo skill, SkillDimension dimension) {
        return skill.name() + "_" + (dimension != null ? dimension.name() : "GLOBAL");
    }
    
    private int calcularPuntuacionMaxima(PreguntaEntity preguntaEnt) {
        if (TipoPregunta.DESCRIPCION.equals(preguntaEnt.getTipoPregunta())) {
            return 0;
        }

        return switch (preguntaEnt.getTipoPregunta()) {
            // Para OPCION_UNICA y VERDADERO_FALSO: el máximo es el peso de la opción correcta
            case OPCION_UNICA, VERDADERO_FALSO -> preguntaEnt.getOpcionPregunta().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrecta()))
                    .mapToInt(OpcionPreguntaEntity::getPeso)
                    .sum();

            // Para OPCION_MULTIPLE: suma de todas las opciones correctas
            case OPCION_MULTIPLE -> preguntaEnt.getOpcionPregunta().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrecta()))
                    .mapToInt(OpcionPreguntaEntity::getPeso)
                    .sum();

            // Para LIKERT: el mayor peso disponible
            case LIKERT -> preguntaEnt.getOpcionPregunta().stream()
                    .mapToInt(OpcionPreguntaEntity::getPeso)
                    .max()
                    .orElse(0);

            default -> 0;
        };
    }

}
