package com.udea.skillbridge.seguridad.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.udea.skillbridge.seguridad.entity.PermisoEntity;
import com.udea.skillbridge.seguridad.entity.RolEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.enums.AuthProvider;
import com.udea.skillbridge.seguridad.enums.TipoIdentificacion;
import com.udea.skillbridge.seguridad.enums.TipoPermiso;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.seguridad.repository.IRolRepository;
import com.udea.skillbridge.seguridad.repository.IUsuarioRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Inicializa roles, permisos y el usuario admin por defecto.
 * Se ejecuta al iniciar la aplicación si no existen datos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
	
	private final IRolRepository rolRepository;
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        initPermissionsAndRoles();
        initAdminUser();
    }

    private void initPermissionsAndRoles() {
        if (rolRepository.count() > 0) {
            log.info("Roles ya inicializados — omitiendo DataInitializer");
            return;
        }

        // ── Crear todos los permisos ─────────────────────────────────
        Map<TipoPermiso, PermisoEntity> permisos = new HashMap<>();
        for (TipoPermiso tipo : TipoPermiso.values()) {
        	PermisoEntity perm = PermisoEntity.builder()
                    .nombre(tipo)
                    .descripcion(tipo.name().replace("_", " ").toLowerCase())
                    .build();
            entityManager.persist(perm);
            permisos.put(tipo, perm);
        }
        entityManager.flush();

        // ── ADMIN — todos los permisos ───────────────────────────────
        RolEntity adminRol = RolEntity.builder()
                .nombre(TipoRol.ROLE_ADMIN)
                .descripcion("Administrador del sistema")
                .permisos(new HashSet<>(permisos.values()))
                .build();

        // ── COORDINADOR — permisos de coordinador ───────────────────────────
        Set<PermisoEntity> permisosCoordinador = new HashSet<>(Set.of(
            permisos.get(TipoPermiso.QUESTIONNAIRE_CREATE),
            permisos.get(TipoPermiso.QUESTIONNAIRE_READ),
            permisos.get(TipoPermiso.QUESTIONNAIRE_UPDATE),
            permisos.get(TipoPermiso.QUESTIONNAIRE_PUBLISH),
            permisos.get(TipoPermiso.QUESTIONNAIRE_DELIVER),
            permisos.get(TipoPermiso.QUESTION_CREATE),
            permisos.get(TipoPermiso.QUESTION_READ),
            permisos.get(TipoPermiso.QUESTION_UPDATE),
            permisos.get(TipoPermiso.QUESTION_DELETE),
            permisos.get(TipoPermiso.CONDITION_MANAGE),
            permisos.get(TipoPermiso.SCORE_MATRIX_CREATE),
            permisos.get(TipoPermiso.SCORE_MATRIX_READ),
            permisos.get(TipoPermiso.SCORE_MATRIX_UPDATE),
            permisos.get(TipoPermiso.PLAN_CREATE),
            permisos.get(TipoPermiso.PLAN_READ),
            permisos.get(TipoPermiso.PLAN_UPDATE),
            permisos.get(TipoPermiso.ASSESSMENT_READ_ALL),
            permisos.get(TipoPermiso.ANALYTICS_READ_GROUP)
        ));

        RolEntity coordinadorRol = RolEntity.builder()
                .nombre(TipoRol.ROLE_COORDINADOR)
                .descripcion("Coordinador")
                .permisos(permisosCoordinador)
                .build();

        // ── ESTUDIANTE — permisos de estudiante ─────────────────────────
        Set<PermisoEntity> permisosEstudiante = new HashSet<>(Set.of(
            permisos.get(TipoPermiso.QUESTIONNAIRE_READ),
            permisos.get(TipoPermiso.QUESTIONNAIRE_DELIVER),
            permisos.get(TipoPermiso.ASSESSMENT_START),
            permisos.get(TipoPermiso.ASSESSMENT_SUBMIT),
            permisos.get(TipoPermiso.ASSESSMENT_READ_OWN),
            permisos.get(TipoPermiso.ANALYTICS_READ_OWN),
            permisos.get(TipoPermiso.SCORE_MATRIX_READ),
            permisos.get(TipoPermiso.PLAN_READ)
        ));

        RolEntity estudianteRol = RolEntity.builder()
                .nombre(TipoRol.ROLE_ESTUDIANTE)
                .descripcion("Estudiante")
                .permisos(permisosEstudiante)
                .build();

        rolRepository.saveAll(List.of(adminRol, coordinadorRol, estudianteRol));
        log.info("Roles y permisos inicializados correctamente");
    }

    private void initAdminUser() {
        if (usuarioRepository.existsByEmail("admin@skillbridge.edu.co")) {
            return;
        }

        RolEntity adminRol = rolRepository.findByNombre(TipoRol.ROLE_ADMIN)
                .orElseThrow();

        UsuarioEntity admin = UsuarioEntity.builder()
                .tipoIdentificacion(TipoIdentificacion.CC)
                .numeroIdentificacion("0000000000")
                .nombre("Admin")
                .apellido("SkillBridge")
                .email("admin@skillbridge.edu.co")
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .authProvider(AuthProvider.LOCAL)
                .emailVerificado(true)
                .activado(true)
                .roles(Set.of(adminRol))
                .build();

        usuarioRepository.save(admin);
        log.info("Usuario admin creado: admin@skillbridge.edu.co / Admin123!");
    }

}
