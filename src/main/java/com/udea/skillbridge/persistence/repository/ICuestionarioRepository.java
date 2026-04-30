package com.udea.skillbridge.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.persistence.entity.CuestionarioEntity;

@Repository
public interface ICuestionarioRepository extends JpaRepository<CuestionarioEntity, Long>{
	
	// Solo los no borrados lógicamente
    List<CuestionarioEntity> findByIsDeletedFalse();
    
    // Por estado, excluyendo borrados
    //List<CuestionarioEntity> findByStatusAndIsDeletedFalse(EstadoCuestionario estado);
    
    /**
     * Verifica si un cuestionario ya tiene respuestas asociadas.
     * IMPORTANTE: esto bloquea el borrado físico y determina si se puede
     * hacer borrado lógico vs archivar.
     *
     * Nota: la tabla 'respuestas_cuestionario' se implementará en el módulo
     * de respuestas. Por ahora retorna false (query comentada).
     */
//    @Query("SELECT COUNT(r) > 0 FROM RespuestaCuestionario r WHERE r.cuestionario.id = :id")
//    // boolean hasResponses(Long id);
//    // Temporalmente:
//    default boolean hasResponses(Long id) { return false; }

}
