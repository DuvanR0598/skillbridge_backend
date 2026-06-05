package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evento de actividad reciente para el dashboard. Se deriva de las tablas
 * existentes (evaluaciones del estudiante, cuestionarios del coordinador/admin)
 * según el rol del usuario autenticado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadRecienteResponse {

    private Long id;
    private String type;        // identificador del tipo de evento
    private String description;  // texto a mostrar
    private String icon;         // nombre del Material Icon
    private LocalDateTime date;   // fecha del evento (ISO) — el frontend la formatea
}
