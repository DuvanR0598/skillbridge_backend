package com.udea.skillbridge.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.udea.skillbridge.dto.Cuestionario;
import com.udea.skillbridge.persistence.entity.CuestionarioEntity;

@Mapper(
	    componentModel = "spring",                    // Genera un bean de Spring
	    unmappedTargetPolicy = ReportingPolicy.WARN  // Advierte si se olvida mapear algo
)
public interface ICuestionarioMapper {
	
	@Mapping(target = "idCuestionario", ignore = true)  // El ID se genera automáticamente
	//@Mapping(target = "creadoPor", ignore = true) // Lo setea el service, no el mapper
	CuestionarioEntity toEntity (Cuestionario cuestionario);
	
	Cuestionario toDto (CuestionarioEntity cuestionarioEnt);
	
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "idCuestionario", ignore = true)
    void updateEntity(@MappingTarget CuestionarioEntity target, Cuestionario cuestionario);
	
	List<Cuestionario> toDtoList(List<CuestionarioEntity> entities);
	
	List<CuestionarioEntity> toEntityList(List<Cuestionario> dtos);

}
