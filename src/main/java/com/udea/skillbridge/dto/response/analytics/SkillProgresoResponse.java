package com.udea.skillbridge.dto.response.analytics;

import java.math.BigDecimal;

import com.udea.skillbridge.enums.SkillDimension;
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Progreso de un skill+dimensión comparando PRE vs POST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillProgresoResponse {
	
    private SkillTipo skill;
    private SkillDimension dimension;      // enum legado
    private Long idDimension;              // dimensión gestionada (tabla)
    private String dimensionNombre;        // nombre legible (null si no está vinculada)

    // PRE_TEST
    private Integer prePuntaje;
    private Integer preMaxPuntaje;
    private BigDecimal prePorcentaje;
    private SkillNivel preNivel;

    // POST_TEST
    private Integer postPuntaje;
    private Integer postMaxPuntaje;
    private BigDecimal postPorcentaje;
    private SkillNivel postNivel;

    // Delta de crecimiento
    private Integer puntajeDelta;           // postPuntaje - prePuntaje
    private BigDecimal porcentajeDelta;     // postPorcentaje - prePorcentaje
    private boolean nivelMejorado;          // subió de nivel
    private boolean alcanzoNivelAva;        // llegó a AVANZADO

}
