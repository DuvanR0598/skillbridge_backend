package com.udea.skillbridge.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.udea.skillbridge.enums.EstadoCuestionario;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cuestionario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuestionarioEntity {

	//@Id
	//@GeneratedValue(strategy = GenerationType.UUID)
	//@Column(name = "id_Cuestionario", columnDefinition = "VARCHAR(36)")
	// private UUID idCuestionario; para postgres
	//private String idCuestionario;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCuestionario;

	@Column(nullable = false, unique = true)
	private String nombre; // Nombre del cuestionario

	private String objetivo; // Finalidad u objetivo

	@CreationTimestamp
	@Column(name = "fecha_Creacion")
	private LocalDate fechaCreacion; // Fecha en que se crea el cuestionario

	@Enumerated(EnumType.STRING)
	@Column(name = "estado_cuestionario", nullable = false, length = 20)
	@Builder.Default
	private EstadoCuestionario estadoCuestionario = EstadoCuestionario.BORRADOR;

	/**
     * Borrado lógico: true cuando el cuestionario se "elimina".
     * REGLA: Solo se puede borrar lógicamente si NO tiene respuestas.
     */
	
	@Column(name = "is_deleted")
	@Builder.Default
	private Boolean isDeleted = false;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// --- RELACIONES ---

	/**
	 * Relación con la tabla intermedia que une cuestionario <-> pregunta. cascade =
	 * ALL: cuando persisto el cuestionario, persisto también sus entradas en la
	 * tabla intermedia. orphanRemoval = true: si quito un item de esta lista, se
	 * borra de BD.
	 */

	@OneToMany(mappedBy = "cuestionarioEnt", 
			cascade = CascadeType.ALL, 
			orphanRemoval = true, 
			fetch = FetchType.LAZY)
	@Builder.Default
	private List<PreguntaCuestionarioEntity> preguntasCuestionario = new ArrayList<>();
	
	// --- VALIDACIONES ---
	
	/**
     * Verifica si el cuestionario permite modificaciones estructurales
     * (agregar/quitar preguntas).
     * Solo está permitido en estado DRAFT.
     */
	
	public boolean isEditable() {
        return EstadoCuestionario.BORRADOR.equals(this.estadoCuestionario) && !Boolean.TRUE.equals(this.isDeleted);
    }
	
	/**
     * Verifica si cumple el mínimo de preguntas para pasar a COMPLETE.
     */
    public boolean tienePreguntasMinimas() {
        return this.preguntasCuestionario.size() >= 2;
    }
}
