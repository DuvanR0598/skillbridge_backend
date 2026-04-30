package com.udea.skillbridge.persistence.entity;

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
 * Matriz de valoración: define contra qué concepto se evalúa
 * cada pregunta y qué nivel corresponde a cada rango de puntaje.
 */
@Entity
@Table(name = "puntuacion_matrices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntuacionMatrixEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPuntMatrix;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private PreguntaEntity preguntaEnt;
	
	@Column(nullable = false)
    private String concepto;     // Ej: "Pensamiento crítico", "Adaptabilidad"
	
	@Column(name = "min_puntaje", nullable = false)
    private Integer minPuntaje;   // Puntaje mínimo para este nivel
	
	@Column(name = "max_puntaje", nullable = false)
    private Integer maxPuntaje;   // Puntaje máximo para este nivel
	
	@Column(nullable = false)
    private String nivel;       // Ej: "Básico", "Intermedio", "Avanzado"

    private String descripcion; // Descripción del nivel

}
