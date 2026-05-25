package com.udea.skillbridge.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "evaluacion_estudiante",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_intento_fase_cuestionario_evaluacion_estudiante",
        columnNames = {
            "id_estudiante", "id_cuestionario",
            "evaluacion_fase", "numero_intento"
        }
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionEstudianteEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // --- Quién responde y a qué ---

    @NotNull
    @Column(name = "id_estudiante", nullable = false)
    private Long studentId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", nullable = false)
    private CuestionarioEntity cuestionarioEnt;
    
    // --- Fase y estado ---

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "evaluacion_fase", nullable = false, length = 20)
    private evaluacionFaseEntity evaluacionFase;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private evaluacionEstadoEntity estado = AssessmentStatus.IN_PROGRESS;
    
    /**
     * Número de intento. Permite múltiples ciclos del modelo pedagógico.
     * Intento 1 = primera aplicación, Intento 2 = luego del plan, etc.
     */
    @Builder.Default
    @Column(name = "numero_intento", nullable = false)
    private Integer numeroIntento = 1;
    
    // --- Trazabilidad temporal ---

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    
    // ── Relaciones ──────────────────────────────────────────────────

    @Builder.Default
    @OneToMany(
        mappedBy = "evaluacion",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<DetalleRespuestaEntity> respuestas = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(
        mappedBy = "assessment",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<PuntuacionresultadoEntity> resultados = new ArrayList<>();
    
    // ── Método de dominio ───────────────────────────────────────────

    public boolean isEditable() {
        return AssessmentStatus.IN_PROGRESS.equals(this.status);
    }

}
