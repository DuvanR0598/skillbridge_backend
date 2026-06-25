package com.udea.skillbridge.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
import com.udea.skillbridge.entity.DimensionEntity;
import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;
import com.udea.skillbridge.entity.PuntuacionMatrixEntity;
import com.udea.skillbridge.entity.PuntuacionResultadoEntity;
import com.udea.skillbridge.enums.EvaluacionFase;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // mantiene la sesión abierta para cargas LAZY (dimensionEnt)
public class AnalyticsServiceImpl implements IAnalyticsService {
	
	private final ICuestionarioRepository cuestionarioRepository;
	private final IAnalyticsRepository analyticsRepository;
	private final IPuntuacionMatrixRepository puntuacionMatrixRepository;
	private final IPlanFortalecimientoMapper planMapper;
	private final com.udea.skillbridge.seguridad.repository.IUsuarioRepository usuarioRepository;

	
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

		// Sin PRE_TEST aún no es un error: el estudiante simplemente no ha
		// empezado. Devolvemos un informe vacío (200) para que el dashboard
		// pueda mostrar el estado "sin datos" sin tratarlo como fallo.
		if (preResultados.isEmpty()) {
			return InformeProgresoEstudianteResponse.builder()
					.idEstudiante(idEstudiante)
					.idCuestionario(idCuestionario)
					.nombreCuestionario(cuestionarioEnt.getNombre())
					.skillProgreso(List.of())
					.planActual(List.of())
					.resumen("El estudiante aún no tiene un PRE_TEST completado para este cuestionario.")
					.build();
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

        List<GrupoDimension> grupos = gruposDeDimension(matrices);

        // Análisis por dimensión
        List<AnalisisDimensionalResponse> analisisDimensional = new ArrayList<>();
        for (GrupoDimension grupo : grupos) {
            analisisDimensional.add(buildAnalisisDimensional(idCuestionario, grupo));
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
            idCuestionario, grupos.size(), totalAmbos
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

        List<AnalisisDimensionalResponse> analisis = gruposDeDimension(matrices).stream()
                .map(grupo -> buildAnalisisDimensional(idCuestionario, grupo))
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
                      .map(sp -> (sp.getDimensionNombre() != null
                          ? sp.getDimensionNombre() : sp.getSkill().name()))
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
                                    .idDimension(r.getDimensionEnt() != null ? r.getDimensionEnt().getId() : null)
                                    .dimensionNombre(r.getDimensionEnt() != null ? r.getDimensionEnt().getNombre() : null)
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

        List<GrupoDimension> grupos = gruposDeDimension(matrices);

        List<DistribucionNivelesResponse> distribuciones = new ArrayList<>();
        for (EvaluacionFase fase : EvaluacionFase.values()) {
            for (GrupoDimension grupo : grupos) {
                distribuciones.add(
                    buildDistribucionNiveles(idCuestionario, grupo, fase)
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

        // Datos de los estudiantes (nombre/apellido/email) en una sola consulta.
        Map<Long, com.udea.skillbridge.seguridad.entity.UsuarioEntity> usuariosById =
                usuarioRepository.findAllById(byEstudiante.keySet()).stream()
                        .collect(Collectors.toMap(
                            com.udea.skillbridge.seguridad.entity.UsuarioEntity::getId,
                            u -> u
                        ));

        return byEstudiante.entrySet().stream()
                .map(entry -> {
                    List<EstudianteQueNecesitaApoyoResponse.DimensionBajaResponse> dimensionBaja =
                        entry.getValue().stream()
                            .map(r -> EstudianteQueNecesitaApoyoResponse.DimensionBajaResponse
                                .builder()
                                .skill(r.getSkill())
                                .idDimension(r.getDimensionEnt() != null ? r.getDimensionEnt().getId() : null)
                                .dimensionNombre(r.getDimensionEnt() != null ? r.getDimensionEnt().getNombre() : null)
                                .puntaje(r.getTotalPuntaje())
                                .porcentaje(r.getPorcentajePuntuacion())
                                .build())
                            .toList();

                    Long preTestId = entry.getValue().get(0).getEvaluacionEnt().getId();

                    com.udea.skillbridge.seguridad.entity.UsuarioEntity u = usuariosById.get(entry.getKey());
                    String nombreCompleto = u != null
                            ? (u.getNombre() + " " + u.getApellido()).trim()
                            : null;
                    String email = u != null ? u.getEmail() : null;

                    return EstudianteQueNecesitaApoyoResponse.builder()
                            .idEstudiante(entry.getKey())
                            .nombreCompleto(nombreCompleto)
                            .email(email)
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

        // Indexar POST por key (por dimensión gestionada FK — Fase 3)
        Map<String, PuntuacionResultadoEntity> postByKey = postResultados.stream()
                .collect(Collectors.toMap(
                    this::keyPorDimensionResultado,
                    r -> r,
                    (a, b) -> a
                ));

        return preResultados.stream().map(pre -> {
            String key = keyPorDimensionResultado(pre);
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
                    .idDimension(pre.getDimensionEnt() != null ? pre.getDimensionEnt().getId() : null)
                    .dimensionNombre(pre.getDimensionEnt() != null ? pre.getDimensionEnt().getNombre() : null)
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
						.map(sp -> sp.getDimensionNombre() != null ? sp.getDimensionNombre() : sp.getSkill().name())
						.collect(Collectors.joining(", "))
				+ ". Se recomienda reiniciar la Fase 2 con un plan de acción ajustado.";
		case PENDIENTE -> "El estudiante aún no ha completado el POST_TEST.";
		};
	}
	
    private List<PlanFortalecimientoResponse> construirPlanesActuales(List<PuntuacionResultadoEntity> resultados) {
        // El "plan actual" es el del resultado GLOBAL (sin dimensión), con sus
        // ejes (Académico/Experimental/Personal). No se mezclan los planes de
        // cada dimensión. Si el cuestionario no tiene entrada global, se cae a
        // todos los resultados con plan (compatibilidad).
        List<PuntuacionResultadoEntity> globales = resultados.stream()
                .filter(r -> r.getDimensionEnt() == null && r.getPuntuacionMatrizEnt() != null)
                .toList();

        List<PuntuacionResultadoEntity> fuente = globales.isEmpty()
                ? resultados.stream().filter(r -> r.getPuntuacionMatrizEnt() != null).toList()
                : globales;

        return fuente.stream()
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
	
    /**
     * Grupo de análisis: skill + dimensión gestionada (FK). Reemplaza al
     * antiguo enum SkillDimension. idDimension null = evaluación global del skill.
     */
    private record GrupoDimension(SkillTipo skill, Long idDimension, String nombre) {}

    /**
     * Construye la lista única de grupos (skill + dimensión gestionada) a partir
     * de las entradas de la matriz del cuestionario, preservando el orden.
     */
    private List<GrupoDimension> gruposDeDimension(List<PuntuacionMatrixEntity> matrices) {
        Map<String, GrupoDimension> grupos = new LinkedHashMap<>();
        for (PuntuacionMatrixEntity m : matrices) {
            DimensionEntity dim = m.getDimensionEnt();
            Long idDim = dim != null ? dim.getId() : null;
            String nombre = dim != null ? dim.getNombre() : null;
            String key = m.getSkill().name() + "_" + (idDim != null ? idDim : "GLOBAL");
            grupos.putIfAbsent(key, new GrupoDimension(m.getSkill(), idDim, nombre));
        }
        return new ArrayList<>(grupos.values());
    }

    /**
     * Clave de agrupamiento por skill + dimensión gestionada (FK) — Fase 3.
     * Debe coincidir con la del MotorDePuntuacion para emparejar PRE/POST.
     */
    private String keyPorDimensionResultado(PuntuacionResultadoEntity r) {
        String dimPart = r.getDimensionEnt() != null ? "DIM_" + r.getDimensionEnt().getId() : "GLOBAL";
        return r.getSkill().name() + "_" + dimPart;
    }
    
    private AnalisisDimensionalResponse buildAnalisisDimensional(
            Long idCuestionario, GrupoDimension grupo) {

        SkillTipo skill = grupo.skill();
        Long idDimension = grupo.idDimension();

        Double avgPre = analyticsRepository.avgPorcentajePorDimensionDeHabilidadFase(
                idCuestionario, EvaluacionFase.PRE_TEST, skill, idDimension);
        Double avgPost = analyticsRepository.avgPorcentajePorDimensionDeHabilidadFase(
                idCuestionario, EvaluacionFase.POST_TEST, skill, idDimension);

        BigDecimal avgPreBD  = toBD(avgPre);
        BigDecimal avgPostBD = toBD(avgPost);
        BigDecimal avgDelta  = avgPostBD.subtract(avgPreBD)
                .setScale(2, RoundingMode.HALF_UP);

        DistribucionNivelesResponse preDistribucion =
        		buildDistribucionNiveles(idCuestionario, grupo, EvaluacionFase.PRE_TEST);
        DistribucionNivelesResponse postDistribucion =
        		buildDistribucionNiveles(idCuestionario, grupo, EvaluacionFase.POST_TEST);

        // Calcular estudiantes que mejoraron/estancaron/regresaron
        List<PuntuacionResultadoEntity> preResultados = analyticsRepository
                .findAllResultadosPorDimensionSkillDelCuestionarioFase(
                    idCuestionario, EvaluacionFase.PRE_TEST, skill, idDimension
                );

        List<PuntuacionResultadoEntity> postResultados = analyticsRepository
                .findAllResultadosPorDimensionSkillDelCuestionarioFase(
                    idCuestionario, EvaluacionFase.POST_TEST, skill, idDimension
                );

        // putIfAbsent (no Collectors.toMap) porque el nivel puede ser null
        // (resultado fuera de los rangos configurados) y toMap rechaza valores null.
        Map<Long, SkillNivel> preByEstudiante = new HashMap<>();
        for (PuntuacionResultadoEntity r : preResultados) {
            preByEstudiante.putIfAbsent(r.getEvaluacionEnt().getIdEstudiante(), r.getNivel());
        }

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
                .idDimension(idDimension)
                .dimensionNombre(grupo.nombre())
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
            Long idCUestionario, GrupoDimension grupo, EvaluacionFase fase) {

        List<PuntuacionResultadoEntity> resultados = analyticsRepository
                .findAllResultadosPorDimensionSkillDelCuestionarioFase(
                    idCUestionario, fase, grupo.skill(), grupo.idDimension()
                );

        long bajo        = resultados.stream().filter(r -> SkillNivel.BAJO.equals(r.getNivel())).count();
        long intermedio  = resultados.stream().filter(r -> SkillNivel.INTERMEDIO.equals(r.getNivel())).count();
        long avanzado    = resultados.stream().filter(r -> SkillNivel.AVANZADO.equals(r.getNivel())).count();
        long total       = resultados.size();

        return DistribucionNivelesResponse.builder()
                .skill(grupo.skill())
                .idDimension(grupo.idDimension())
                .dimensionNombre(grupo.nombre())
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

        // Datos de los estudiantes (nombre/apellido/email) en una sola consulta.
        java.util.Set<Long> idsEstudiantes = allResultados.stream()
                .map(r -> r.getEvaluacionEnt().getIdEstudiante())
                .collect(Collectors.toSet());
        Map<Long, com.udea.skillbridge.seguridad.entity.UsuarioEntity> usuariosById =
                usuarioRepository.findAllById(idsEstudiantes).stream()
                        .collect(Collectors.toMap(
                            com.udea.skillbridge.seguridad.entity.UsuarioEntity::getId,
                            u -> u
                        ));

        return allResultados.stream().map(r -> {
                com.udea.skillbridge.seguridad.entity.UsuarioEntity u =
                        usuariosById.get(r.getEvaluacionEnt().getIdEstudiante());
                return NivelEstudianteResumenResponse.builder()
                    .idEstudiante(r.getEvaluacionEnt().getIdEstudiante())
                    .nombreCompleto(u != null ? (u.getNombre() + " " + u.getApellido()).trim() : null)
                    .email(u != null ? u.getEmail() : null)
                    .idEvaluacion(r.getEvaluacionEnt().getId())
                    .skill(r.getSkill())
                    .idDimension(r.getDimensionEnt() != null ? r.getDimensionEnt().getId() : null)
                    .dimensionNombre(r.getDimensionEnt() != null ? r.getDimensionEnt().getNombre() : null)
                    .fase(r.getEvaluacionEnt().getEvaluacionFase())
                    .totalPuntaje(r.getTotalPuntaje())
                    .maxPosiblePuntaje(r.getMaxPuntuacionPosible())
                    .puntajePorcentaje(r.getPorcentajePuntuacion())
                    .nivel(r.getNivel())
                    .build();
        }).toList();
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
                        this::keyPorDimensionResultado,
                        r -> r, (a, b) -> a
                    ));
            return resultados.stream()
                    .filter(r -> EvaluacionFase.POST_TEST
                        .equals(r.getEvaluacionEnt().getEvaluacionFase()))
                    .anyMatch(post -> {
                        String key = keyPorDimensionResultado(post);
                        PuntuacionResultadoEntity prePuntuacion = pre.get(key);
                        return prePuntuacion != null && post.getNivel() != null
                            && prePuntuacion.getNivel() != null
                            && nivelOrden(post.getNivel()) > nivelOrden(prePuntuacion.getNivel());
                    });
        }).count();
    }
}
