package com.udea.skillbridge.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cuestionario {
	
	@NotBlank(message = "El nombre del cuestionario es obligatorio")
	private String nombre;
	
	private String objetivo;
	private LocalDate fechaCreacion;
	private Boolean isDeleted = false;
}
