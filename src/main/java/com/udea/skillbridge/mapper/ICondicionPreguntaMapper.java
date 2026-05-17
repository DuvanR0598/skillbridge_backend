package com.udea.skillbridge.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.udea.skillbridge.dto.response.CondicionPreguntaResponse;
import com.udea.skillbridge.entity.CondicionPreguntaEntity;

@Mapper(
	    componentModel = "spring",                    // Genera un bean de Spring
	    unmappedTargetPolicy = ReportingPolicy.WARN  // Advierte si se olvida mapear algo
)
public interface ICondicionPreguntaMapper {
	
	@Mapping(target = "idCuestionario",       source = "cuestionarioEnt.idCuestionario")
    @Mapping(target = "triggerIdPregunta",    source = "triggerPregunta.idPregunta")
    @Mapping(target = "triggerTextoPregunta", source = "triggerPregunta.texto")
    @Mapping(target = "triggerIdOpcion",      source = "triggerOpcion.id")
    @Mapping(target = "triggerTextoOpcion",   source = "triggerOpcion.texto")
    @Mapping(target = "targetIdPregunta",     source = "targetPregunta.idPregunta")
    @Mapping(target = "targetTextopregunta",  source = "targetPregunta.texto")
    @Mapping(target = "createdAt",            source = "createdAt")
    CondicionPreguntaResponse toResponse(CondicionPreguntaEntity entity);

}
