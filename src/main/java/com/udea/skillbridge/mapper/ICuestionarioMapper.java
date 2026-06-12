package com.udea.skillbridge.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.udea.skillbridge.dto.request.CuestionarioRequest;
import com.udea.skillbridge.dto.response.CuestionarioResponse;
import com.udea.skillbridge.entity.CuestionarioEntity;

@Mapper(
	    componentModel = "spring",                    // Genera un bean de Spring
	    unmappedTargetPolicy = ReportingPolicy.WARN  // Advierte si se olvida mapear algo
)
public interface ICuestionarioMapper {
	
	/**
     * Request → Entity.
     * Los campos que no existen en el request (id, status, createdAt, etc.)
     * se ignoran — Hibernate los gestiona.
     */
	@Mapping(target = "idCuestionario",        ignore = true)
	@Mapping(target = "estadoCuestionario",    ignore = true)
	@Mapping(target = "createdAt",             ignore = true)
	@Mapping(target = "updatedAt",             ignore = true)
	@Mapping(target = "creadoPor",             ignore = true)
	@Mapping(target = "preguntasCuestionario", ignore = true)
	CuestionarioEntity toEntity (CuestionarioRequest cuestionarioRequest);
	
	/**
     * Entity → Response.
     * totalPreguntas y editable son campos calculados que no existen
     * en la entidad, los seteamos con una expresión inline.
     */
    @Mapping(
            target = "totalPreguntas",
            expression = "java(cuestionarioEnt.getPreguntasCuestionario().size())"
    )
    @Mapping(
            target = "editable",
            expression = "java(cuestionarioEnt.isEditable())"
    )
    @Mapping(
            target = "disponible",
            expression = "java(cuestionarioEnt.disponibleParaResponder())"
    )
	CuestionarioResponse toResponse(CuestionarioEntity cuestionarioEnt);
    
    /**
     * Actualizar una entidad existente desde un request de actualización.
     * @BeanMapping + NullValuePropertyMappingStrategy.IGNORE
     * solo actualiza los campos que vienen con valor (patch parcial).
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "idCuestionario",        ignore = true)
    @Mapping(target = "estadoCuestionario",    ignore = true)
    @Mapping(target = "createdAt",             ignore = true)
    @Mapping(target = "updatedAt",             ignore = true)
    @Mapping(target = "creadoPor",             ignore = true)
    @Mapping(target = "preguntasCuestionario", ignore = true)
    void actualizarCuestionarioRequest(
        @MappingTarget CuestionarioEntity cuestionarioEnt,
        com.udea.skillbridge.dto.request.ActualizarCuestionarioRequest request
    );
}
