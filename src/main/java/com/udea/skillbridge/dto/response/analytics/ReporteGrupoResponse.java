package com.udea.skillbridge.dto.response.analytics;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reporte completo del grupo para el docente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteGrupoResponse {
	
    private Long idCuestionario;
    private String nombreCuestionario;
    private LocalDateTime generatedAt;

    // Estadísticas de participación
    private Long totalEstudiantesConPreTest;
    private Long totalEstudiantesConPostTest;
    private Long totalEstudiantesCompletados;  // tienen ambos

    // Análisis por dimensión
    private List<AnalisisDimensionalResponse> analisiDimensional;

    // Tabla comparativa estudiante por estudiante
    private List<NivelEstudianteResumenResponse> resumenEstudiantes;

    // Dimensiones críticas del grupo
    private AnalisisDimensionalResponse dimensionMasCritica;
    private AnalisisDimensionalResponse dimensionMasMejorada;

    // Porcentaje del grupo que logró AVANZADO en al menos 1 dimensión
    private Double pcAlcanzadoCualquierNivelAvanzado;

    // Porcentaje del grupo que logró AVANZADO en TODAS las dimensiones
    private Double pcTotalAvanzado;

}
