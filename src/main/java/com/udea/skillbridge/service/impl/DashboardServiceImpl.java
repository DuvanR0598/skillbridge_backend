package com.udea.skillbridge.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.skillbridge.dto.response.ActividadRecienteResponse;
import com.udea.skillbridge.entity.CuestionarioEntity;
import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;
import com.udea.skillbridge.enums.EstadoCuestionario;
import com.udea.skillbridge.enums.EvaluacionEstado;
import com.udea.skillbridge.repository.ICuestionarioRepository;
import com.udea.skillbridge.repository.IEvaluacionEstudianteRepository;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.service.IDashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final IEvaluacionEstudianteRepository evaluacionRepository;
    private final ICuestionarioRepository cuestionarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ActividadRecienteResponse> getActividadReciente(UsuarioEntity usuario) {
        // El estudiante ve su propia actividad de evaluaciones.
        if (usuario.hasRole(TipoRol.ROLE_ESTUDIANTE)) {
            return actividadDeEstudiante(usuario.getId());
        }
        // Admin y Coordinador ven la actividad de gestión de cuestionarios.
        return actividadDeGestion();
    }

    // ── Estudiante: sus evaluaciones ────────────────────────────────
    private List<ActividadRecienteResponse> actividadDeEstudiante(Long idEstudiante) {
        return evaluacionRepository
                .findTop10ByIdEstudianteOrderByStartedAtDesc(idEstudiante)
                .stream()
                .map(this::mapEvaluacion)
                .toList();
    }

    private ActividadRecienteResponse mapEvaluacion(EvaluacionEstudianteEntity ev) {
        String fase = ev.getEvaluacionFase().name();           // PRE_TEST / POST_TEST
        String cuestionario = ev.getCuestionarioEnt().getNombre();

        String type;
        String icon;
        String description;
        LocalDateTime date;

        if (ev.getEstado() == EvaluacionEstado.COMPLETADO) {
            type = "assessment_completed";
            icon = "assignment_turned_in";
            description = fase + " completado — " + cuestionario;
            date = ev.getFinishedAt() != null ? ev.getFinishedAt() : ev.getStartedAt();
        } else if (ev.getEstado() == EvaluacionEstado.ABANDONADO) {
            type = "assessment_abandoned";
            icon = "assignment_late";
            description = fase + " abandonado — " + cuestionario;
            date = ev.getStartedAt();
        } else {
            type = "assessment_started";
            icon = "assignment";
            description = fase + " en progreso — " + cuestionario;
            date = ev.getStartedAt();
        }

        return ActividadRecienteResponse.builder()
                .id(ev.getId())
                .type(type)
                .icon(icon)
                .description(description)
                .date(date)
                .build();
    }

    // ── Coordinador / Admin: cuestionarios ──────────────────────────
    private List<ActividadRecienteResponse> actividadDeGestion() {
        return cuestionarioRepository.findAllActivos()
                .stream()
                .sorted(Comparator.comparing(
                        CuestionarioEntity::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(this::mapCuestionario)
                .toList();
    }

    private ActividadRecienteResponse mapCuestionario(CuestionarioEntity c) {
        boolean publicado = c.getEstadoCuestionario() == EstadoCuestionario.PUBLICADO;

        return ActividadRecienteResponse.builder()
                .id(c.getIdCuestionario())
                .type(publicado ? "questionnaire_published" : "questionnaire_created")
                .icon(publicado ? "campaign" : "note_add")
                .description((publicado ? "Cuestionario publicado — " : "Cuestionario actualizado — ")
                        + c.getNombre())
                .date(c.getUpdatedAt() != null ? c.getUpdatedAt() : c.getCreatedAt())
                .build();
    }
}
