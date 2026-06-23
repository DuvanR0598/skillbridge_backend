package com.udea.skillbridge.seguridad.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.udea.skillbridge.seguridad.enums.Genero;
import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;
import com.udea.skillbridge.seguridad.enums.TipoIdentificacion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
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
	
	// ── Identificación (solo para usuarios sin documento, ej. Google) ──

    private TipoIdentificacion tipoIdentificacion;

    @Pattern(
        regexp = "^[a-zA-Z0-9]*$",
        message = "El número de identificación solo puede contener letras y números"
    )
    @Size(max = 30, message = "El número de identificación no puede superar 30 caracteres")
    private String numeroIdentificacion;

	// ── Información personal ────────────────────────────────────────

    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @JsonProperty("dateOfBirth")
    private LocalDate fechaNacimiento;

    @JsonProperty("gender")
    private Genero genero;

    @Size(max = 500, message = "La biografía no puede superar 500 caracteres")
    @JsonProperty("biography")
    private String biografia;

    // ── Información académica ───────────────────────────────────────

    @JsonProperty("engineeringProgram")
    private ProgramaIngenieria programaIngenieria;

    @Min(value = 1, message = "El semestre mínimo es 1")
    @Max(value = 10, message = "El semestre máximo es 10")
    @JsonProperty("academicSemester")
    private Integer semestreAcademico;

}
