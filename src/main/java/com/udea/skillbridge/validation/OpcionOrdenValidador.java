package com.udea.skillbridge.validation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.udea.skillbridge.dto.OpcionPregunta;
import com.udea.skillbridge.exception.CuestionarioException;

/**
 * Valida restricciones de las opciones que son transversales a todos los tipos.
 * Se llama DESPUÉS del validador específico del tipo.
 */
@Component
public class OpcionOrdenValidador {
	
	public void validate(List<OpcionPregunta> opcion) {
        if (opcion == null || opcion.isEmpty()) 
        	return;

        // REGLA 1: No puede haber dos opciones con el mismo ordenVisualizacion
        Set<Integer> orden = opcion.stream()
                .map(OpcionPregunta::getOrdenVisualizacion)
                .collect(Collectors.toSet());

        if (orden.size() != opcion.size()) {
            throw new CuestionarioException(
                "Existen opciones con el mismo número de orden (mostrarOrden duplicado). " +
                "Cada opción debe tener un orden único."
            );
        }
        
        // REGLA 2: Los órdenes deben ser números positivos
        boolean tieneOrdenInvalido = opcion.stream()
                .anyMatch(o -> o.getOrdenVisualizacion() == null || o.getOrdenVisualizacion() < 1);

        if (tieneOrdenInvalido) {
            throw new CuestionarioException(
                "El orden de visualización (ordenVisualizacion) debe ser un número positivo mayor a 0."
            );
        }
        
        // REGLA 3: Ninguna opción puede tener texto vacío
        boolean tieneTextoEnBlanco = opcion.stream()
                .anyMatch(o -> o.getTexto() == null || o.getTexto().isBlank());

        if (tieneTextoEnBlanco) {
            throw new CuestionarioException(
                "Todas las opciones deben tener texto. Se encontró al menos una opción sin texto."
            );
        }
	}

}
