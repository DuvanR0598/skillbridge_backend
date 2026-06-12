package com.udea.skillbridge.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.udea.skillbridge.enums.NivelBloom;
import com.udea.skillbridge.enums.PlanAxis;
// DimensionEntity está en el mismo paquete (entity), no requiere import
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(
	    name = "puntuacion_matrices",
	    /*
	     * Unicidad compuesta: no puede haber dos entradas con el mismo
	     * cuestionario + skill + nivel + pregunta (null incluido).
	     * Garantiza que cada nivel de cada skill tiene una sola definición.
	     */
	    uniqueConstraints = @UniqueConstraint(
	        name = "uk_entrada_unica_de_matriz",
	        columnNames = {"id_cuestionario", "skill", "id_dimension", "nivel", "id_pregunta"}
	    )
	)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class PuntuacionMatrixEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // --- Relaciones ---
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", nullable = false)
    private CuestionarioEntity cuestionarioEnt;

    /**
     * Pregunta específica a la que aplica esta entrada de la matriz.
     * NULL = aplica al cuestionario completo (evaluación global del skill).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    private PreguntaEntity preguntaEnt;
    
    // --- Clasificación pedagógica ---
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillTipo skill;
    
    /**
     * Dimensión gestionada por el coordinador (tabla `dimension`).
     * NULL = evaluación global del skill sin desglose por dimensión.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dimension")
    private DimensionEntity dimensionEnt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillNivel nivel;
    
    // --- Rango de Puntaje ---
    
    @NotNull
    @Min(0)
    @Column(name = "min_puntaje", nullable = false)
    private Integer minPuntaje;

    @NotNull
    @Min(0)
    @Column(name = "max_puntaje", nullable = false)
    private Integer maxPuntaje;
    
    // --- Descriptores pedagogicos ---
    
    /**
     * Descripción del nivel: qué puede hacer el estudiante en este nivel.
     * Ej: "Analiza información desde diferentes perspectivas, relaciona conceptos..."
     */
    private String descripcion;
    
    /**
     * Comportamientos observables en este nivel
     * Ej: "Reconoce cambios y nuevas situaciones, aunque puede presentar dificultad..."
     */
    @Column(name = "caracteristicas_observables", length = 1000)
    private String caracteristicasObservables;
    
    /**
     * Procesos cognitivos de Bloom asociados a este nivel.
     * Almacenados en tabla separada para permitir múltiples valores.
     * BASICO → [RECORDAR, ENTENDER]
     * INTERMEDIO → [APLICAR, ANALIZAR]
     * AVANZADO → [EVALUAR, CREAR]
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "nivel_matrix_bloom",
        joinColumns = @JoinColumn(name = "id_matrix")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_bloom", length = 20)
    @Builder.Default
    private List<NivelBloom> nivelesBloom = new ArrayList<>();
    
    // --- Trazabilidad ---
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // --- Planes de fortalecimiento (1 por eje = max 3) ---
    
    /**
     * Relación 1:N con StrengtheningPlan.
     * Cada nivel tiene hasta 3 planes: ACADEMIC, EXPERIENTIAL, PERSONAL.
     * cascade = ALL: eliminar la entrada de la matriz elimina sus planes.
     */
    @OneToMany(
        mappedBy = "puntuacionMatrixEnt",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<PlanFortalecimientoEntity> planFortalecimientoEnt = new ArrayList<>();
    
    // --- Validación de dominio ---S
    
    public boolean hasRangoValido() {
        return minPuntaje != null
            && maxPuntaje != null
            && minPuntaje < maxPuntaje;
    }
    
    public boolean hasPlanForAxis(PlanAxis axis) {
        return planFortalecimientoEnt.stream()
                .anyMatch(p -> axis.equals(p.getPlanAxis()));
    }

}
