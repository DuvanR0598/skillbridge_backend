package com.udea.skillbridge.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.enums.TipoPregunta;

@Repository
public interface IPreguntaRepository extends JpaRepository<PreguntaEntity, Long>  {

	List<PreguntaEntity> findByTipoPregunta(TipoPregunta tipoPregunta);

	// Variantes paginadas (banco de preguntas)
	Page<PreguntaEntity> findByTipoPregunta(TipoPregunta tipoPregunta, Pageable pageable);

	/**
	 * Búsqueda paginada del banco de preguntas con filtros opcionales:
	 * tipo (null = todos) y texto (null/vacío = sin filtro de texto, busca en el enunciado).
	 */
	@Query("""
		SELECT p FROM PreguntaEntity p
		WHERE (:tipo IS NULL OR p.tipoPregunta = :tipo)
		  AND (:texto IS NULL OR LOWER(p.texto) LIKE LOWER(CONCAT('%', :texto, '%')))
	""")
	Page<PreguntaEntity> buscar(
			@Param("tipo") TipoPregunta tipo,
			@Param("texto") String texto,
			Pageable pageable);

}
