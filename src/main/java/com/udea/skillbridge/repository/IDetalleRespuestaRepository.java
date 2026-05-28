package com.udea.skillbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.DetalleRespuestaEntity;

@Repository
public interface IDetalleRespuestaRepository extends JpaRepository<DetalleRespuestaEntity, Long> {
	
    boolean existsByEvaluacionEntIdAndPreguntaEntIdPregunta(Long idEvaluacion, Long idPregunta);
    
    Optional<DetalleRespuestaEntity> findByEvaluacionEntIdAndPreguntaEntIdPregunta(Long assessmentId, Long questionId);

    List<DetalleRespuestaEntity> findByEvaluacionEntId(Long idEvaluacion);
}
