package com.udea.skillbridge.dto.request;

import java.util.List;

import com.udea.skillbridge.enums.TipoAccion;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * planAxis NO es modificable — es la clave de unicidad del plan dentro de la
 * matriz.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarPlanFortalecimientoRequest {
	
    @Size(max = 200)
    private String titulo;

    @Size(max = 2000)
    private String descripcion;

    private TipoAccion tipoAccion;

    @Size(max = 10)
    private List<String> recursos;

}
