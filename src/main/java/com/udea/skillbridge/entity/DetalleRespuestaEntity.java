package com.udea.skillbridge.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
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
    name = "detalle_respuesta",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_responder_pregunta_evaluacion",
        columnNames = {"id_evaluacion", "id_pregunta"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleRespuestaEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relaciones ──────────────────────────────────────────────────

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private EvaluacionEstudianteEntity evaluacionEnt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private PreguntaEntity preguntaEnt;
    
    /**
     * IDs de las opciones seleccionadas.
     * Para FALSO_VERDADERO y OPCION_UNICA: 1 elemento.
     * Para OPCION_MULTIPLE: N elementos.
     * Para LIKERT: 1 elemento.
     * Para DESCRIPCION: vacío (la respuesta va en respuestaAbierta).
     */
    @ElementCollection
    @CollectionTable(
        name = "opciones_respuestas_seleccionadas",
        joinColumns = @JoinColumn(name = "id_respuesta")
    )
    @Column(name = "id_opcion")
    @Builder.Default
    private List<Long> idsOpcionesSeleccionadas = new ArrayList<>();
    
    /**
     * Respuesta de texto libre. Solo aplica para preguntas DESCRIPCION.
     * Para todos los demás tipos siempre es null.
     */
    @Column(name = "respuesta_abierta", length = 3000)
    private String respuestaAbierta;
    
    /**
     * Puntaje obtenido en esta pregunta.
     * Calculado como suma de pesos de las opciones seleccionadas.
     * Para DESCRIPCION siempre es 0.
     */
    @Builder.Default
    @Column(name = "puntaje_obtenido", nullable = false)
    private Integer puntajeObtenido = 0;
    
    @CreationTimestamp
    @Column(name = "answered_at", updatable = false)
    private LocalDateTime answeredAt;

}
