package com.udea.skillbridge.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.udea.skillbridge.dto.Cuestionario;
import com.udea.skillbridge.dto.PreguntaCuestionario;
import com.udea.skillbridge.enums.EstadoCuestionario;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.mapper.ICuestionarioMapper;
import com.udea.skillbridge.persistence.entity.CuestionarioEntity;
import com.udea.skillbridge.persistence.entity.PreguntaCuestionarioEntity;
import com.udea.skillbridge.persistence.entity.PreguntaEntity;
import com.udea.skillbridge.persistence.repository.ICuestionarioRepository;
import com.udea.skillbridge.persistence.repository.IPreguntaCuestionarioRepository;
import com.udea.skillbridge.persistence.repository.IPreguntaRepository;
import com.udea.skillbridge.service.ICuestionarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuestionarioServiceImpl implements ICuestionarioService{
	
	private final ICuestionarioRepository cuestionarioRepository;
	private final ICuestionarioMapper cuestionarioMapper;
	private final IPreguntaRepository preguntaRepository;
	private final IPreguntaCuestionarioRepository pqRepository;

	// *****************************************
	// CREAR CUESTIONARIO
	// *****************************************
	
	@Override
	public Cuestionario crearCuestionario(Cuestionario cuestionario) {
		log.info("Creando cuestionario: {}", cuestionario.getNombre());
		CuestionarioEntity cuestionarioEnt = cuestionarioMapper.toEntity(cuestionario);
		CuestionarioEntity guardarEntity = cuestionarioRepository.save(cuestionarioEnt);
		log.info("Cuestionario creado con ID: {}", guardarEntity.getIdCuestionario());
		return cuestionarioMapper.toDto(guardarEntity);
	}
	
	// *****************************************
	// AGREGAR PREGUNTA AL CUESTIONARIO
	// *****************************************
	
	@Override
	public void addPretuntaToCuestinario(Long idCuestionario, PreguntaCuestionario preguntaCuestionario) {
		
		// 1. Buscar el cuestionario (lanza 404 si no existe o está borrado)
        CuestionarioEntity cuestionarioEnt = findActiveById(idCuestionario);

        // 2. REGLA DE NEGOCIO: solo se pueden agregar preguntas en estado DRAFT
        if (!cuestionarioEnt.isEditable()) {
            throw new CuestionarioException(
                "No se pueden agregar preguntas. El cuestionario no está en estado DRAFT."
            );
        }

        // 3. Verificar que la pregunta existe
        PreguntaEntity preguntaEnt = preguntaRepository.findById(preguntaCuestionario.getIdpregunta())
                .orElseThrow(() -> new CuestionarioException(
                    "Pregunta no encontrada: " + preguntaCuestionario.getIdpregunta(),
                    HttpStatus.NOT_FOUND
                ));

        // 4. Verificar que la pregunta no esté ya en el cuestionario
        if (pqRepository.existsByIdIdCuestionarioAndIdIdPregunta(idCuestionario, preguntaCuestionario.getIdpregunta())) {
            throw new CuestionarioException("La pregunta ya está asociada a este cuestionario.");
        }

        // 5. Crear la relación
        PreguntaCuestionarioEntity.IdPreguntaCuestionario pqId =
                new PreguntaCuestionarioEntity.IdPreguntaCuestionario(idCuestionario, preguntaCuestionario.getIdpregunta());

        PreguntaCuestionarioEntity pqEnt = PreguntaCuestionarioEntity.builder()
                .id(pqId)
                .cuestionarioEnt(cuestionarioEnt)
                .preguntaEnt(preguntaEnt)
                .obligatoria(preguntaCuestionario.getObligatoria())
                .peso(preguntaCuestionario.getPeso())
                .build();

        pqRepository.save(pqEnt);
        log.info("Pregunta {} agregada al cuestionario {}", preguntaEnt.getIdPregunta(), idCuestionario);
	}
	
	// ***********************************************
	// COMPLETAR CUESTIONARIO (MARCAR COMO COMPLETE)
	// ***********************************************
	
	@Override
	public Cuestionario cuestionarioCompleto(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActiveById(idCuestionario);

        if (!EstadoCuestionario.BORRADOR.equals(cuestionarioEnt.getEstadoCuestionario())) {
            throw new CuestionarioException("Solo los cuestionarios en estado BORRADOR pueden completarse.");
        }

        // REGLA: Mínimo 2 preguntas
        int count = pqRepository.countByIdIdCuestionario(idCuestionario);
        if (count < 2) {
            throw new CuestionarioException(
                "El cuestionario debe tener al menos 2 preguntas. Actualmente tiene: " + count + " pregunta"
            );
        }

        cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.COMPLETO);
        CuestionarioEntity saved = cuestionarioRepository.save(cuestionarioEnt);
        log.info("Cuestionario {} marcado como COMPLETE", idCuestionario);
        return cuestionarioMapper.toDto(saved);
	}
	
	
	// *****************************************
	// LISTAR
	// *****************************************
	
	@Override
	public Cuestionario getById(Long idCuestionario) {
		Optional<CuestionarioEntity> cuestionarioOptEnt = Optional.of(cuestionarioRepository.findById(idCuestionario).
				orElseThrow(() -> new CuestionarioException(
						"Cuestionario no encontrado: " + idCuestionario, HttpStatus.NOT_FOUND)));
		
		return cuestionarioMapper.toDto(cuestionarioOptEnt.get());
	}
	
	@Override
	public List<Cuestionario> listarAllCuestionarios() {
		return cuestionarioRepository.findAll()
				.stream()
				.map(cuestionarioMapper::toDto)
				.toList();
	}

	@Override
	public List<Cuestionario> listarCuestionariosActivos() {
		return cuestionarioRepository.findByIsDeletedFalse()
				.stream()
				.map(cuestionarioMapper::toDto)
				.toList();
	}
	
	private CuestionarioEntity findActiveById(Long idCuestionario) {
		return cuestionarioRepository.findByidCuestionarioAndIsDeletedFalse(idCuestionario)
				.orElseThrow(() -> new CuestionarioException(
						"Cuestionario no encontrado: " + idCuestionario, HttpStatus.NOT_FOUND));
	}
	
	// *****************************************
	// BORRADO LOGICO
	// *****************************************

	@Override
	public void borradoLogico(Long idCuestionario) {
		CuestionarioEntity cuestionarioEnt = findActiveById(idCuestionario);

        // REGLA: No se puede borrar si ya tiene respuestas
        if (cuestionarioRepository.hasResponses(idCuestionario)) {
            throw new CuestionarioException(
                "No se puede eliminar. El cuestionario ya tiene respuestas registradas. " +
                "Considere archivarlo en su lugar."
            );
        }

        cuestionarioEnt.setIsDeleted(true);
        cuestionarioEnt.setEstadoCuestionario(EstadoCuestionario.ELIMINADO);
        cuestionarioRepository.save(cuestionarioEnt);
        log.info("Cuestionario con uuid [{}] borrado lógicamente", idCuestionario); 
		
	}
}