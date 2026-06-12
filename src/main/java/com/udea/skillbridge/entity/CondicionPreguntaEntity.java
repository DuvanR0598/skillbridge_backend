package com.udea.skillbridge.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Define UNA regla de ramificación:
 * "Si en la pregunta [triggerPregunta] el usuario elige [triggerOpcion],
 *  entonces mostrar la pregunta [targetPregunta]."
 *
 * Ejemplos reales:
 *   triggerPregunta   -> "¿Realizas actividad física?"
 *   triggerOpcion     -> "Sí"
 *   targetPregunta    -> "¿Qué tipo de actividad realizas?"
 *
 * Una opción puede tener MÚLTIPLES condiciones (dispara varias preguntas).
 * Una pregunta hija tiene MÁXIMO UNA condición de entrada (una sola puerta de entrada).
 */
@Entity
@Table(
	    name = "condicion_pregunta",
	    // Evita duplicados DENTRO de un mismo cuestionario: la misma opción no puede
	    // abrir la misma pregunta dos veces en ese cuestionario. La misma combinación
	    // SÍ puede repetirse en cuestionarios distintos (por eso incluye id_cuestionario).
	    uniqueConstraints = @UniqueConstraint(
	        name = "uk_target_condicion_opcion",
	        columnNames = {"id_cuestionario", "id_trigger_opcion", "id_target_pregunta"}
	    )
	)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder	
public class CondicionPreguntaEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	/**
     * El cuestionario al que pertenece esta condición.
     * Necesario para validar que trigger y target están en el mismo cuestionario.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", nullable = false)
    private CuestionarioEntity cuestionarioEnt;
    
    /**
     * La pregunta que actúa como disparador (trigger).
     * Debe ser de tipo que tenga opciones: VERDADER_FALSO, OPCION_UNICA, OPCION_MULTIPLE, LIKERT.
     * DESCRIPCION no puede ser trigger porque no tiene opciones seleccionables.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_trigger_pregunta", nullable = false)
    private PreguntaEntity triggerPregunta;
    
    /**
     * La opción específica que activa esta rama.
     * Debe pertenecer a triggerPregunta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_trigger_opcion", nullable = false)
    private OpcionPreguntaEntity triggerOpcion;
    
    /**
     * La pregunta que se revela cuando se cumple la condición.
     * No puede ser la misma que triggerPregunta (evita auto-referencia).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_target_pregunta", nullable = false)
    private PreguntaEntity targetPregunta;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
