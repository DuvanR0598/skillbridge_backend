package com.udea.skillbridge.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
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
	private Boolean ordenAleatorio = false;

	// Ventana de disponibilidad (fecha + hora)
	private LocalDateTime fechaInicio;
	private LocalDateTime fechaFin;

	// Tiempo límite para responder, en minutos. null = sin límite.
	@Min(value = 1, message = "El tiempo límite debe ser de al menos 1 minuto")
	private Integer tiempoLimiteMinutos;
}
