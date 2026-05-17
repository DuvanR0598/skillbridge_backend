package com.udea.skillbridge.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Campos del encabezado que el docente puede modificar en BORRADOR.
 * Todos son opcionales — solo se actualiza lo que llega con valor.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarCuestionarioRequest {
	private String nombre;
	private String objetivo;
	private String instrucciones;
	private LocalDate fechaCreacion;
	private Boolean ordenAleatorio;   // El docente lo activa o desactiva aquí

}
