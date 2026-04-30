package com.udea.skillbridge.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.udea.skillbridge.dto.Pregunta;
import com.udea.skillbridge.persistence.entity.PreguntaEntity;

@Mapper(
	    componentModel = "spring",                    // Genera un bean de Spring
	    unmappedTargetPolicy = ReportingPolicy.WARN  // Advierte si se olvida mapear algo
)
public interface IPreguntaMapper {
	
	@Mapping(target = "idPregunta", ignore = true)  // El ID se genera automáticamente
	@Mapping(target = "opcionPregunta", ignore = true)
	PreguntaEntity toEntity (Pregunta pregunta);
	
	Pregunta toDto (PreguntaEntity preguntaEnt);

}
