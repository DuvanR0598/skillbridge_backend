package com.udea.skillbridge.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.persistence.entity.PreguntaCuestionarioEntity;
import com.udea.skillbridge.persistence.entity.PreguntaCuestionarioEntity.IdPreguntaCuestionario;

@Repository
public interface IPreguntaCuestionarioRepository extends JpaRepository<PreguntaCuestionarioEntity, IdPreguntaCuestionario> {

	boolean existsByIdIdCuestionarioAndIdIdPregunta(Long idCuestionario, Long idPregunta);
	
	Optional<PreguntaCuestionarioEntity> findByIdIdCuestionarioAndIdIdPregunta(Long idCuestionario, Long idPregunta);
	
	int countByIdIdCuestionario(Long cuestionarioId);
	
	//List<PreguntaCuestionarioEntity> findByIdCuestionarioEntityId(Long cuestionarioId);
}
