package com.udea.skillbridge.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.udea.skillbridge.enums.EstadoCuestionario;

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
	private LocalDate fechaCreacion;
	private Boolean ordenAleatorio;
	private EstadoCuestionario estadoCuestionario;
	private LocalDateTime createdAt;
	private int totalPreguntas;
	private boolean editable;
}
