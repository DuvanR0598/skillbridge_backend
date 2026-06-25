package com.udea.skillbridge.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.enums.TipoPregunta;

@Repository
public interface IPreguntaRepository
        extends JpaRepository<PreguntaEntity, Long>,
                JpaSpecificationExecutor<PreguntaEntity> {

	List<PreguntaEntity> findByTipoPregunta(TipoPregunta tipoPregunta);

	Page<PreguntaEntity> findByTipoPregunta(TipoPregunta tipoPregunta, Pageable pageable);

}
