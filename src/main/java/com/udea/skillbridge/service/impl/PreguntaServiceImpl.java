package com.udea.skillbridge.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.ActualizarPesoOpcionesRequest;
import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.dto.response.PaginaResponse;
import com.udea.skillbridge.dto.response.PreguntaResponse;
import com.udea.skillbridge.entity.DimensionEntity;
import com.udea.skillbridge.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.mapper.IPreguntaMapper;
import com.udea.skillbridge.repository.IDimensionRepository;
import com.udea.skillbridge.repository.IPreguntaRepository;
import com.udea.skillbridge.repository.PreguntaSpec;
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
	private final PreguntaValidadorFactory validadorFactory;
	private final OpcionOrdenValidador opcionOrdenValidador;
	private final IPreguntaMapper preguntaMapper;
	private final IDimensionRepository dimensionRepository;
	
	// *****************************************
    //  SUBIR IMAGEN DE LA PREGUNTA
    // *****************************************

	@Override
	public String subirImagen(MultipartFile file) {
		validarArchivoDeImagen(file);

		String filename = UUID.randomUUID() + getExtension(file);
		Path uploadDir = Path.of("uploads", "preguntas");

		try {
			Files.createDirectories(uploadDir);
			Files.copy(file.getInputStream(), uploadDir.resolve(filename),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			throw new BusinessException(
					"Error al guardar la imagen. Intente nuevamente.", "IMAGE_UPLOAD_FAILED");
		}

		String url = "/uploads/preguntas/" + filename;
		log.info("Imagen de pregunta guardada en {}", url);
		return url;
	}

	private void validarArchivoDeImagen(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("El archivo de imagen no puede estar vacío.", "IMAGE_EMPTY");
		}
		String contentType = file.getContentType();
		if (contentType == null
				|| !(contentType.equals("image/jpeg")
					|| contentType.equals("image/png")
					|| contentType.equals("image/webp"))) {
			throw new BusinessException(
					"Formato no permitido. Usa JPEG, PNG o WebP.", "INVALID_IMAGE_FORMAT");
		}
		long maxSizeBytes = 5L * 1024 * 1024; // 5 MB
		if (file.getSize() > maxSizeBytes) {
			throw new BusinessException("La imagen no puede superar 5 MB.", "IMAGE_TOO_LARGE");
		}
	}

	private String getExtension(MultipartFile file) {
		String original = file.getOriginalFilename();
		if (original != null && original.contains(".")) {
			return original.substring(original.lastIndexOf('.'));
		}
		// Por defecto según el tipo
		String ct = file.getContentType();
		if ("image/png".equals(ct)) return ".png";
		if ("image/webp".equals(ct)) return ".webp";
		return ".jpg";
	}

	@Override
	public PreguntaResponse actualizarImagen(Long idPregunta, String imagenUrl) {
		PreguntaEntity pregunta = findEntityById(idPregunta);
		String anterior = pregunta.getImagenUrl();
		String nueva = (imagenUrl != null && !imagenUrl.isBlank()) ? imagenUrl.trim() : null;

		// Borrar el archivo local anterior si cambió o se quitó.
		if (anterior != null && anterior.startsWith("/uploads/preguntas/")
				&& !anterior.equals(nueva)) {
			try {
				Files.deleteIfExists(Path.of(anterior.replace("/uploads/", "uploads/")));
			} catch (IOException ignored) { /* no crítico */ }
		}

		pregunta.setImagenUrl(nueva);
		PreguntaEntity guardar = preguntaRepository.save(pregunta);
		log.info("Imagen de la pregunta [{}] actualizada → {}", idPregunta, nueva);
		return preguntaMapper.toResponse(guardar);
	}

	// *****************************************
    //  CREAR PREGUNTA
    // *****************************************

	@Override
	public PreguntaResponse crearPregunta(PreguntaRequest request) {
		log.info("Creando pregunta tipo: {}", request.getTipoPregunta());
		
		// PASO 1: Validación específica del tipo (Strategy Pattern)
        // El factory elige el validador correcto según el tipo de pregunta.
		validadorFactory.getValidador(request.getTipoPregunta()).validador(request);

        // PASO 2: Validación transversal de las opciones (orden, texto vacío)
        // Solo aplica si hay opciones (DESCRIPCION no las tiene)
		opcionOrdenValidador.validate(request.getOpcionPreguntaRequest());
		
		// PASO 3: Construir la entidad
		PreguntaEntity preguntaEnt = preguntaMapper.toEntity(request);

		// PASO 3.b: Asignar dimensión si viene en el request
		if (request.getIdDimension() != null) {
			preguntaEnt.setDimension(buscarDimension(request.getIdDimension()));
		}

		// PASO 4: Construir y asociar las opciones de respuesta
        // Cada opción conoce a su pregunta (relación bidireccional)
        if (request.getOpcionPreguntaRequest() != null && !request.getOpcionPreguntaRequest().isEmpty()) {
        	List<OpcionPreguntaEntity> opciones = request.getOpcionPreguntaRequest().stream()
        			.map(optReq -> {
        				OpcionPreguntaEntity opcion = preguntaMapper.toOpcionPreguntaEntity(optReq);
        				opcion.setPreguntaEnt(preguntaEnt);  // Establecer relación bidireccional
        				return opcion;
        			})
        			.toList();
        	
        	preguntaEnt.getOpcionPregunta().addAll(opciones);
        }
        // PASO 5: Persistir (cascade guarda las opciones automáticamente)
        PreguntaEntity guardar = preguntaRepository.save(preguntaEnt);
        log.info("Pregunta creada con ID: {}", guardar.getIdPregunta());

        return preguntaMapper.toResponse(guardar);
	}
	
	// *****************************************
    //  OBTENER POR ID
    // *****************************************
	
	@Override
	public PreguntaResponse findById(Long idPregunta) {
		return preguntaMapper.toResponse(findEntityById(idPregunta));
	}
	
	// *****************************************
    //  LISTAR TODAS
    // *****************************************
	
	@Override
	public List<PreguntaResponse> listarTodo() {
		return preguntaRepository.findAll()
                .stream()
                .map(preguntaMapper::toResponse)
                .toList();
	}
	
	// *****************************************
    //  LISTAR POR TIPO
    // *****************************************
	
	@Override
	public List<PreguntaResponse> listarPorTipo(TipoPregunta tipoPregunta) {
		return preguntaRepository.findByTipoPregunta(tipoPregunta)
				.stream()
				.map(preguntaMapper::toResponse)
				.toList();
	}

	// *****************************************
    //  LISTAR PAGINADO (banco de preguntas)
    // *****************************************

	@Override
	public PaginaResponse<PreguntaResponse> listarPaginado(int page, int size, TipoPregunta tipoPregunta, String texto, SkillTipo skill, Long idDimension) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "idPregunta"));
		Specification<PreguntaEntity> spec = Specification
				.where(PreguntaSpec.porTipo(tipoPregunta))
				.and(PreguntaSpec.porTexto(texto))
				.and(PreguntaSpec.porSkill(skill))
				.and(PreguntaSpec.porDimension(idDimension));
		Page<PreguntaEntity> pagina = preguntaRepository.findAll(spec, pageable);
		return PaginaResponse.of(pagina.map(preguntaMapper::toResponse));
	}
	
    // **************************************************
    //  ACTUALIZAR PESOS DE OPCIONES
    //  (permitido aunque el cuestionario esté COMPLETO)
    // **************************************************

	@Override
	public PreguntaResponse actualizarPesosOpciones(Long idPregunta, ActualizarPesoOpcionesRequest request) {
		PreguntaEntity preguntaEnt = findEntityById(idPregunta);

		request.getPesos().forEach((idOpcion, newPeso) -> {
            if (newPeso < 0) {
                throw new BusinessException(
                    "El peso no puede ser negativo. Opción: " + idOpcion,
                    "INVALID_WEIGHT"
                );
            }
            preguntaEnt.getOpcionPregunta().stream()
                    .filter(o -> o.getId().equals(idOpcion))
                    .findFirst()
                    .ifPresentOrElse(
                        o -> o.setPeso(newPeso),
                        () -> { throw new ResourceNotFoundException("Opción", idOpcion); }
                    );
        });

        return preguntaMapper.toResponse(preguntaRepository.save(preguntaEnt));
	}
	
	// **************************************************
    //  ELIMINAR PREGUNTA
    //  Solo si NO está asociada a ningún cuestionario
    // **************************************************
	
	@Override
	public void eliminarPregunta(Long preguntaId) {
		PreguntaEntity preguntaEnt = findEntityById(preguntaId);
		
		// Verificar que la pregunta no esté en uso en ningún cuestionario
        // (la relación inversa en PreguntaCuestionario nos lo indica)
        boolean isEnUso = !preguntaEnt.getPreguntaCuestionarioEnt().isEmpty();

        if (isEnUso) {
        	throw new BusinessException(
                    "No se puede eliminar. La pregunta está asociada a " +
                    preguntaEnt.getPreguntaCuestionarioEnt().size() + " cuestionario(s).",
                    "QUESTION_IN_USE"
                );
        }

        preguntaRepository.delete(preguntaEnt);
        log.info("Pregunta {} eliminada", preguntaId);
		
	}
	
	// **************************************************
    //  ASIGNAR / CAMBIAR DIMENSIÓN DE UNA PREGUNTA
    // **************************************************

	@Override
	public PreguntaResponse asignarDimension(Long idPregunta, Long idDimension) {
		PreguntaEntity pregunta = findEntityById(idPregunta);
		// idDimension null = desasignar
		pregunta.setDimension(idDimension == null ? null : buscarDimension(idDimension));
		log.info("Pregunta {} -> dimensión {}", idPregunta, idDimension);
		return preguntaMapper.toResponse(preguntaRepository.save(pregunta));
	}

	// **************************************************
    //  METODOS PRIVADOS
    // **************************************************

	public PreguntaEntity findEntityById(Long id) {
        return preguntaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta", id));
    }

	private DimensionEntity buscarDimension(Long idDimension) {
        return dimensionRepository.findById(idDimension)
                .orElseThrow(() -> new ResourceNotFoundException("Dimensión", idDimension));
    }
}
