package com.udea.skillbridge.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.udea.skillbridge.dto.request.ActualizarPlanFortalecimientoRequest;
import com.udea.skillbridge.dto.request.ActualizarPuntuacionMatrixRequest;
import com.udea.skillbridge.dto.request.PlanFortalecimientoRequest;
import com.udea.skillbridge.dto.response.PlanFortalecimientoResponse;
import com.udea.skillbridge.entity.PlanFortalecimientoEntity;

@Mapper(componentModel = "spring")
public interface IPlanFortalecimientoMapper {
	
    @Mapping(target = "id",                  ignore = true)
    @Mapping(target = "puntuacionMatrixEnt", ignore = true)
    @Mapping(target = "createdAt",           ignore = true)
    @Mapping(target = "updatedAt",           ignore = true)
    PlanFortalecimientoEntity toEntity(PlanFortalecimientoRequest request);
    
    @Mapping(source = "puntuacionMatrixEnt.id", target = "id")
    PlanFortalecimientoResponse toResponse(PlanFortalecimientoEntity plan);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",                  ignore = true)
    @Mapping(target = "puntuacionMatrixEnt", ignore = true)
    @Mapping(target = "planAxis",            ignore = true)
    @Mapping(target = "createdAt",           ignore = true)
    @Mapping(target = "updatedAt",           ignore = true)
    void updateFromRequest(
        @MappingTarget PlanFortalecimientoEntity plan,
        ActualizarPlanFortalecimientoRequest request
    );

}
