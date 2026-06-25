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

		// 3. Agrupar matrices por (skill + dimensión gestionada FK) — Fase 3
        Map<String, List<PuntuacionMatrixEntity>> grupoMatrices = matrices.stream()
                .collect(Collectors.groupingBy(this::buildGroupKey));
        
        // 4. Calcular un PuntuacionMatrixEntity por grupo
		List<PuntuacionResultadoEntity> resultados = new ArrayList<>();

		for (Map.Entry<String, List<PuntuacionMatrixEntity>> entry : grupoMatrices.entrySet()) {
			List<PuntuacionMatrixEntity> groupMatrices = entry.getValue();

			// Determinar el skill y dimensión gestionada (FK) del grupo
			PuntuacionMatrixEntity muestra = groupMatrices.get(0);
			SkillTipo skill = muestra.getSkill();
			Long grupoIdDimension = muestra.getDimensionEnt() != null
					? muestra.getDimensionEnt().getId() : null;

			// Sumar puntajes de las preguntas asociadas a este grupo.
			// IMPORTANTE: cada pregunta debe contar UNA sola vez aunque el grupo
			// tenga varias entradas de matriz (una por nivel). Por eso se deduplica
			// por idPregunta antes de sumar (evita el "triplicado").
			int totalPuntaje = 0;
			int maxPuntajePosible = 0;

			boolean esGlobal = groupMatrices.stream().anyMatch(m -> m.getPreguntaEnt() == null);

			Map<Long, PreguntaEntity> preguntasUnicas;
			if (esGlobal) {
				// Evaluación global: se suman las preguntas respondidas una sola vez.
				// Si el grupo tiene dimensión, se restringe a las preguntas de ESA
				// dimensión (no todo el cuestionario); si no, se suman todas.
				preguntasUnicas = respuesta.stream()
						.map(DetalleRespuestaEntity::getPreguntaEnt)
						.filter(p -> perteneceADimension(p, grupoIdDimension))
						.collect(Collectors.toMap(PreguntaEntity::getIdPregunta, p -> p, (a, b) -> a));
			} else {
				// Evaluación por pregunta específica: deduplicar preguntas del grupo
				preguntasUnicas = groupMatrices.stream()
						.map(PuntuacionMatrixEntity::getPreguntaEnt)
						.filter(p -> p != null)
						.collect(Collectors.toMap(PreguntaEntity::getIdPregunta, p -> p, (a, b) -> a));
			}

			for (PreguntaEntity preguntaEnt : preguntasUnicas.values()) {
				totalPuntaje += puntajeByPregunta.getOrDefault(preguntaEnt.getIdPregunta(), 0);
				maxPuntajePosible += calcularPuntuacionMaxima(preguntaEnt);
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
					.dimensionEnt(muestra.getDimensionEnt())  // dimensión gestionada (si la matriz la tiene)
					.totalPuntaje(totalPuntaje)
					.maxPuntuacionPosible(maxPuntajePosible)
					.porcentajePuntuacion(porcentaje)
					.nivel(matrizCoincidencia != null ? matrizCoincidencia.getNivel() : null)
					.puntuacionMatrizEnt(matrizCoincidencia)
					.build();

			resultados.add(resultado);
			log.info("PuntuacionResultadoEntity → skill={} idDimension={} score={}/{} ({}%) nivel={}", skill,
					muestra.getDimensionEnt() != null ? muestra.getDimensionEnt().getId() : null, totalPuntaje,
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
	
    /**
     * Indica si una pregunta pertenece a la dimensión del grupo.
     * - grupoIdDimension null (grupo global del skill): aplica a todas las preguntas.
     * - grupoIdDimension no null: solo las preguntas vinculadas a esa dimensión.
     */
    private boolean perteneceADimension(PreguntaEntity pregunta, Long grupoIdDimension) {
        if (grupoIdDimension == null) {
            return true;
        }
        return pregunta.getDimension() != null
                && grupoIdDimension.equals(pregunta.getDimension().getId());
    }

    /**
     * Clave de agrupamiento por skill + dimensión gestionada (FK).
     * Si la entrada no tiene dimensión, se agrupa como "GLOBAL".
     */
    private String buildGroupKey(PuntuacionMatrixEntity m) {
        String dimPart = m.getDimensionEnt() != null ? "DIM_" + m.getDimensionEnt().getId() : "GLOBAL";
        return m.getSkill().name() + "_" + dimPart;
    }
    
    private int calcularPuntuacionMaxima(PreguntaEntity preguntaEnt) {
        if (TipoPregunta.DESCRIPCION.equals(preguntaEnt.getTipoPregunta())) {
            return 0;
        }

        return switch (preguntaEnt.getTipoPregunta()) {
            // En soft skills no hay opción "correcta": el puntaje se mide por peso.
            // El estudiante selecciona UNA opción → el máximo es el mayor peso disponible.
            case OPCION_UNICA, VERDADERO_FALSO, LIKERT -> preguntaEnt.getOpcionPregunta().stream()
                    .mapToInt(OpcionPreguntaEntity::getPeso)
                    .max()
                    .orElse(0);

            // Para OPCION_MULTIPLE el estudiante puede elegir varias opciones.
            // El máximo es la suma de los N pesos más altos, donde N = maxOpciones
            // (si no se definió límite, se suman todos los pesos).
            case OPCION_MULTIPLE -> {
                var pesosOrdenados = preguntaEnt.getOpcionPregunta().stream()
                        .mapToInt(OpcionPreguntaEntity::getPeso)
                        .boxed()
                        .sorted(java.util.Comparator.reverseOrder())
                        .toList();
                Integer maxOpciones = preguntaEnt.getMaxOpciones();
                int limite = (maxOpciones != null && maxOpciones > 0)
                        ? Math.min(maxOpciones, pesosOrdenados.size())
                        : pesosOrdenados.size();
                yield pesosOrdenados.stream().limit(limite).mapToInt(Integer::intValue).sum();
            }

            default -> 0;
        };
    }

}
