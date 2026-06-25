package com.udea.skillbridge.dto.request;

import java.util.List;

import com.udea.skillbridge.enums.PlanAxis;
import com.udea.skillbridge.enums.TipoAccion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class PlanFortalecimientoRequest {
	
    @NotNull(message = "El eje del plan es obligatorio (ACADEMICO, EXPERIMENTAL o PERSONAL)")
    private PlanAxis planAxis;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000)
    private String descripcion;

    @NotNull(message = "El tipo de acción es obligatorio (LEER, VER o PRACTICAR)")
    private TipoAccion tipoAccion;

    @Size(max = 10, message = "Máximo 10 recursos por plan")
    private List<@NotBlank @Size(max = 500) String> recursos;

}
