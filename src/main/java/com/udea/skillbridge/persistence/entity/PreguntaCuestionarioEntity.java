package com.udea.skillbridge.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla intermedia entre Cuestionario y Pregunta.
 * Tiene datos propios (peso, obligatoria), por eso NO usamos @ManyToMany.
 * Usamos @EmbeddedId para la clave compuesta (idCuestionario + idPregunta).
 */

@Entity
@Table(name = "pregunta_cuestionario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaCuestionarioEntity {
	
	/**
     * Clave primaria compuesta: id del cuestionario + id de la pregunta.
     * Esta clase embebida va dentro de esta misma entidad
     */
	
	@EmbeddedId
	private IdPreguntaCuestionario id;
	
	// Relación con el cuestionario 
	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCuestionario")   // Mapea la parte "cuestionarioId" del EmbeddedId
    @JoinColumn(name = "id_cuestionario")
    private CuestionarioEntity cuestionarioEnt;
	
	// Relación con la pregunta (lado "muchos")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPregunta")         // Mapea la parte "questionId" del EmbeddedId
    @JoinColumn(name = "id_pregunta")
    private PreguntaEntity preguntaEnt;
    
    // ¿La pregunta es obligatoria en este cuestionario?
    @Column(nullable = false)
    @Builder.Default
    private Boolean obligatoria = true;
    
    // Peso de la pregunta dentro de este cuestionario (puede variar entre cuestionarios)
    @Column(nullable = false)
    @Builder.Default
    private Integer peso = 1;

	
	// --- Clase interna para la clave compuesta ---
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode   // IMPORTANTE: Hibernate necesita equals/hashCode en claves compuestas
    public static class IdPreguntaCuestionario implements java.io.Serializable {
		private static final long serialVersionUID = 1L;

		@Column(name = "id_cuestionario")
        private Long idCuestionario;

        @Column(name = "id_pregunta")
        private Long idPregunta;
    }

}
