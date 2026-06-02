package com.udea.skillbridge.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.dto.response.analytics.AnalisisDimensionalResponse;
import com.udea.skillbridge.dto.response.analytics.DistribucionNivelesResponse;
import com.udea.skillbridge.dto.response.analytics.EscalamientoResponse;
import com.udea.skillbridge.dto.response.analytics.EstudianteQueNecesitaApoyoResponse;
import com.udea.skillbridge.dto.response.analytics.HistorialIntentosResponse;
import com.udea.skillbridge.dto.response.analytics.InformeProgresoEstudianteResponse;
import com.udea.skillbridge.dto.response.analytics.ReporteGrupoResponse;
import com.udea.skillbridge.dto.response.analytics.ResumenCuestionarioResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.service.IAnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/analitica")
@RequiredArgsConstructor
public class AnalyticsController {
	
	private final IAnalyticsService analyticsService;
	
    /**
     * Reporte individual PRE vs POST de un estudiante.
     */
    @GetMapping("/estudiante/{idEstudiante}/cuestionario/{idCuestionario}/progreso")
    public ResponseEntity<ApiResponse<InformeProgresoEstudianteResponse>> getProgresoEstudiante(
            @PathVariable Long idEstudiante,
            @PathVariable Long idCuestionario,
            @AuthenticationPrincipal UsuarioEntity usuarioActual) {

        // Estudiante solo puede ver su propio progreso
        // Admin y Coordinador pueden ver el de cualquiera
        if (usuarioActual.hasRole(TipoRol.ROLE_ESTUDIANTE)
                && !usuarioActual.getId().equals(idEstudiante)) {
            throw new BusinessException(
                "No tiene permiso para ver el progreso de otro estudiante.",
                "ACCESS_DENIED"
            );
        }

        return ResponseEntity.ok(ApiResponse.ok(
            analyticsService.getProgresoEstudiante(idEstudiante, idCuestionario)
        ));
    }
    
    /**
     * Reporte de grupo para el docente.
     */
    @GetMapping("/cuestionario/{idCuestionario}/reporte-grupo")
    public ResponseEntity<ApiResponse<ReporteGrupoResponse>> getReporteGrupo(@PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getReporteGrupo(idCuestionario)));
    }
 
    /**
     * Análisis por dimensión del grupo.
     */
    @GetMapping("/cuestionario/{idCuestionario}/analisis-dimension")
    public ResponseEntity<ApiResponse<List<AnalisisDimensionalResponse>>> getAnalisisDimensional(
            @PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(
            analyticsService.getAnalisisDimensional(idCuestionario)
        ));
    }
    
    /**
     * Decisión de escalamiento de un estudiante (certificar vs reiniciar).
     */
    @GetMapping("/estudiantes/{idEstudiante}/cuestionario/{idCuestionario}/escalar")
    public ResponseEntity<ApiResponse<EscalamientoResponse>> getEscalamiento(
            @PathVariable Long idEstudiante,
            @PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(
            analyticsService.getEscalamiento(idEstudiante, idCuestionario)
        ));
    }
    
    /**
     * Resumen ejecutivo del cuestionario.
     */
    @GetMapping("/cuestionario/{idCuestionario}/resumen")
    public ResponseEntity<ApiResponse<ResumenCuestionarioResponse>> getResumen(
            @PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(
            analyticsService.getResumen(idCuestionario)
        ));
    }
    
    /**
     * Historial de intentos de un estudiante.
     */
    @GetMapping("/estudiantes/{idEstudiante}/cuestionario/{idCuestionario}/historial")
    public ResponseEntity<ApiResponse<List<HistorialIntentosResponse>>> getHistorialIntentos(
            @PathVariable Long idEstudiante,
            @PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(
            analyticsService.getHistorialIntentos(idEstudiante, idCuestionario)
        ));
    }
    
    /**
     * Distribución de niveles por fase.
     */
    @GetMapping("/cuestionario/{idCuestionario}/distribucion-nivel")
    public ResponseEntity<ApiResponse<List<DistribucionNivelesResponse>>> getDistribucionNiveles(
            @PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(
            analyticsService.getDistribucionNiveles(idCuestionario)
        ));
    }
    
    /**
     * Estudiantes en nivel BAJO que necesitan atención prioritaria.
     */
    @GetMapping("/cuestionario/{idCuestionario}/estudiantes-necesitan-apoyo")
    public ResponseEntity<ApiResponse<List<EstudianteQueNecesitaApoyoResponse>>> getEstudiantesNecesitanApoyo(
            @PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(
            analyticsService.getEstudiantesNecesitanApoyo(idCuestionario)
        ));
    }
    
    /**
     * IDs de estudiantes con POST_TEST pero sin PRE_TEST (dato anómalo).
     */
    @GetMapping("/cuestionario/{idCuestionario}/no-pretest")
    public ResponseEntity<ApiResponse<List<Long>>> getEstudiantesSinPreTest(
            @PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(
            analyticsService.getEstudiantesSinPreTest(idCuestionario)
        ));
    }
    
    /**
     * Estadísticas de participación y completitud.
     */
    @GetMapping("/cuestionario/{idCuestionario}/estadistica-finalizacion")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEstadisticaFinalizacion(
            @PathVariable Long idCuestionario) {
        return ResponseEntity.ok(ApiResponse.ok(
            analyticsService.getEstadisticaFinalizacion(idCuestionario)
        ));
    }

}
