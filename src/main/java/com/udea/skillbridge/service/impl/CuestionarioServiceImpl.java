package com.udea.skillbridge.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.udea.skillbridge.dto.Cuestionario;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.mapper.ICuestionarioMapper;
import com.udea.skillbridge.persistence.entity.CuestionarioEntity;
import com.udea.skillbridge.persistence.repository.ICuestionarioRepository;
import com.udea.skillbridge.service.ICuestionarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuestionarioServiceImpl implements ICuestionarioService{
	
	private final ICuestionarioRepository cuestionarioRepository;
	private final ICuestionarioMapper cuestionarioMapper;

	// ─────────────────────────────────────────
	// CREAR CUESTIONARIO
	// ─────────────────────────────────────────
	@Override
	public Cuestionario crearCuestionario(Cuestionario cuestionario) {
		log.info("Creando cuestionario: {}", cuestionario.getNombre());
		CuestionarioEntity cuestionarioEnt = cuestionarioMapper.toEntity(cuestionario);
		CuestionarioEntity guardarEntity = cuestionarioRepository.save(cuestionarioEnt);
		log.info("Cuestionario creado con ID: {}", guardarEntity.getIdCuestionario());
		return cuestionarioMapper.toDto(guardarEntity);
	}
	
	// ─────────────────────────────────────────
	// LISTAR
	// ─────────────────────────────────────────
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
}