package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;

import com.udea.skillbridge.enums.EstadoCuestionario;
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
public class CuestionarioResponse {
	
	private Long idCuestionario;
	private String nombre;
	private String objetivo;
	private String instrucciones;
	private Boolean ordenAleatorio;
	private EstadoCuestionario estadoCuestionario;
	private LocalDateTime createdAt;
	private String creadoPor;       // nombre y apellido de quien lo creó
	private int totalPreguntas;
	private boolean editable;

	// Ventana de disponibilidad
	private LocalDateTime fechaInicio;
	private LocalDateTime fechaFin;
	private boolean disponible;  // calculado: PUBLICADO y dentro de la ventana

	// Tiempo límite para responder, en minutos. null = sin límite.
	private Integer tiempoLimiteMinutos;

	// Programa objetivo. null = general (todos los estudiantes).
	private ProgramaIngenieria programaObjetivo;
	private String programaObjetivoNombre; // displayName, ej. "Ingeniería de Sistemas" (null = "General")
}
