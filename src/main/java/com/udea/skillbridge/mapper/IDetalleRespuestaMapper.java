package com.udea.skillbridge.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.udea.skillbridge.dto.response.DetalleRespuestaResponse;
import com.udea.skillbridge.entity.DetalleRespuestaEntity;

@Mapper(componentModel = "spring")
public interface IDetalleRespuestaMapper {
	
    @Mapping(source = "preguntaEnt.idPregunta",   target = "idPregunta")
    @Mapping(source = "preguntaEnt.texto",        target = "textoPregunta")
    DetalleRespuestaResponse toResponse(DetalleRespuestaEntity respuesta);

}
