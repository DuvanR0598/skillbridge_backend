package com.udea.skillbridge.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;

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
public class PuntuacionResultadoResponse {
	
    private Long id;
    private SkillTipo skill;

    // Dimensión gestionada evaluada (null = resultado general del cuestionario).
    // El estudiante no la ve mientras responde, pero sí en sus resultados finales.
    private Long idDimension;
    private String dimensionNombre;

    private Integer totalPuntaje;
    private Integer maxPuntuacionPosible;
    private BigDecimal porcentajePuntuacion;
    private SkillNivel nivel;
    private String descripcionNivel;
    private String caracteristicasObservables;

    // Planes de fortalecimiento asignados según el nivel
    private List<PlanFortalecimientoResponse> planesAsignados;
    private LocalDateTime calculatedAt;

}
