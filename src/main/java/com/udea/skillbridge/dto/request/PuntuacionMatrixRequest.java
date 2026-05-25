package com.udea.skillbridge.dto.request;

import java.util.List;

import com.udea.skillbridge.enums.NivelBloom;
import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;

import jakarta.validation.constraints.Min;
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
public class PuntuacionMatrixRequest {
	
    // Pregunta específica — null = evaluación global
    private Long idPregunta;

    @NotNull(message = "El skill es obligatorio")
    private SkillTipo skill;

    // Dimensión del skill — null = evaluación global del skill completo
    private SkillDimension dimension;

    @NotNull(message = "El nivel es obligatorio")
    private SkillNivel nivel;

    @NotNull(message = "El puntaje mínimo es obligatorio")
    @Min(value = 0, message = "El puntaje mínimo no puede ser negativo")
    private Integer minPuntaje;

    @NotNull(message = "El puntaje máximo es obligatorio")
    @Min(value = 1, message = "El puntaje máximo debe ser mayor a 0")
    private Integer maxPuntaje;

    private String descripcion;
    private String caracteristicasObservables;

    /**
     * Niveles de Bloom asociados a esta entrada.
     * Recomendación según el PDF:
     *   BASICO     → [RECORDAR, ENTENDER]
     *   INTERMEDIO → [APLICAR, ANALIZAR]
     *   AVANZADO   → [EVALUAR, CREAR]
     */
    @Size(max = 6, message = "Máximo 6 niveles de Bloom")
    private List<NivelBloom> nivelBloom;

}
