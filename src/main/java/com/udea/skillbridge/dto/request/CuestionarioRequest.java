package com.udea.skillbridge.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CuestionarioRequest {
	
	@NotBlank(message = "El nombre del cuestionario es obligatorio")
	private String nombre;
	
	private String objetivo;
	private String instrucciones;
	private LocalDate fechaCreacion;
	private Boolean ordenAleatorio = false;
}
