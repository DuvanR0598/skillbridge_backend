package com.udea.skillbridge.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;

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
    name = "puntuacion_resultado",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_dimension_evaluacion_skill_resultados",
        columnNames = {"id_evaluacion", "skill", "id_dimension"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntuacionResultadoEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Relaciones ---

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private EvaluacionEstudianteEntity evaluacionEnt;
    
    /**
     * Entrada de la matriz que determinó el nivel del estudiante.
     * Null si no hay entrada configurada para este skill+dimension+score.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puntuacion_matriz")
    private PuntuacionMatrixEntity puntuacionMatrizEnt;
    
    // --- Clasificación ---

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SkillTipo skill;

    /**
     * Dimensión gestionada (tabla `dimension`), copiada de la matriz al calcular.
     * Null = resultado global del skill (sin desglose por dimensión).
     * Solo la ve el coordinador.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dimension")
    private DimensionEntity dimensionEnt;
    
    // --- Puntaje ---

    @NotNull
    @Column(name = "total_puntaje", nullable = false)
    private Integer totalPuntaje;

    @NotNull
    @Column(name = "max_puntuacion_posible", nullable = false)
    private Integer maxPuntuacionPosible;

    /**
     * Porcentaje redondeado a 2 decimales.
     * Ej: totalScore=7 / maxPossible=10 → 70.00
     */
    @NotNull
    @Column(name = "porcentaje_puntuacion",
            nullable = false,
            precision = 5,
            scale = 2)
    private BigDecimal porcentajePuntuacion;
    
    // --- Nivel determinado ---

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SkillNivel nivel;

    @CreationTimestamp
    @Column(name = "calculated_at", updatable = false)
    private LocalDateTime calculatedAt;

}
