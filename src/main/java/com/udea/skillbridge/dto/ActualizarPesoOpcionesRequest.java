package com.udea.skillbridge.dto;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPesoOpcionesRequest {
	
	@NotNull(message = "El mapa de pesos no puede ser nulo")
    private Map<Long, Integer> pesos;

}
