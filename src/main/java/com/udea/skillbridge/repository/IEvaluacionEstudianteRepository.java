package com.udea.skillbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;
import com.udea.skillbridge.enums.EvaluacionEstado;
import com.udea.skillbridge.enums.EvaluacionFase;

@Repository
public interface IEvaluacionEstudianteRepository extends JpaRepository<EvaluacionEstudianteEntity, Long> {
	
    // Buscar sesión activa (IN_PROGRESS) de un estudiante
    Optional<EvaluacionEstudianteEntity> findByIdEstudianteAndCuestionarioEntIdCuestionarioAndEstado(
        Long idEstudiante, Long idCuestionario, EvaluacionEstado estado);
    
    // Verificar si ya existe un PRE_TEST o POST_TEST completado
    boolean existsByIdEstudianteAndCuestionarioEntIdCuestionarioAndEvaluacionFaseAndEstado(
        Long idEstudiante,
        Long idCuestionario,
        EvaluacionFase fase,
        EvaluacionEstado estado
    );
    
    // Último intento de un estudiante para una fase
    Optional<EvaluacionEstudianteEntity> findTopByIdEstudianteAndCuestionarioEntIdCuestionarioAndEvaluacionFaseOrderByNumeroIntentoDesc(
        Long studentId, Long questionnaireId, EvaluacionFase fase
    );
    
    // Todas las sesiones de un estudiante para un cuestionario
    List<EvaluacionEstudianteEntity> findByIdEstudianteAndCuestionarioEntIdCuestionario(Long idEstuadiante, Long idCuestionario);

    // Actividad reciente: últimas sesiones del estudiante (más recientes primero)
    List<EvaluacionEstudianteEntity> findTop10ByIdEstudianteOrderByStartedAtDesc(Long idEstudiante);

}
