package com.udea.skillbridge.seguridad.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.seguridad.dto.request.CompletarPerfilRequest;
import com.udea.skillbridge.seguridad.dto.response.EstadoPerfilResponse;
import com.udea.skillbridge.seguridad.dto.response.ProgramaIngenieriaResponse;
import com.udea.skillbridge.seguridad.dto.response.UsuarioPerfilResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioPerfilEntity;
import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;
import com.udea.skillbridge.seguridad.mapper.IUsuarioPerfilMapper;
import com.udea.skillbridge.seguridad.repository.IUsuarioPerfilRepository;
import com.udea.skillbridge.seguridad.repository.IUsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioPerfilService {

	private final IUsuarioPerfilRepository perfilRepository;
	private final IUsuarioRepository usuarioRepository;
	private final IUsuarioPerfilMapper perfilMapper;

	// ── Ver mi perfil ───────────────────────────────────────────────

	@Transactional
	public UsuarioPerfilResponse getMyPerfil(Long idUsuario) {
		UsuarioPerfilEntity perfil = findOrCreatePerfil(idUsuario);
		return perfilMapper.toResponse(perfil);
	}

	// ── Ver perfil de otro usuario (Admin/Teacher) ──────────────────

	@Transactional(readOnly = true)
    public UsuarioPerfilResponse getPerfilByIdUsuario(Long idUsuario) {
    	UsuarioPerfilEntity perfil = perfilRepository.findByUsuarioEntId(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Perfil del usuario " + idUsuario + " no encontrado."
                ));
        return perfilMapper.toResponse(perfil);
	}
	
    // ── Completar / actualizar perfil ───────────────────────────────

    @Transactional
    public UsuarioPerfilResponse actualizarMyPerfil(Long idUsuario, CompletarPerfilRequest request) {

        UsuarioEntity usuarioEnt = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("UsuarioEntity", idUsuario));

        UsuarioPerfilEntity perfilEnt = findOrCreatePerfil(idUsuario);

        // Validar fecha de nacimiento: no puede ser futura ni mayor a 100 años
        if (request.getFechaNacimiento() != null) {
        	validarFechaDeNacimiento(request.getFechaNacimiento());
        }

        // Aplicar los cambios (solo campos que llegan con valor)
        perfilMapper.updateFromRequest(perfilEnt, request);

        UsuarioPerfilEntity guardar = perfilRepository.save(perfilEnt);

        // Actualizar perfilCompleto en UsuarioEntity si el perfil quedó completo
        boolean wasCompletado = Boolean.TRUE.equals(usuarioEnt.getPerfilCompleto());
        boolean isNowCompleto = guardar.isCompleto();

        if (!wasCompletado && isNowCompleto) {
            usuarioEnt.setPerfilCompleto(true);
            usuarioRepository.save(usuarioEnt);
            log.info("Perfil del usuario [{}] marcado como completo", idUsuario);
        }

        return perfilMapper.toResponse(guardar);
    }
    
    // ── Subir avatar ────────────────────────────────────────────────

    @Transactional
    public UsuarioPerfilResponse subirAvatar(Long idUsuario, MultipartFile file) {
    	validarArchivoDeImagen(file);

        UsuarioPerfilEntity perfilEnt = findOrCreatePerfil(idUsuario);

        // En producción esto iría a S3 / Google Cloud Storage.
        // Por ahora guardamos localmente en /uploads/avatars/.
        String filename = idUsuario + "_" + UUID.randomUUID() + getExtension(file);
        Path uploadDir = Path.of("uploads", "avatars");

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(),
                uploadDir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(
                "Error al guardar la imagen. Intente nuevamente.",
                "AVATAR_UPLOAD_FAILED"
            );
        }

        // Eliminar avatar anterior si existe
        if (perfilEnt.getAvatarUrl() != null) {
            try {
                Files.deleteIfExists(
                    Path.of(perfilEnt.getAvatarUrl().replace("/uploads/", "uploads/"))
                );
            } catch (IOException ignored) { /* no crítico */ }
        }

        String avatarUrl = "/uploads/avatars/" + filename;
        perfilEnt.setAvatarUrl(avatarUrl);

        // Sincronizar también en User.avatarUrl (se muestra en el JWT)
        usuarioRepository.findById(idUsuario).ifPresent(user -> {
            user.setAvatarUrl(avatarUrl);
            usuarioRepository.save(user);
        });

        log.info("Avatar actualizado para usuario {}", idUsuario);
        return perfilMapper.toResponse(perfilRepository.save(perfilEnt));
    }
    
    // ── Estado de completitud del perfil ────────────────────────────

    @Transactional(readOnly = true)
    public EstadoPerfilResponse getEstadoPerfil(Long idUsuario) {
        UsuarioPerfilEntity perfilEnt = findOrCreatePerfil(idUsuario);

        List<String> faltaRequerido = buildFaltaRequerido(perfilEnt);
        List<String> flataOpcional = buildFaltaOpcional(perfilEnt);

        String mensaje = perfilEnt.isCompleto()
                ? "¡Tu perfil está completo!"
                : "Completa los campos obligatorios para acceder a todas las funciones de Skill Bridge.";

        return EstadoPerfilResponse.builder()
                .perfilCompleto(perfilEnt.isCompleto())
                .porcentajeCompleto(perfilEnt.porcentajeCompleto())
                .camposObligatoriosFaltantes(faltaRequerido)
                .camposOpcionalesFaltantes(flataOpcional)
                .mensaje(mensaje)
                .build();
    }
    
    // ── Listar programas de ingeniería ──────────────────────────────

    public List<ProgramaIngenieriaResponse> listaProgramas() {
        return Arrays.stream(ProgramaIngenieria.values())
                .map(p -> ProgramaIngenieriaResponse.builder()
                        .valor(p)
                        .montrarNombre(p.getDisplayName())
                        .build())
                .toList();
    }
    
    
    // ── Metodos Privados ───────────────────────────────────────────────

	/**
	 * Busca el perfil del usuario o lo crea vacío si no existe. Esto garantiza que
	 * siempre haya un perfil desde el primer login.
	 */
	private UsuarioPerfilEntity findOrCreatePerfil(Long idUsuario) {
		return perfilRepository.findByUsuarioEntId(idUsuario).orElseGet(() -> {
			UsuarioEntity usuarionEnt = usuarioRepository.findById(idUsuario)
					.orElseThrow(() -> new ResourceNotFoundException("UsuarioEntity", idUsuario));

			UsuarioPerfilEntity newPerfil = UsuarioPerfilEntity.builder().usuarioEnt(usuarionEnt).build();

			UsuarioPerfilEntity guardar = perfilRepository.save(newPerfil);
			log.info("Perfil vacío creado para usuario {}", idUsuario);
			return guardar;
		});
	}
	
    private void validarFechaDeNacimiento(LocalDate dob) {
        LocalDate minDato = LocalDate.now().minusYears(100);
        LocalDate maxDato = LocalDate.now().minusYears(10);

        if (dob.isBefore(minDato) || dob.isAfter(maxDato)) {
            throw new BusinessException(
                "La fecha de nacimiento debe estar entre hace 100 años y hace 10 años.",
                "INVALID_DATE_OF_BIRTH"
            );
        }
    }
    
    private void validarArchivoDeImagen(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                "El archivo de imagen no puede estar vacío.",
                "EMPTY_FILE"
            );
        }

        List<String> tiposPermitidos = List.of(
            "image/jpeg", "image/png", "image/webp"
        );

        if (!tiposPermitidos.contains(file.getContentType())) {
            throw new BusinessException(
                "Formato no permitido. Use JPEG, PNG o WebP.",
                "INVALID_IMAGE_FORMAT"
            );
        }

        long maxSizeBytes = 5 * 1024 * 1024; // 5 MB
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(
                "La imagen no puede superar 5 MB.",
                "IMAGE_TOO_LARGE"
            );
        }
    }
    
    private String getExtension(MultipartFile file) {
        String contentType = file.getContentType();
        return switch (contentType != null ? contentType : "") {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            default           -> ".jpg";
        };
    }
    
    private List<String> buildFaltaRequerido(UsuarioPerfilEntity perfilEnt) {
        List<String> missing = new ArrayList<>();
        if (perfilEnt.getFechaNacimiento() == null)        missing.add("Fecha de nacimiento");
        if (perfilEnt.getProgramaIngenieria() == null)     missing.add("Programa de ingeniería");
        if (perfilEnt.getSemestreAcademico() == null)      missing.add("Semestre actual");
        return missing;
    }

    private List<String> buildFaltaOpcional(UsuarioPerfilEntity perfilEnt) {
        List<String> missing = new ArrayList<>();
        if (perfilEnt.getAvatarUrl() == null || perfilEnt.getAvatarUrl().isBlank())
            missing.add("Foto de perfil");
        if (perfilEnt.getGenero() == null)
            missing.add("Género");
        if (perfilEnt.getBiografia() == null || perfilEnt.getBiografia().isBlank())
            missing.add("Biografía");
        return missing;
    }

}
