package com.udea.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.enums.TipoPregunta;

@Repository
public interface IPreguntaRepository extends JpaRepository<PreguntaEntity, Long>  {
	
	List<PreguntaEntity> findByTipoPregunta(TipoPregunta tipoPregunta);

}
