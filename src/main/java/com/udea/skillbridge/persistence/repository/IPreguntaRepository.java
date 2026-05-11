package com.udea.skillbridge.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.persistence.entity.PreguntaEntity;

@Repository
public interface IPreguntaRepository extends JpaRepository<PreguntaEntity, Long>  {
	
	List<PreguntaEntity> findByTipoPregunta(TipoPregunta tipoPregunta);

}
