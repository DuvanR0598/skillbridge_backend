package com.udea.skillbridge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "plan_fortalecimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFortalecimientoEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlanFortalecimiento;
	
	// Asociado a una entrada de la matriz de valoración
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puntuacion_matrix", nullable = false)
    private PuntuacionMatrixEntity puntuacionMatrixEnt;
    
 // También asociado directamente al cuestionario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", nullable = false)
    private CuestionarioEntity cuestionarioEnt;

    @Column(nullable = false)
    private String titulo;           // Título del plan

    private String descripcion;     // Descripción detallada

    private String recursos;       // URLs o referencias a recursos

}
