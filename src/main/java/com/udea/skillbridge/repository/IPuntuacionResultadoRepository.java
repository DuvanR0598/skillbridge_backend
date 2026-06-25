package com.udea.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.PuntuacionResultadoEntity;

@Repository
public interface IPuntuacionResultadoRepository extends JpaRepository<PuntuacionResultadoEntity, Long> {
	
    List<PuntuacionResultadoEntity> findByEvaluacionEntId(Long assessmentId);

}
