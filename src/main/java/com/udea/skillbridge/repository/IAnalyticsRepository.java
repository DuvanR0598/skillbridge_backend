package com.udea.skillbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;
import com.udea.skillbridge.entity.PuntuacionResultadoEntity;
import com.udea.skillbridge.enums.EvaluacionFase;
import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillTipo;

/**
 * Repositorio dedicado exclusivamente a consultas de analytics.
 * Todas las queries son de solo lectura.
 * Extiende JpaRepository de StudentAssessment como entidad principal.
 */
@Repository
public interface IAnalyticsRepository extends JpaRepository<EvaluacionEstudianteEntity, Long> {
	
    // ── Consultas de sesión ─────────────────────────────────────────

    @Query("""
        SELECT a FROM EvaluacionEstudianteEntity a
        WHERE a.idEstudiante                   = :idEstudiante
          AND a.cuestionarioEnt.idCuestionario = :idCuestionario
          AND a.evaluacionFase                 = :fase
          AND a.estado                         = 'COMPLETADO'
        ORDER BY a.numeroIntento DESC
    """)
    Optional<EvaluacionEstudianteEntity> findUltimaCompletadaPorEstudianteYFase(
        Long idEstudiante, Long idCuestionario, EvaluacionFase fase
    );
    
    @Query("""
            SELECT a FROM EvaluacionEstudianteEntity a
            WHERE a.cuestionarioEnt.idCuestionario = :idCuestionario
              AND a.evaluacionFase                 = 'POST_TEST'
              AND a.estado                         = 'COMPLETADO'
        """)
        List<EvaluacionEstudianteEntity> findTodasLasPruebasPostCompletadas(Long idCuestionario);
    
    @Query("""
            SELECT a FROM EvaluacionEstudianteEntity a
            WHERE a.idEstudiante                   = :idEstudiante
              AND a.cuestionarioEnt.idCuestionario = :idCuestionario
            ORDER BY a.evaluacionFase, a.numeroIntento
        """)
        List<EvaluacionEstudianteEntity> findHistorialCompletoDelEstudiante(
            Long idEstudiante, Long idCuestionario
        );
    
    // ── Conteos de participación ────────────────────────────────────

    @Query("""
        SELECT COUNT(DISTINCT a.idEstudiante)
        FROM EvaluacionEstudianteEntity a
        WHERE a.cuestionarioEnt.idCuestionario = :idCuestionario
          AND a.evaluacionFase                 = 'PRE_TEST'
          AND a.estado                         = 'COMPLETADO'
    """)
    Long countEstudiantesDistintosConPreTest(Long idCuestionario);
    
    @Query("""
            SELECT COUNT(DISTINCT a.idEstudiante)
            FROM EvaluacionEstudianteEntity a
            WHERE a.cuestionarioEnt.idCuestionario = :idCuestionario
              AND a.evaluacionFase                 = 'POST_TEST'
              AND a.estado                         = 'COMPLETADO'
        """)
        Long countEstudiantesDistintosConPostTest(Long idCuestionario);
    
    @Query("""
            SELECT COUNT(DISTINCT pre.idEstudiante)
            FROM EvaluacionEstudianteEntity pre
            WHERE pre.cuestionarioEnt.idCuestionario = :idCuestionario
              AND pre.evaluacionFase                 = 'PRE_TEST'
              AND pre.estado                         = 'COMPLETADO'
              AND EXISTS (
                  SELECT 1 FROM EvaluacionEstudianteEntity post
                  WHERE post.idEstudiante                   = pre.idEstudiante
                    AND post.cuestionarioEnt.idCuestionario = :idCuestionario
                    AND post.evaluacionFase                 = 'POST_TEST'
                    AND post.estado                         = 'COMPLETADO'
              )
        """)
        Long countEstudiantesConAmbasFases(Long idCuestionario);
    
    // ── Consultas de PuntuacionResultadoEntity ────────────────────────────────────

    @Query("""
        SELECT r FROM PuntuacionResultadoEntity r
        WHERE r.evaluacionEnt.idEstudiante                   = :idEstudiante
          AND r.evaluacionEnt.cuestionarioEnt.idCuestionario = :idCuestionario
          AND r.evaluacionEnt.evaluacionFase                 = :fase
          AND r.evaluacionEnt.estado                         = 'COMPLETADO'
        ORDER BY r.evaluacionEnt.numeroIntento DESC
    """)
    List<PuntuacionResultadoEntity> findUltimosResultadosPorEstudianteAndFase(
        Long idEstudiante, Long idCuestionario, EvaluacionFase fase
    );
    
