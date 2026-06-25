         package com.udea.skillbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.PuntuacionMatrixEntity;
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;

@Repository
public interface IPuntuacionMatrixRepository extends JpaRepository<PuntuacionMatrixEntity, Long>{
	
	List<PuntuacionMatrixEntity> findByCuestionarioEntIdCuestionario(Long idCuestionario);

	List<PuntuacionMatrixEntity> findByCuestionarioEntIdCuestionarioAndSkill(Long idCuestionario, SkillTipo skill);
	
    // ── Variantes basadas en la dimensión gestionada (FK) — Fase 3 ──

    @Query("""
        SELECT m FROM PuntuacionMatrixEntity m
        WHERE m.cuestionarioEnt.idCuestionario = :idCuestionario
          AND m.skill = :skill
          AND m.nivel = :nivel
          AND ((:idDimension IS NULL AND m.dimensionEnt IS NULL) OR m.dimensionEnt.id = :idDimension)
          AND ((:idPregunta IS NULL AND m.preguntaEnt IS NULL) OR m.preguntaEnt.idPregunta = :idPregunta)
    """)
    Optional<PuntuacionMatrixEntity> buscarEntradaPorDimension(
            @Param("idCuestionario") Long idCuestionario,
            @Param("skill") SkillTipo skill,
            @Param("idDimension") Long idDimension,
            @Param("nivel") SkillNivel nivel,
            @Param("idPregunta") Long idPregunta
        );

    @Query("""
        SELECT COUNT(m) > 0
        FROM PuntuacionMatrixEntity m
        WHERE m.cuestionarioEnt.idCuestionario = :idCuestionario
          AND m.skill = :skill
          AND m.nivel = :nivel
          AND m.id != :excludeId
          AND ((:idDimension IS NULL AND m.dimensionEnt IS NULL) OR m.dimensionEnt.id = :idDimension)
          AND ((:idPregunta IS NULL AND m.preguntaEnt IS NULL) OR m.preguntaEnt.idPregunta = :idPregunta)
          AND :newMin <= m.maxPuntaje
          AND :newMax >= m.minPuntaje
    """)
    boolean existeRangoSuperpuestoPorDimension(
            @Param("idCuestionario") Long idCuestionario,
            @Param("skill") SkillTipo skill,
            @Param("idDimension") Long idDimension,
            @Param("nivel") SkillNivel nivel,
            @Param("idPregunta") Long idPregunta,
            @Param("newMin") Integer newMin,
            @Param("newMax") Integer newMax,
            @Param("excludeId") Long excludeId
        );

}
