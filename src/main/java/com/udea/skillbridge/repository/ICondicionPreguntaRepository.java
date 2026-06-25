package com.udea.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.CondicionPreguntaEntity;

@Repository
public interface ICondicionPreguntaRepository extends JpaRepository<CondicionPreguntaEntity, Long> {
	
	// Todas las condiciones de un cuestionario
    List<CondicionPreguntaEntity> findByCuestionarioEntIdCuestionario(Long idCuestionario);
    
    // Las condiciones que dispara una opción específica
    List<CondicionPreguntaEntity> findByTriggerOpcionId(Long triggerIdOpcion);
    
    // Verificar si ya existe esta combinación DENTRO de un cuestionario específico.
    // La misma condición (opción → destino) puede repetirse en cuestionarios distintos.
    boolean existsByTriggerOpcionIdAndTargetPreguntaIdPreguntaAndCuestionarioEntIdCuestionario(
            Long triggerIdOpcion, Long targetIdPregunta, Long idCuestionario);

    // Igual que la anterior pero excluyendo una condición concreta (para editar sin chocar consigo misma).
    boolean existsByTriggerOpcionIdAndTargetPreguntaIdPreguntaAndCuestionarioEntIdCuestionarioAndIdNot(
            Long triggerIdOpcion, Long targetIdPregunta, Long idCuestionario, Long idCondicion);

    // Cuántas condiciones de ENTRADA tiene una pregunta hija
    // Usamos esto para validar que no tenga más de una
    int countByTargetPreguntaIdPreguntaAndCuestionarioEntIdCuestionario(Long targetIdPregunta, Long idCuestionario);

    /**
     * Detección de ciclos: busca si targetPregunta ya es trigger
     * de alguna condición que eventualmente lleva a triggerPregunta.
     * Consulta directa de un nivel — suficiente para el 99% de casos.
     * Para grafos profundos se necesitaría un BFS/DFS en el service.
     */
    @Query("""
        SELECT COUNT(c) > 0
        FROM CondicionPreguntaEntity c
        WHERE c.triggerPregunta.idPregunta      = :targetPreguntaIdPregunta
          AND c.targetPregunta.idPregunta       = :triggerPreguntaIdPregunta
          AND c.cuestionarioEnt.idCuestionario  = :cuestionarioEntIdCuestionario
    """)
    boolean existeCicloDirecto(
    		@Param("triggerPreguntaIdPregunta") Long triggerIdPregunta, 
    		@Param("targetPreguntaIdPregunta") Long targetIdPregunta, 
    		@Param("cuestionarioEntIdCuestionario") Long idCuestionario);

}
 