    @Query("""
            SELECT r FROM PuntuacionResultadoEntity r
            WHERE r.evaluacionEnt.cuestionarioEnt.idCuestionario = :idCuestionario
              AND r.evaluacionEnt.evaluacionFase                 = :fase
              AND r.evaluacionEnt.estado                         = 'COMPLETADO'
              AND r.skill                                        = :skill
              AND ((:dimension IS NULL AND r.dimension IS NULL)
                   OR r.dimension = :dimension)
        """)
        List<PuntuacionResultadoEntity> findAllResultadosPorDimensionSkillDelCuestionarioFase(
            Long idCuestionario,
            EvaluacionFase fase,
            SkillTipo skill,
            SkillDimension dimension
        );
    
    @Query("""
            SELECT r FROM PuntuacionResultadoEntity r
            WHERE r.evaluacionEnt.cuestionarioEnt.idCuestionario = :idCuestionario
              AND r.evaluacionEnt.estado                         = 'COMPLETADO'
        """)
        List<PuntuacionResultadoEntity> findTodosLosResultadosPorCuestionario(Long idCuestionario);
    
    // ── Promedios ───────────────────────────────────────────────────

    @Query("""
        SELECT AVG(r.porcentajePuntuacion)
        FROM PuntuacionResultadoEntity r
        WHERE r.evaluacionEnt.cuestionarioEnt.idCuestionario = :idCuestionario
          AND r.evaluacionEnt.evaluacionFase                 = :fase
          AND r.evaluacionEnt.estado                         = 'COMPLETADO'
          AND r.skill                                        = :skill
          AND ((:dimension IS NULL AND r.dimension IS NULL)
               OR r.dimension = :dimension)
    """)
    Double avgPorcentajePorDimensionDeHabilidadFase(
        Long idCuestionario,
        EvaluacionFase fase,
        SkillTipo skill,
        SkillDimension dimension
    );
    
    // ── Estudiantes en nivel BAJO (necesitan apoyo prioritario) ────

    @Query("""
        SELECT r FROM PuntuacionResultadoEntity r
        WHERE r.evaluacionEnt.cuestionarioEnt.idCuestionario = :idCuestionario
          AND r.evaluacionEnt.evaluacionFase                 = 'PRE_TEST'
          AND r.evaluacionEnt.estado                         = 'COMPLETADO'
          AND r.nivel                                        = 'BAJO'
    """)
    List<PuntuacionResultadoEntity> findResultadosNivelBasicoInPreTest(Long idCuestionario);
    
    // ── Todos los resultados de un estudiante en un cuestionario ────

    @Query("""
        SELECT r FROM PuntuacionResultadoEntity r
        WHERE r.evaluacionEnt.idEstudiante                   = :idEstudiante
          AND r.evaluacionEnt.cuestionarioEnt.idCuestionario = :idCuestionario
          AND r.evaluacionEnt.estado                         = 'COMPLETADO'
        ORDER BY r.evaluacionEnt.numeroIntento, r.evaluacionEnt.evaluacionFase
    """)
    List<PuntuacionResultadoEntity> finTodosLosResultadosPorEstudianteAndCuestionario(
        Long idEstudiante, Long idCuestionario
    );
    
    // ── Estudiantes sin PRE_TEST ────────────────────────────────────

    @Query("""
        SELECT DISTINCT a.idEstudiante
        FROM EvaluacionEstudianteEntity a
        WHERE a.cuestionarioEnt.idCuestionario = :idCuestionario
          AND a.evaluacionFase                 = 'POST_TEST'
          AND NOT EXISTS (
              SELECT 1 FROM EvaluacionEstudianteEntity pre
              WHERE pre.idEstudiante                   = a.idEstudiante
                AND pre.cuestionarioEnt.idCuestionario = :idCuestionario
                AND pre.evaluacionFase                 = 'PRE_TEST'
                AND pre.estado                         = 'COMPLETED'
          )
    """)
    List<Long> findIdsEstudianteConPostTestPeroNoPreTest(Long idCuestionario);

}
