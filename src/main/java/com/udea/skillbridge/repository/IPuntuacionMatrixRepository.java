package com.udea.skillbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.PuntuacionMatrixEntity;
import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;

@Repository
public interface IPuntuacionMatrixRepository extends JpaRepository<PuntuacionMatrixEntity, Long>{
	
	List<PuntuacionMatrixEntity> findByCuestionarioEntIdCuestionario(Long idCuestionario);

	List<PuntuacionMatrixEntity> findByCuestionarioEntIdCuestionarioAndSkill(Long idCuestionario, SkillTipo skill);
	
    Optional<PuntuacionMatrixEntity> findByCuestionarioEntIdCuestionarioAndSkillAndDimensionAndNivelAndPreguntaEntIdPregunta(
            Long idCuestionario,
            SkillTipo skill,
            SkillDimension dimension,
            SkillNivel nivel,
            Long idPregunta
        );
    
    /**
     * Verifica si existe un rango de puntuación superpuesto para los mismos criterios.
     * 
     * @param idCuestionario ID del cuestionario
     * @param skill Skill a evaluar
     * @param dimension Dimensión del skill (puede ser null)
     * @param nivel Nivel del skill
     * @param idPregunta ID de la pregunta (puede ser null para nivel global)
     * @param newMin Puntaje mínimo nuevo
     * @param newMax Puntaje máximo nuevo
     * @param excludeId ID a excluir (para actualizaciones)
     * @return true si existe superposición, false en caso contrario
     */
    @Query("""
        SELECT COUNT(m) > 0 
        FROM PuntuacionMatrixEntity m
        WHERE m.cuestionarioEnt.idCuestionario = :idCuestionario
          AND m.skill = :skill
          AND m.nivel = :nivel
          AND m.id != :excludeId
          AND ((:dimension IS NULL AND m.dimension IS NULL) OR m.dimension = :dimension)
          AND ((:idPregunta IS NULL AND m.preguntaEnt IS NULL) OR m.preguntaEnt.idPregunta = :idPregunta)
          AND :newMin <= m.maxPuntaje
          AND :newMax >= m.minPuntaje
    """)
    boolean existeRangoSuperpuesto(
            @Param("idCuestionario") Long idCuestionario,
            @Param("skill") SkillTipo skill,
            @Param("dimension") SkillDimension dimension,
            @Param("nivel") SkillNivel nivel,
            @Param("idPregunta") Long idPregunta,
            @Param("newMin") Integer newMin,
            @Param("newMax") Integer newMax,
            @Param("excludeId") Long excludeId
        );

}
