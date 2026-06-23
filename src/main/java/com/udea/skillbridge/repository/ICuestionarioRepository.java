package com.udea.skillbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.CuestionarioEntity;

@Repository
public interface ICuestionarioRepository extends JpaRepository<CuestionarioEntity, Long>{
	
	// Todos los no eliminados
	@Query("SELECT c FROM CuestionarioEntity c WHERE c.estadoCuestionario != 'ELIMINADO'")
	List<CuestionarioEntity> findAllActivos();
    
    // Por ID solo si no está eliminado
	@Query("SELECT c FROM CuestionarioEntity c WHERE c.idCuestionario = :id " +
		       "AND c.estadoCuestionario != 'ELIMINADO'")
		Optional<CuestionarioEntity> findActivoById(@Param("id") Long id);

	// El nombre es único: lo usamos para generar un nombre de copia que no choque.
	boolean existsByNombre(String nombre);
   
    
    /**
     * Verifica si un cuestionario ya tiene respuestas asociadas.
     * IMPORTANTE: esto bloquea el borrado físico y determina si se puede
     * hacer borrado lógico vs archivar.
     *
     * Nota: la tabla 'respuestas_cuestionario' se implementará en el módulo
     * de respuestas. Por ahora retorna false (query comentada).
     */
    @Query("SELECT COUNT(r) > 0 FROM RespuestaCuestionario r WHERE r.cuestionario.id = :id")
    // boolean hasResponses(Long id);
    // Temporalmente:
    default boolean hasResponses(Long id) { return false; }

}
