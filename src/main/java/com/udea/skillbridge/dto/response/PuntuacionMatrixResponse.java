package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.udea.skillbridge.dto.request.PlanFortalecimientoRequest;
import com.udea.skillbridge.enums.NivelBloom;
import com.udea.skillbridge.enums.SkillDimension;
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
public class PuntuacionMatrixResponse {
	
    private Long id;
    private Long idCuestionario;

    // Pregunta asociada (null si es evaluación global)
    private Long idPregunta;
    private String textoPregunta;

    private SkillTipo skill;
    private SkillDimension dimension;     // enum legado — null = global
    private Long idDimension;             // dimensión gestionada (tabla)
    private String dimensionNombre;       // nombre legible de la dimensión gestionada
    private SkillNivel nivel;

    private Integer minPuntaje;
    private Integer maxPuntaje;

    private String descripcion;
    private String caracteristicasObservables;

    private List<NivelBloom> nivelesBloom;

    // Planes por eje — puede haber de 0 a 3
    private List<PlanFortalecimientoRequest> planFortalecimiento;

    // Indica si los 3 ejes ya tienen plan configurado
    private boolean fullConfigurado;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
