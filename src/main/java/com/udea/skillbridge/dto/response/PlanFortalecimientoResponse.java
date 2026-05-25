package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.udea.skillbridge.enums.PlanAxis;
import com.udea.skillbridge.enums.TipoAccion;

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
public class PlanFortalecimientoResponse {
	
    private Long id;
    private Long idPuntuacionMatrix;
    private PlanAxis planAxis;
    private String titulo;
    private String descripcion;
    private TipoAccion tipoAccion;
    private List<String> recursos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
