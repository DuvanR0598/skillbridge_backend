package com.udea.skillbridge.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.persistence.entity.PreguntaCuestionarioEntity;
import com.udea.skillbridge.persistence.entity.PreguntaCuestionarioEntity.IdPreguntaCuestionario;

@Repository
public interface IPreguntaCuestionarioRepository extends JpaRepository<PreguntaCuestionarioEntity, IdPreguntaCuestionario> {

	//List<PreguntaCuestionarioEntity> findByIdCuestionarioEntityId(Long cuestionarioId);
	
	//boolean existsByIdCuestionarioIdAndIdPreguntaId(Long cuestionarioId, Long preguntaId);
	
	//int countByIdCuestionarioId(Long cuestionarioId);
}
