package com.udea.skillbridge.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.udea.skillbridge.enums.TipoPregunta;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pregunta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPregunta;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "tipo_pregunta", nullable = false, length = 30)
    private TipoPregunta tipoPregunta;
	
	@Column(name = "texto", nullable = false)
    private String texto;                     // Texto de la pregunta
	
	@Column(name = "imagen_url")
    private String imagenUrl;                // URL de imagen opcional
    
    private String ayuda;                  // Texto de ayuda para el estudiante
    
    @Column(name = "max_opciones")
    private Integer maxOpciones;          // Máximo de opciones seleccionables
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
    
    /**
     * Las opciones de respuesta de esta pregunta.
     * Para DESCRIPCION no habrá opciones.
     */
    @OneToMany(
        mappedBy = "preguntaEnt",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("mostrar_orden ASC")  // Orden de visualización
    @Builder.Default
    private List<OpcionPreguntaEntity> opcionPregunta = new ArrayList<>();
    
    /**
     * La matriz de valoración asociada a esta pregunta.
     */
    @OneToMany(
        mappedBy = "preguntaEnt",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<PuntuacionMatrixEntity> puntuacionMatrices = new ArrayList<>();
    
    @OneToMany(mappedBy = "preguntaEnt", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PreguntaCuestionarioEntity> preguntaCuestionarioEnt = new ArrayList<>();

}
