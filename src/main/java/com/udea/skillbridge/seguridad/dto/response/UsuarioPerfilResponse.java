package com.udea.skillbridge.seguridad.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.udea.skillbridge.seguridad.enums.Genero;
import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioPerfilResponse {

	private Long id;
	private Long idUsuario;

	// Información personal
	private String avatarUrl;
	private LocalDate fechaNacimiento;
	private Genero genero;
	private String visualizacionGenero; // "Masculino", "Femenino", etc.
	private String biografia;

	// Información académica
	private ProgramaIngenieria programaIngenieria;
	private String visualizacionProgramaIngenieria; // "Ingeniería de Sistemas"
	private String codigoProgramaIngenieria;        // código oficial, ej. "504"
	private Integer semestreAcademico;

	// Completitud
	private boolean perfilCompleto;
	private int porcentajeCompleto;
	private LocalDateTime updatedAt;

}
