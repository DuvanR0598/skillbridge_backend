package com.udea.skillbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.PlanFortalecimientoEntity;
import com.udea.skillbridge.enums.PlanAxis;

@Repository
public interface IPlanFortalecimientoRepository extends JpaRepository<PlanFortalecimientoEntity, Long>{
	
	boolean existsByPuntuacionMatrixEntIdAndPlanAxis(Long idPuntuacionMatrix, PlanAxis planAxis);
	
    // Cuántos planes tiene configurados una entrada de la matriz (max 3)
    int countByPuntuacionMatrixEntId(Long idPuntuacionMatrix);
    
    List<PlanFortalecimientoEntity> findBypuntuacionMatrixEntId(Long scoreMatrixId);
    
    Optional<PlanFortalecimientoEntity> findBypuntuacionMatrixEntIdAndPlanAxis(
            Long scoreMatrixId, PlanAxis planAxis);

}
