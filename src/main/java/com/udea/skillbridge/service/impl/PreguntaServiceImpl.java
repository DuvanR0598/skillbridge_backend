package com.udea.skillbridge.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.udea.skillbridge.dto.OpcionPregunta;
import com.udea.skillbridge.dto.Pregunta;
import com.udea.skillbridge.mapper.IPreguntaMapper;
import com.udea.skillbridge.persistence.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.persistence.entity.PreguntaEntity;
import com.udea.skillbridge.persistence.repository.IPreguntaRepository;
import com.udea.skillbridge.service.IPreguntaService;
import com.udea.skillbridge.validation.OpcionOrdenValidador;
import com.udea.skillbridge.validation.PreguntaValidadorFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreguntaServiceImpl implements IPreguntaService {
	
	private final IPreguntaRepository preguntaRepository;
	private final PreguntaValidadorFactory preguntaValidadorFactory;
	private final OpcionOrdenValidador opcionOrdenValidador;
	private final IPreguntaMapper preguntaMapper;
	
	// ─────────────────────────────────────────
    //  CREAR PREGUNTA
    // ─────────────────────────────────────────

	@Override
	public Pregunta crearPregunta(Pregunta pregunta) {
		log.info("Creando pregunta tipo: {}", pregunta.getTipoPregunta());
		
		// PASO 1: Validación específica del tipo (Strategy Pattern)
        // El factory elige el validador correcto según el tipo de pregunta.
		preguntaValidadorFactory
				.getValidador(pregunta.getTipoPregunta())
                .validador(pregunta);

        // PASO 2: Validación transversal de las opciones (orden, texto vacío)
        // Solo aplica si hay opciones (DESCRIPCION no las tiene)
		opcionOrdenValidador.validate(pregunta.getOpcionPregunta());
		
		// PASO 3: Construir la entidad
		PreguntaEntity preguntaEnt = preguntaMapper.toEntity(pregunta);
		
		// Guardar pregunta para generar el id_pregunta
		PreguntaEntity savedPregunta = preguntaRepository.save(preguntaEnt);
		log.info("Pregunta guardada con ID: {}", savedPregunta.getIdPregunta());
		
		
		// PASO 4: Construir y asociar las opciones de respuesta
        // Cada opción conoce a su pregunta (relación bidireccional)
        if (pregunta.getOpcionPregunta() != null && !pregunta.getOpcionPregunta().isEmpty()) {
            List<OpcionPreguntaEntity> opciones = pregunta.getOpcionPregunta().stream()
                    .map(optReq -> construirOpcion(optReq, savedPregunta))
                    .toList();

            savedPregunta.getOpcionPregunta().addAll(opciones);
        }
        // PASO 5: Persistir (cascade guarda las opciones automáticamente)
        PreguntaEntity guardar = preguntaRepository.save(savedPregunta);

        return toResponse(guardar);
	}
	
	private OpcionPreguntaEntity construirOpcion (OpcionPregunta req, PreguntaEntity pregunta) {
		return OpcionPreguntaEntity.builder()
				.preguntaEnt(pregunta)
				.texto(req.getTexto())
				.isCorrecta(req.getIsCorrecta())
				.peso(req.getPeso())
				.ordenVisualizacion(req.getOrdenVisualizacion())
				.build();
	}
	
	private Pregunta toResponse(PreguntaEntity preguntaEnt) {
		List<OpcionPregunta> opcPregunta = preguntaEnt.getOpcionPregunta() == null
				? Collections.emptyList()
				: preguntaEnt.getOpcionPregunta().stream()
					.map(this::toOpcionPregunta)
					.toList();
		
		return Pregunta.builder()
				.tipoPregunta(preguntaEnt.getTipoPregunta())
				.texto(preguntaEnt.getTexto())
				.imagenUrl(preguntaEnt.getImagenUrl())
				.descripcion(preguntaEnt.getDescripcion())
				.ayuda(preguntaEnt.getAyuda())
				.maxOpciones(preguntaEnt.getMaxOpciones())
				.createdAt(preguntaEnt.getCreatedAt())
				.opcionPregunta(opcPregunta)
				.build();
	}
	
	private OpcionPregunta toOpcionPregunta(OpcionPreguntaEntity opcPregunta) {
		return OpcionPregunta.builder()
				.texto(opcPregunta.getTexto())
				.isCorrecta(opcPregunta.getIsCorrecta())
				.peso(opcPregunta.getPeso())
				.ordenVisualizacion(opcPregunta.getOrdenVisualizacion())
				.build();
	}
	


}
