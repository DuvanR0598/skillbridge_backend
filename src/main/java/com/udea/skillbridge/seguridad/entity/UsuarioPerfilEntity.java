package com.udea.skillbridge.seguridad.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.udea.skillbridge.seguridad.enums.Genero;
import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;

import jakarta.persistence.Column;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario_perfil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioPerfilEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relación con User (dueño de la FK) ──────────────────────────

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private UsuarioEntity usuarioEnt;
    
 // ── Información personal ────────────────────────────────────────

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    private Genero genero;

    @Size(max = 500)
    @Column(length = 500)
    private String biografia;
    
    // ── Información académica ───────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "programa_ingenieria", length = 60)
    private ProgramaIngenieria programaIngenieria;

    /**
     * Semestre actual del estudiante (1 al 10).
     */
    @Min(1) @Max(10)
    @Column(name = "semestre_academico")
    private Integer semestreAcademico;
    
    // ── Trazabilidad ────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ── Método de dominio ───────────────────────────────────────────

    /**
     * Determina si el perfil tiene los campos mínimos obligatorios.
     * Se usa para calcular perfilCompleto en UsuarioEntity.
     *
     */
    public boolean isCompleto() {
        return fechaNacimiento != null
            && programaIngenieria != null
            && semestreAcademico != null;
    }
    
    /**
     * Calcula el porcentaje de completitud del perfil.
     * Útil para la barra de progreso del frontend.
     */
    public int porcentajeCompleto() {
        int total = 5; // número de campos opcionales + obligatorios que cuentan
        int filled = 0;

        if (avatarUrl != null     && !avatarUrl.isBlank())       filled++;
        if (fechaNacimiento != null)                             filled++;
        if (genero != null)                                      filled++;
        if (programaIngenieria != null)                          filled++;
        if (semestreAcademico != null)                           filled++;

        return (int) Math.round((filled * 100.0) / total);
    }
}
