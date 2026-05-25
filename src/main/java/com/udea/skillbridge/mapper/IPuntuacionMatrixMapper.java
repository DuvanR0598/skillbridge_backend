package com.udea.skillbridge.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.udea.skillbridge.dto.request.ActualizarPuntuacionMatrixRequest;
import com.udea.skillbridge.dto.request.PuntuacionMatrixRequest;
import com.udea.skillbridge.dto.response.PuntuacionMatrixResponse;
import com.udea.skillbridge.entity.PuntuacionMatrixEntity;

@Mapper(
	    componentModel = "spring",
	    uses = { IPlanFortalecimientoMapper.class }
	)
public interface IPuntuacionMatrixMapper {
	
    @Mapping(target = "id",                      ignore = true)
    @Mapping(target = "cuestionarioEnt",         ignore = true)
    @Mapping(target = "preguntaEnt",             ignore = true)
    @Mapping(target = "planFortalecimientoEnt",  ignore = true)
    @Mapping(target = "createdAt",               ignore = true)
    @Mapping(target = "updatedAt",               ignore = true)
    PuntuacionMatrixEntity toEntity(PuntuacionMatrixRequest request);
    
    @Mapping(source = "cuestionarioEnt.idCuestionario", target = "idCuestionario")
    @Mapping(source = "preguntaEnt.idPregunta",         target = "idPregunta")
    @Mapping(source = "preguntaEnt.texto",              target = "textoPregunta")
    @Mapping(
        target = "fullConfigurado",
        expression = "java(scoreMatrix.getPlanFortalecimientoEnt().size() == 3)"
    )
    // strengtheningPlans se mapea automáticamente via StrengtheningPlanMapper
    PuntuacionMatrixResponse toResponse(PuntuacionMatrixEntity scoreMatrix);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",                      ignore = true)
    @Mapping(target = "cuestionarioEnt",           ignore = true)
    @Mapping(target = "preguntaEnt",                ignore = true)
    @Mapping(target = "skill",                   ignore = true)
    @Mapping(target = "dimension",               ignore = true)
    @Mapping(target = "nivel",                   ignore = true)
    @Mapping(target = "planFortalecimientoEnt",      ignore = true)
    @Mapping(target = "createdAt",               ignore = true)
    @Mapping(target = "updatedAt",               ignore = true)
    void updateFromRequest(
        @MappingTarget PuntuacionMatrixEntity scoreMatrix,
        ActualizarPuntuacionMatrixRequest request
    );

}
