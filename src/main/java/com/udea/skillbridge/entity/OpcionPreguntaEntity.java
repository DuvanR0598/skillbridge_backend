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

@Entity
@Table(name = "opcion_pregunta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcionPreguntaEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private PreguntaEntity preguntaEnt;
	
	@Column(nullable = false)
    private String texto;
	
	/**
     * ¿Es la opción correcta?
     * Para TRUE_FALSE: solo una es correcta.
     * Para MULTIPLE_CHOICE: varias pueden ser correctas.
     * Para DESCRIPTION: siempre null.
     */
    @Column(name = "is_correcta")
    private Boolean isCorrecta;
    
    // Peso de esta opción específica (para scoring)
    @Column(nullable = false)
    @Builder.Default
    private Integer peso = 0;
    
    // Orden de visualización
    @Column(name = "mostrar_orden", nullable = false)
    private Integer ordenVisualizacion;

}
