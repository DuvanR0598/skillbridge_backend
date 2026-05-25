package com.udea.skillbridge.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.udea.skillbridge.enums.PlanAxis;
import com.udea.skillbridge.enums.TipoAccion;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Plan de fortalecimiento: asociado a la matriz de valoración,
 * configurable por el docente/admin.
 */
@Entity
@Table(
    name = "plan_fortalecimiento",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_plan_matrix_axis",
        columnNames = {"id_puntuacion_matrix", "plan_axis"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFortalecimientoEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	// --- Relación con la matriz ---
	
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puntuacion_matrix", nullable = false, unique = true)
    private PuntuacionMatrixEntity puntuacionMatrixEnt;
    
    // --- Eje del plan ---
    
    /**
     * Eje al que pertenece este plan.
     * ACADEMICO     → talleres, micro-learning, razonamiento científico
     * EXPERIMENTAL  → proyectos colaborativos, simulaciones
     * PERSONAL      → mindfulness, escucha activa, hábitos
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_axis", nullable = false, length = 20)
    private PlanAxis planAxis;
    
    // --- Contenido del plan ---
    
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String descripcion;
    
    /**
     * Tipo de acción principal recomendada.
     * LEER   → artículos o libros
     * VER  → videos o charlas
     * PRACTICAR → ejercicio práctico
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private TipoAccion tipoAccion;

    /**
     * Lista de recursos (URLs o referencias bibliográficas).
     * Almacenada como colección de elementos en tabla separada.
     * Permite agregar/quitar recursos sin cambiar la entidad principal.
     */
    @ElementCollection
    @CollectionTable(
        name = "plan_recursos",
        joinColumns = @JoinColumn(name = "id_plan")
    )
    @Column(name = "recurso_url", length = 500)
    @Builder.Default
    private List<String> recursos = new ArrayList<>();
    
    // --- Trazabilidad ---
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
