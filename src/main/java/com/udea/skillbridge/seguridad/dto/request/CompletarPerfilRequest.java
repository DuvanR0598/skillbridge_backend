package com.udea.skillbridge.seguridad.dto.request;

import java.time.LocalDate;

import com.udea.skillbridge.seguridad.enums.Genero;
import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request para completar o actualizar el perfil.
 * Todos los campos son opcionales en el PATCH —
 * solo se actualizan los que llegan con valor.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletarPerfilRequest {
	
	// ── Información personal ────────────────────────────────────────

    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate fechaNacimiento;

    private Genero genero;

    @Size(max = 500, message = "La biografía no puede superar 500 caracteres")
    private String biografia;

    // ── Información académica ───────────────────────────────────────

    private ProgramaIngenieria programaIngenieria;

    @Min(value = 1, message = "El semestre mínimo es 1")
    @Max(value = 10, message = "El semestre máximo es 10")
    private Integer semestreAcademico;

}
