package com.udea.skillbridge.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.udea.skillbridge.enums.SkillTipo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dimensión de un Power Skill, gestionada por el coordinador.
 * Ej: "Pensamiento Crítico" tiene dimensiones (Interpretación, Inferencia, Análisis...).
 *
 * El Skill al que pertenece se toma del enum {@link SkillTipo}.
 * Reemplaza progresivamente al enum SkillDimension para permitir que el
 * coordinador defina sus propias dimensiones.
 */
@Entity
@Table(
    name = "dimension",
    // Una misma skill no puede tener dos dimensiones con el mismo nombre
    uniqueConstraints = @UniqueConstraint(
        name = "uk_dimension_skill_nombre",
        columnNames = {"skill", "nombre"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    /** Power Skill al que pertenece la dimensión (PENSAMIENTO_CRITICO, ADAPTABILIDAD). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SkillTipo skill;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
