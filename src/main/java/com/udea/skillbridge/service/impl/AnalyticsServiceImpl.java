package com.udea.skillbridge.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.response.PlanFortalecimientoResponse;
import com.udea.skillbridge.dto.response.analytics.AnalisisDimensionalResponse;
import com.udea.skillbridge.dto.response.analytics.DistribucionNivelesResponse;
import com.udea.skillbridge.dto.response.analytics.EscalamientoResponse;
import com.udea.skillbridge.dto.response.analytics.EstudianteQueNecesitaApoyoResponse;
import com.udea.skillbridge.dto.response.analytics.HistorialIntentosResponse;
import com.udea.skillbridge.dto.response.analytics.InformeProgresoEstudianteResponse;
import com.udea.skillbridge.dto.response.analytics.NivelEstudianteResumenResponse;
import com.udea.skillbridge.dto.response.analytics.ReporteGrupoResponse;
import com.udea.skillbridge.dto.response.analytics.ResumenCuestionarioResponse;
import com.udea.skillbridge.dto.response.analytics.SkillProgresoResponse;
import com.udea.skillbridge.entity.CuestionarioEntity;
import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;
import com.udea.skillbridge.entity.PuntuacionMatrixEntity;
import com.udea.skillbridge.entity.PuntuacionResultadoEntity;
import com.udea.skillbridge.enums.EvaluacionFase;
import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.enums.analytics.DecisionEscala;
import com.udea.skillbridge.mapper.IPlanFortalecimientoMapper;
import com.udea.skillbridge.repository.IAnalyticsRepository;
import com.udea.skillbridge.repository.ICuestionarioRepository;
import com.udea.skillbridge.repository.IPuntuacionMatrixRepository;
import com.udea.skillbridge.service.IAnalyticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements IAnalyticsService {
	
	private final ICuestionarioRepository cuestionarioRepository;
	private final IAnalyticsRepository analyticsRepository;
	private final IPuntuacionMatrixRepository puntuacionMatrixRepository;
	private final IPlanFortalecimientoMapper planMapper;

	
	// *****************************************
	// Reporte individual PRE vs POST
	// *****************************************
	
	@Override
	public InformeProgresoEstudianteResponse getProgresoEstudiante(Long idEstudiante, Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findCuestionario(idCuestionario);

		// Obtener los últimos resultados de cada fase
		List<PuntuacionResultadoEntity> preResultados = analyticsRepository
				.findUltimosResultadosPorEstudianteAndFase(
						idEstudiante, idCuestionario, EvaluacionFase.PRE_TEST
						);

		List<PuntuacionResultadoEntity> postResultados = analyticsRepository
				.findUltimosResultadosPorEstudianteAndFase(
						idEstudiante, idCuestionario, EvaluacionFase.POST_TEST
						);

		if (preResultados.isEmpty()) {
			throw new BusinessException(
					"El estudiante " + idEstudiante +
					" no tiene un PRE_TEST completado para el cuestionario " + idCuestionario + ".",
					"PRE_TEST_NOT_FOUND"
					);
		}

		// Construir el progreso por dimensión
		List<SkillProgresoResponse> skillProgreso =
				construirProgresoSkill(preResultados, postResultados);

		// Decisión de escalamiento
		DecisionEscala decision = resolverEscalamiento(postResultados, skillProgreso);
		String razon = crearMotivoDeEscalacion(decision, skillProgreso);

		// Planes actuales (del POST_TEST si existe, sino del PRE_TEST)
		List<PuntuacionResultadoEntity> resultadosActuales = postResultados.isEmpty() ? preResultados : postResultados;
		List<PlanFortalecimientoResponse> planesActuales = construirPlanesActuales(resultadosActuales);

		// IDs de sesiones
		Long preId = preResultados.isEmpty() ? null
				: preResultados.get(0).getEvaluacionEnt().getId();
		Long postId = postResultados.isEmpty() ? null
				: postResultados.get(0).getEvaluacionEnt().getId();
		LocalDateTime preDate = preResultados.isEmpty() ? null
				: preResultados.get(0).getEvaluacionEnt().getFinishedAt();
		LocalDateTime postDate = postResultados.isEmpty() ? null
				: postResultados.get(0).getEvaluacionEnt().getFinishedAt();

		return InformeProgresoEstudianteResponse.builder()
				.idEstudiante(idEstudiante)
				.idCuestionario(idCuestionario)
				.nombreCuestionario(cuestionarioEnt.getNombre())
				.idPreTestEvaluacion(preId)
				.idPostTestEvaluacion(postId)
				.preTestDate(preDate)
				.postTestDate(postDate)
				.skillProgreso(skillProgreso)
				.decisionEscala(decision)
				.razonEscala(razon)
				.planActual(planesActuales)
				.resumen(buildResumenProgreso(skillProgreso, decision))
				.build();
	}
	
	// *****************************************
	// Reporte de grupo (docente
	// *****************************************
	
	@Override
	public ReporteGrupoResponse getReporteGrupo(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findCuestionario(idCuestionario);

        Long totalPre  = analyticsRepository.countEstudiantesDistintosConPreTest(idCuestionario);
        Long totalPost = analyticsRepository.countEstudiantesDistintosConPostTest(idCuestionario);
        Long totalAmbos = analyticsRepository.countEstudiantesConAmbasFases(idCuestionario);

        // Obtener todas las combinaciones de skill+dimension del cuestionario
        List<PuntuacionMatrixEntity> matrices = puntuacionMatrixRepository
                .findByCuestionarioEntIdCuestionario(idCuestionario);

        Set<String> dimensionKeys = matrices.stream()
                .map(m -> buildGroupKey(m.getSkill(), m.getDimension()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Análisis por dimensión
        List<AnalisisDimensionalResponse> analisisDimensional = new ArrayList<>();
        for (String key : dimensionKeys) {
            SkillTipo skill = extraerSkill(key);
            SkillDimension dimension = extraerDimension(key);
            analisisDimensional.add(buildAnalisisDimensional(
                idCuestionario, skill, dimension
            ));
        }

        // Marcar dimensión más crítica y con mayor mejora
        marcarAsCriticoAndMayormenteMejorado(analisisDimensional);

        AnalisisDimensionalResponse masCritica = analisisDimensional.stream()
                .filter(AnalisisDimensionalResponse::isMasCritico)
                .findFirst().orElse(null);

        AnalisisDimensionalResponse masMejorado = analisisDimensional.stream()
                .filter(AnalisisDimensionalResponse::isMasMejoro)
                .findFirst().orElse(null);

        // Tabla estudiante por estudiante
        List<NivelEstudianteResumenResponse> resumenesEstudiantes =
        		buildResumenesEstudiantes(idCuestionario);

        // % que llegó a AVANZADO
        double pctCualquierAvanzado = calcularPctCualquierAvanzado(idCuestionario, totalAmbos);
        double pctFullyAdvanced = calcularPctFullyAvanzado(
            idCuestionario, dimensionKeys.size(), totalAmbos
        );

        return ReporteGrupoResponse.builder()
                .idCuestionario(idCuestionario)
                .nombreCuestionario(cuestionarioEnt.getNombre())
                .generatedAt(LocalDateTime.now())
                .totalEstudiantesConPreTest(totalPre)
                .totalEstudiantesConPostTest(totalPost)
                .totalEstudiantesCompletados(totalAmbos)
                .analisiDimensional(analisisDimensional)
                .resumenEstudiantes(resumenesEstudiantes)
                .dimensionMasCritica(masCritica)
                .dimensionMasMejorada(masMejorado)
                .pcAlcanzadoCualquierNivelAvanzado(pctCualquierAvanzado)
                .pcTotalAvanzado(pctFullyAdvanced)
                .build();
	}
	
	// *****************************************
	// Análisis por dimensión
	// *****************************************
	
	@Override
	public List<AnalisisDimensionalResponse> getAnalisisDimensional(Long idCuestionario) {
        findCuestionario(idCuestionario);

        List<PuntuacionMatrixEntity> matrices = puntuacionMatrixRepository
                .findByCuestionarioEntIdCuestionario(idCuestionario);

        Set<String> dimensionKeys = matrices.stream()
                .map(m -> buildGroupKey(m.getSkill(), m.getDimension()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<AnalisisDimensionalResponse> analisis = dimensionKeys.stream()
                .map(key -> buildAnalisisDimensional(
                    idCuestionario, extraerSkill(key), extraerDimension(key)
                ))
                .toList();

        marcarAsCriticoAndMayormenteMejorado(analisis);
        return analisis;
	}
	
	// *****************************************
	// Decisión de escalamiento individual
	// *****************************************
	
	@Override
	public EscalamientoResponse getEscalamiento(Long idEstudiante, Long idCuestionario) {
		findCuestionario(idCuestionario);

        List<PuntuacionResultadoEntity> preResultados = analyticsRepository
                .findUltimosResultadosPorEstudianteAndFase(
                    idEstudiante, idCuestionario, EvaluacionFase.PRE_TEST
                );

        List<PuntuacionResultadoEntity> postResultados = analyticsRepository
                .findUltimosResultadosPorEstudianteAndFase(
                    idEstudiante, idCuestionario, EvaluacionFase.POST_TEST
                );

        if (postResultados.isEmpty()) {
            return EscalamientoResponse.builder()
                    .idEstudiante(idEstudiante)
                    .idCuestionario(idCuestionario)
                    .decision(DecisionEscala.PENDIENTE)
                    .razon("El estudiante no ha completado el POST_TEST.")
                    .dimensionesPendientes(Collections.emptyList())
                    .dimensionesAlcanzadas(Collections.emptyList())
                    .build();
        }

        List<SkillProgresoResponse> skillProgreso =
                construirProgresoSkill(preResultados, postResultados);

        List<SkillProgresoResponse> logrado = skillProgreso.stream()
                .filter(SkillProgresoResponse::isAlcanzoNivelAva)
                .toList();

        List<SkillProgresoResponse> pendiente = skillProgreso.stream()
                .filter(sp -> !sp.isAlcanzoNivelAva())
                .toList();

        DecisionEscala decision = pendiente.isEmpty()
                ? DecisionEscala.CERTIFICAR : DecisionEscala.REINICIAR;

        String razon = pendiente.isEmpty()
                ? "El estudiante alcanzó nivel AVANZADO en todas las dimensiones evaluadas."
                : "El estudiante aún tiene " + pendiente.size() +
                  " dimensión(es) sin alcanzar nivel AVANZADO: " +
                  pendiente.stream()
                      .map(sp -> (sp.getDimension() != null
                          ? sp.getDimension().name() : sp.getSkill().name()))
                      .collect(Collectors.joining(", ")) + ".";

        return EscalamientoResponse.builder()
                .idEstudiante(idEstudiante)
                .idCuestionario(idCuestionario)
                .decision(decision)
                .razon(razon)
                .dimensionesAlcanzadas(logrado)
                .dimensionesPendientes(pendiente)
                .build();
	}
	
	// *****************************************
	// Resumen ejecutivo
	// *****************************************
	
	@Override
	public ResumenCuestionarioResponse getResumen(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findCuestionario(idCuestionario);

        Long totalPre  = analyticsRepository.countEstudiantesDistintosConPreTest(idCuestionario);
        Long totalPost = analyticsRepository.countEstudiantesDistintosConPostTest(idCuestionario);
        Long totalAmbos = analyticsRepository.countEstudiantesConAmbasFases(idCuestionario);

        double tasaFinalizacion = totalPre > 0
                ? (totalAmbos.doubleValue() / totalPre.doubleValue()) * 100 : 0;

        List<PuntuacionResultadoEntity> allResultados = analyticsRepository
                .findTodosLosResultadosPorCuestionario(idCuestionario);

        double avgPre = allResultados.stream()
                .filter(r -> EvaluacionFase.PRE_TEST
                    .equals(r.getEvaluacionEnt().getEvaluacionFase()))
                .mapToDouble(r -> r.getPorcentajePuntuacion().doubleValue())
                .average().orElse(0);

        double avgPost = allResultados.stream()
                .filter(r -> EvaluacionFase.POST_TEST
                    .equals(r.getEvaluacionEnt().getEvaluacionFase()))
                .mapToDouble(r -> r.getPorcentajePuntuacion().doubleValue())
                .average().orElse(0);

        // Estudiantes que mejoraron (al menos una dimensión subió de nivel)
        long mejorado = countEstudiantesMejoraron(idCuestionario, totalAmbos);
        double pctMejorado = totalAmbos > 0
                ? (mejorado / totalAmbos.doubleValue()) * 100 : 0;

        // Elegibles para certificación (AVANZADO en todas las dimensiones en POST)
        long eligibleForCert = countEligibleForCertificacion(idCuestionario);
        long necesitaReincorporacion = totalAmbos - eligibleForCert;

        return ResumenCuestionarioResponse.builder()
                .idCuestionario(idCuestionario)
                .nombreCuestionario(cuestionarioEnt.getNombre())
                .estado(cuestionarioEnt.getEstadoCuestionario().name())
                .generatedAt(LocalDateTime.now())
                .totalEstudiantesConPreTest(totalPre)
                .totalEstudiantesConPostTest(totalPost)
                .TotalCompletadoAmbasFases(totalAmbos)
                .tasaFinalizacion(round(tasaFinalizacion))
                .avgPrePorcentaje(round(avgPre))
                .avgPostPorcentaje(round(avgPost))
                .avgDelta(round(avgPost - avgPre))
                .pctMejorado(round(pctMejorado))
                .EstudiantesElegiblesParaCertificacion(eligibleForCert)
                .estudiantesQueNecesitanSerReadmitidos(Math.max(0, necesitaReincorporacion))
                .build();
	}
	
	// *****************************************
	// Historial de Intentos
	// *****************************************
	
	@Override
	public List<HistorialIntentosResponse> getHistorialIntentos(Long idEstudiante, Long idCuestionario) {
		findCuestionario(idCuestionario);

        List<EvaluacionEstudianteEntity> evaluaciones = analyticsRepository
                .findHistorialCompletoDelEstudiante(idEstudiante, idCuestionario);

        return evaluaciones.stream()
                .map(evaluacion -> {
                    List<HistorialIntentosResponse.PuntuacionIntentosResponse> puntuacion =
                        evaluacion.getResultados().stream()
                            .map(r -> HistorialIntentosResponse.PuntuacionIntentosResponse.builder()
                                    .skill(r.getSkill())
                                    .dimension(r.getDimension())
                                    .totalPuntuacion(r.getTotalPuntaje())
                                    .porcentajePuntuacion(r.getPorcentajePuntuacion())
                                    .nivel(r.getNivel() != null ? r.getNivel().name() : "N/A")
                                    .build())
                            .toList();

                    return HistorialIntentosResponse.builder()
                            .idEvaluacion(evaluacion.getId())
                            .numeroIntentos(evaluacion.getNumeroIntento())
                            .fase(evaluacion.getEvaluacionFase())
                            .estado(evaluacion.getEstado())
                            .startedAt(evaluacion.getStartedAt())
                            .finishedAt(evaluacion.getFinishedAt())
                            .puntaje(puntuacion)
                            .build();
                })
                .toList();
	}
	
	// *****************************************
	// Distribución de niveles
	// *****************************************
	
	@Override
	public List<DistribucionNivelesResponse> getDistribucionNiveles(Long idCuestionario) {
		findCuestionario(idCuestionario);

        List<PuntuacionMatrixEntity> matrices = puntuacionMatrixRepository
                .findByCuestionarioEntIdCuestionario(idCuestionario);

        Set<String> dimensionKeys = matrices.stream()
                .map(m -> buildGroupKey(m.getSkill(), m.getDimension()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<DistribucionNivelesResponse> distribuciones = new ArrayList<>();
        for (EvaluacionFase fase : EvaluacionFase.values()) {
            for (String key : dimensionKeys) {
                SkillTipo skill = extraerSkill(key);
                SkillDimension dimension = extraerDimension(key);
                distribuciones.add(
                    buildDistribucionNiveles(idCuestionario, skill, dimension, fase)
                );
            }
        }
        return distribuciones;
	}
	
	// *****************************************
	// Estudiantes en BASIC (necesitan apoyo)
	// *****************************************
	
	@Override
	public List<EstudianteQueNecesitaApoyoResponse> getEstudiantesNecesitanApoyo(Long idCuestionario) {
		findCuestionario(idCuestionario);

        List<PuntuacionResultadoEntity> resultadoBajo = analyticsRepository
                .findResultadosNivelBasicoInPreTest(idCuestionario);

        // Agrupar por estudiante
        Map<Long, List<PuntuacionResultadoEntity>> byEstudiante = resultadoBajo.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getEvaluacionEnt().getIdEstudiante()
                ));

        return byEstudiante.entrySet().stream()
                .map(entry -> {
                    List<EstudianteQueNecesitaApoyoResponse.DimensionBajaResponse> dimensionBaja =
                        entry.getValue().stream()
                            .map(r -> EstudianteQueNecesitaApoyoResponse.DimensionBajaResponse
                                .builder()
                                .skill(r.getSkill())
                                .dimension(r.getDimension())
                                .puntaje(r.getTotalPuntaje())
                                .porcentaje(r.getPorcentajePuntuacion())
                                .build())
                            .toList();

                    Long preTestId = entry.getValue().get(0).getEvaluacionEnt().getId();

                    return EstudianteQueNecesitaApoyoResponse.builder()
                            .idEstudiante(entry.getKey())
                            .idPreTestEvaluacion(preTestId)
                            .dimensionBaja(dimensionBaja)
                            .build();
                })
                .toList();
	}
	
	// *****************************************
	// Estudiantes sin PRE_TEST
	// *****************************************
	
	@Override
	public List<Long> getEstudiantesSinPreTest(Long idCuestionario) {
		findCuestionario(idCuestionario);
        return analyticsRepository.findIdsEstudianteConPostTestPeroNoPreTest(idCuestionario);
	}
	
	// *****************************************
	// Estadísticas de completitud
	// *****************************************
	
	@Override
	public Map<String, Object> getEstadisticaFinalizacion(Long idCuestionario) {
		findCuestionario(idCuestionario);

        Long totalPre  = analyticsRepository.countEstudiantesDistintosConPreTest(idCuestionario);
        Long totalPost = analyticsRepository.countEstudiantesDistintosConPostTest(idCuestionario);
        Long totalAmbas = analyticsRepository.countEstudiantesConAmbasFases(idCuestionario);

        Map<String, Object> estadisticas = new LinkedHashMap<>();
        estadisticas.put("idCuestionario",    idCuestionario);
        estadisticas.put("estudiantesConPreTest", totalPre);
        estadisticas.put("estudiantesConPostTest", totalPost);
        estadisticas.put("estudiantesConAmbasFases", totalAmbas);
        estadisticas.put("tasaFinalizacion", totalPre > 0
            ? round((double) totalAmbas / totalPre * 100) : 0.0);
        estadisticas.put("tasaAbandono", totalPre > 0
            ? round((double)(totalPre - totalAmbas) / totalPre * 100) : 0.0);
        return estadisticas;
	}

	
	
	// *****************************************
	// Metodos Privados
	// *****************************************
	
    private CuestionarioEntity findCuestionario(Long id) {
        return cuestionarioRepository.findActivoById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuestionario", id));
    }
    
    private List<SkillProgresoResponse> construirProgresoSkill(
            List<PuntuacionResultadoEntity> preResultados,
            List<PuntuacionResultadoEntity> postResultados) {

        // Indexar POST por key
        Map<String, PuntuacionResultadoEntity> postByKey = postResultados.stream()
                .collect(Collectors.toMap(
                    r -> buildGroupKey(r.getSkill(), r.getDimension()),
                    r -> r,
                    (a, b) -> a
                ));

        return preResultados.stream().map(pre -> {
            String key = buildGroupKey(pre.getSkill(), pre.getDimension());
            PuntuacionResultadoEntity post = postByKey.get(key);

            int prePuntaje = pre.getTotalPuntaje();
            BigDecimal prePct = pre.getPorcentajePuntuacion();
            SkillNivel preNivel = pre.getNivel();

            Integer postPuntaje = post != null ? post.getTotalPuntaje() : null;
            BigDecimal postPct = post != null ? post.getPorcentajePuntuacion() : null;
            SkillNivel postNivel = post != null ? post.getNivel() : null;

            Integer delta = postPuntaje != null ? postPuntaje - prePuntaje : null;
            BigDecimal pctDelta = postPct != null
                    ? postPct.subtract(prePct).setScale(2, RoundingMode.HALF_UP)
                    : null;

            boolean mejorado = postNivel != null && preNivel != null
                    && nivelOrden(postNivel) > nivelOrden(preNivel);
            boolean avanzado = SkillNivel.AVANZADO.equals(postNivel);

            return SkillProgresoResponse.builder()
                    .skill(pre.getSkill())
                    .dimension(pre.getDimension())
                    .prePuntaje(prePuntaje)
                    .preMaxPuntaje(pre.getMaxPuntuacionPosible())
                    .prePorcentaje(prePct)
                    .preNivel(preNivel)
                    .postPuntaje(postPuntaje)
                    .postMaxPuntaje(post != null ? post.getMaxPuntuacionPosible() : null)
                    .postPorcentaje(postPct)
                    .postNivel(postNivel)
                    .puntajeDelta(delta)
                    .porcentajeDelta(pctDelta)
                    .nivelMejorado(mejorado)
                    .alcanzoNivelAva(avanzado)
                    .build();
        }).toList();
    }
    
    private int nivelOrden(SkillNivel nivel) {
        if (nivel == null) return -1;
        return switch (nivel) {
            case BAJO        -> 0;
            case INTERMEDIO  -> 1;
            case AVANZADO    -> 2;
        };
    }
    
    private DecisionEscala resolverEscalamiento(List<PuntuacionResultadoEntity> postResultados,
    		List<SkillProgresoResponse> progreso) {
    	if (postResultados.isEmpty()) return DecisionEscala.PENDIENTE;
    	boolean todoAvanzado = progreso.stream().allMatch(SkillProgresoResponse::isAlcanzoNivelAva);
    	return todoAvanzado ? DecisionEscala.CERTIFICAR : DecisionEscala.REINICIAR;
    }
    
	private String crearMotivoDeEscalacion(DecisionEscala decision, List<SkillProgresoResponse> progreso) {
		return switch (decision) {
		case CERTIFICAR ->
			"¡Felicitaciones! El estudiante alcanzó nivel AVANZADO en todas las dimensiones evaluadas. Es elegible para certificación.";
		case REINICIAR -> "El estudiante no alcanzó el nivel AVANZADO en "
				+ progreso.stream().filter(sp -> !sp.isAlcanzoNivelAva())
						.map(sp -> sp.getDimension() != null ? sp.getDimension().name() : sp.getSkill().name())
						.collect(Collectors.joining(", "))
				+ ". Se recomienda reiniciar la Fase 2 con un plan de acción ajustado.";
		case PENDIENTE -> "El estudiante aún no ha completado el POST_TEST.";
		};
	}
	
    private List<PlanFortalecimientoResponse> construirPlanesActuales(List<PuntuacionResultadoEntity> resultados) {
        return resultados.stream()
                .filter(r -> r.getPuntuacionMatrizEnt() != null)
                .flatMap(r -> r.getPuntuacionMatrizEnt().getPlanFortalecimientoEnt().stream())
                .map(planMapper::toResponse)
                .toList();
    }
    
	private String buildResumenProgreso(List<SkillProgresoResponse> progreso, DecisionEscala decision) {
		long mejorado = progreso.stream().filter(SkillProgresoResponse::isNivelMejorado).count();
		long avanzado = progreso.stream().filter(SkillProgresoResponse::isAlcanzoNivelAva).count();
		return String.format("%d de %d dimensiones mejoraron de nivel. %d alcanzaron ADVANCED. Decisión: %s.", mejorado,
				progreso.size(), avanzado, decision.name());
	}
	
    private String buildGroupKey(SkillTipo skill, SkillDimension dimension) {
        return skill.name() + "_" + (dimension != null ? dimension.name() : "_GLOBAL");
    }
	
    private SkillTipo extraerSkill(String key) {
        int lastUnderscore = key.lastIndexOf("_");
        String skillPart = key.substring(0, lastUnderscore);
        return SkillTipo.valueOf(skillPart);
    }
    
    private SkillDimension extraerDimension(String key) {
    	int lastUnderscore = key.lastIndexOf("_");
        String dimensionPart = key.substring(lastUnderscore + 1);
        
        if ("GLOBAL".equals(dimensionPart)) {
            return null;
        }
        
        return SkillDimension.valueOf(dimensionPart);
    }
    
    private AnalisisDimensionalResponse buildAnalisisDimensional(
            Long idCuestionario, SkillTipo skill, SkillDimension dimension) {

        Double avgPre = analyticsRepository.avgPorcentajePorDimensionDeHabilidadFase(
                idCuestionario, EvaluacionFase.PRE_TEST, skill, dimension);
        Double avgPost = analyticsRepository.avgPorcentajePorDimensionDeHabilidadFase(
                idCuestionario, EvaluacionFase.POST_TEST, skill, dimension);

        BigDecimal avgPreBD  = toBD(avgPre);
        BigDecimal avgPostBD = toBD(avgPost);
        BigDecimal avgDelta  = avgPostBD.subtract(avgPreBD)
                .setScale(2, RoundingMode.HALF_UP);

        DistribucionNivelesResponse preDistribucion =
        		buildDistribucionNiveles(idCuestionario, skill, dimension, EvaluacionFase.PRE_TEST);
        DistribucionNivelesResponse postDistribucion =
        		buildDistribucionNiveles(idCuestionario, skill, dimension, EvaluacionFase.POST_TEST);

        // Calcular estudiantes que mejoraron/estancaron/regresaron
        List<PuntuacionResultadoEntity> preResultados = analyticsRepository
                .findAllResultadosPorDimensionSkillDelCuestionarioFase(
                    idCuestionario, EvaluacionFase.PRE_TEST, skill, dimension
                );

        List<PuntuacionResultadoEntity> postResultados = analyticsRepository
                .findAllResultadosPorDimensionSkillDelCuestionarioFase(
                    idCuestionario, EvaluacionFase.POST_TEST, skill, dimension
                );

        Map<Long, SkillNivel> preByEstudiante = preResultados.stream()
                .collect(Collectors.toMap(
                    r -> r.getEvaluacionEnt().getIdEstudiante(),
                    PuntuacionResultadoEntity::getNivel,
                    (a, b) -> a
                ));

        long mejorado = 0;
        long estancado = 0;
        long retrocedio = 0;
        for (PuntuacionResultadoEntity post : postResultados) {
            Long sid = post.getEvaluacionEnt().getIdEstudiante();
            SkillNivel pre = preByEstudiante.get(sid);
            if (pre == null) continue;
            int cmp = nivelOrden(post.getNivel()) - nivelOrden(pre);
            if (cmp > 0) mejorado++;
            else if (cmp == 0) estancado++;
            else retrocedio++;
        }
        
        return AnalisisDimensionalResponse.builder()
                .skill(skill)
                .dimension(dimension)
                .avgPrePorcentaje(avgPreBD)
                .avgPostPorcentaje(avgPostBD)
                .avgDelta(avgDelta)
                .preDistribucion(preDistribucion)
                .postDistribucion(postDistribucion)
                .estudiantesMejorados(mejorado)
                .estudiantesEstancados(estancado)
                .estudiantesRetrocedieron(retrocedio)
                .totalEstudiantes((long) postResultados.size())
                .masCritico(false)  // se marca después
                .masMejoro(false)    // se marca después
                .build();
    }
    
    private BigDecimal toBD(Double valor) {
        return valor != null
                ? BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }
    
    private DistribucionNivelesResponse buildDistribucionNiveles(
            Long idCUestionario, SkillTipo skill,
            SkillDimension dimension, EvaluacionFase fase) {

        List<PuntuacionResultadoEntity> resultados = analyticsRepository
                .findAllResultadosPorDimensionSkillDelCuestionarioFase(
                    idCUestionario, fase, skill, dimension
                );

        long bajo        = resultados.stream().filter(r -> SkillNivel.BAJO.equals(r.getNivel())).count();
        long intermedio  = resultados.stream().filter(r -> SkillNivel.INTERMEDIO.equals(r.getNivel())).count();
        long avanzado    = resultados.stream().filter(r -> SkillNivel.AVANZADO.equals(r.getNivel())).count();
        long total       = resultados.size();

        return DistribucionNivelesResponse.builder()
                .skill(skill)
                .dimension(dimension)
                .fase(fase)
                .recuentoBasico(bajo)
                .recuentoIntermedio(intermedio)
                .recuentoAvanzado(avanzado)
                .totalEstudiantes(total)
                .porcentajeBasico(total > 0 ? round(bajo * 100.0 / total) : 0)
                .porcentajeIntermedio(total > 0 ? round(intermedio * 100.0 / total) : 0)
                .porcentajeAvanzado(total > 0 ? round(avanzado * 100.0 / total) : 0)
                .build();
    }
    
    private double round(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    
    private void marcarAsCriticoAndMayormenteMejorado(List<AnalisisDimensionalResponse> analisis) {
        analisis.stream()
                .min(Comparator.comparing(d ->
                    d.getAvgPrePorcentaje() != null ? d.getAvgPrePorcentaje() : BigDecimal.ZERO))
                .ifPresent(d -> d.setMasCritico(true));

        analisis.stream()
                .max(Comparator.comparing(d ->
                    d.getAvgDelta() != null ? d.getAvgDelta() : BigDecimal.ZERO))
                .ifPresent(d -> d.setMasMejoro(true));
    }
    
    private List<NivelEstudianteResumenResponse> buildResumenesEstudiantes(Long idCuestionario) {
        List<PuntuacionResultadoEntity> allResultados = analyticsRepository
                .findTodosLosResultadosPorCuestionario(idCuestionario);

        return allResultados.stream().map(r -> NivelEstudianteResumenResponse.builder()
                .idEstudiante(r.getEvaluacionEnt().getIdEstudiante())
                .skill(r.getSkill())
                .dimension(r.getDimension())
                .fase(r.getEvaluacionEnt().getEvaluacionFase())
                .totalPuntaje(r.getTotalPuntaje())
                .maxPosiblePuntaje(r.getMaxPuntuacionPosible())
                .puntajePorcentaje(r.getPorcentajePuntuacion())
                .nivel(r.getNivel())
                .build()
        ).toList();
    }
    
    private double calcularPctCualquierAvanzado(Long idCuestionario, Long totalAmbos) {
        if (totalAmbos == 0) return 0;
        List<PuntuacionResultadoEntity> postResultados = analyticsRepository
                .findTodosLosResultadosPorCuestionario(idCuestionario).stream()
                .filter(r -> EvaluacionFase.POST_TEST
                    .equals(r.getEvaluacionEnt().getEvaluacionFase()))
                .toList();

        long count = postResultados.stream()
                .collect(Collectors.groupingBy(r -> r.getEvaluacionEnt().getIdEstudiante()))
                .values().stream()
                .filter(l -> l.stream().anyMatch(r -> SkillNivel.AVANZADO.equals(r.getNivel())))
                .count();

        return round(count * 100.0 / totalAmbos);
    }
    
	private double calcularPctFullyAvanzado(Long idCuestionario, int totalDimensiones, Long totalAmbos) {
		if (totalAmbos == 0 || totalDimensiones == 0)
			return 0;
		long eligible = countEligibleForCertificacion(idCuestionario);
		return round(eligible * 100.0 / totalAmbos);
	}
	
    private long countEligibleForCertificacion(Long idCuestionario) {
        List<PuntuacionResultadoEntity> postResultados = analyticsRepository
                .findTodosLosResultadosPorCuestionario(idCuestionario).stream()
                .filter(r -> EvaluacionFase.POST_TEST
                    .equals(r.getEvaluacionEnt().getEvaluacionFase()))
                .toList();

        Map<Long, List<PuntuacionResultadoEntity>> byEstudiante = postResultados.stream()
                .collect(Collectors.groupingBy(r -> r.getEvaluacionEnt().getIdEstudiante()));

        return byEstudiante.values().stream()
                .filter(results -> results.stream()
                    .allMatch(r -> SkillNivel.AVANZADO.equals(r.getNivel())))
                .count();
    }   
    
    private long countEstudiantesMejoraron(Long idCuestionario, Long totalAmbos) {
        if (totalAmbos == 0) return 0;
        List<PuntuacionResultadoEntity> allResultados = analyticsRepository
                .findTodosLosResultadosPorCuestionario(idCuestionario);

        Map<Long, List<PuntuacionResultadoEntity>> byStudiante = allResultados.stream()
                .collect(Collectors.groupingBy(r -> r.getEvaluacionEnt().getIdEstudiante()));

        return byStudiante.values().stream().filter(resultados -> {
            Map<String, PuntuacionResultadoEntity> pre = resultados.stream()
                    .filter(r -> EvaluacionFase.PRE_TEST
                        .equals(r.getEvaluacionEnt().getEvaluacionFase()))
                    .collect(Collectors.toMap(
                        r -> buildGroupKey(r.getSkill(), r.getDimension()),
                        r -> r, (a, b) -> a
                    ));
            return resultados.stream()
                    .filter(r -> EvaluacionFase.POST_TEST
                        .equals(r.getEvaluacionEnt().getEvaluacionFase()))
                    .anyMatch(post -> {
                        String key = buildGroupKey(post.getSkill(), post.getDimension());
                        PuntuacionResultadoEntity prePuntuacion = pre.get(key);
                        return prePuntuacion != null && post.getNivel() != null
                            && prePuntuacion.getNivel() != null
                            && nivelOrden(post.getNivel()) > nivelOrden(prePuntuacion.getNivel());
                    });
        }).count();
    }
}
