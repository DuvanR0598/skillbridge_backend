package com.udea.skillbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado del tiempo límite de una evaluación.
 * - tiempoLimiteMinutos = null → sin límite (segundosRestantes = null).
 * - segundosRestantes → segundos que le quedan al estudiante en este instante,
 *   calculados desde el ancla persistida (tiempoInicioConteo). 0 = tiempo agotado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiempoConteoResponse {

    private Integer tiempoLimiteMinutos;
    private Long segundosRestantes;
    private boolean tiempoAgotado;
}
