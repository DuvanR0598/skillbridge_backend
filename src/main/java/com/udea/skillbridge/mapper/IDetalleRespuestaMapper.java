package com.udea.skillbridge.mapper;

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.udea.skillbridge.dto.response.DetalleRespuestaResponse;
import com.udea.skillbridge.entity.DetalleRespuestaEntity;
import com.udea.skillbridge.entity.OpcionPreguntaEntity;

@Mapper(componentModel = "spring")
public interface IDetalleRespuestaMapper {

    @Mapping(source = "preguntaEnt.idPregunta",   target = "idPregunta")
    @Mapping(source = "preguntaEnt.texto",        target = "textoPregunta")
    @Mapping(target = "opcionesSeleccionadas",
        expression = "java(textosOpcionesSeleccionadas(respuesta))")
    DetalleRespuestaResponse toResponse(DetalleRespuestaEntity respuesta);

    /** Devuelve los textos de las opciones que el estudiante seleccionó. */
    default List<String> textosOpcionesSeleccionadas(DetalleRespuestaEntity respuesta) {
        if (respuesta.getIdsOpcionesSeleccionadas() == null
                || respuesta.getIdsOpcionesSeleccionadas().isEmpty()
                || respuesta.getPreguntaEnt() == null) {
            return Collections.emptyList();
        }
        List<Long> seleccionadas = respuesta.getIdsOpcionesSeleccionadas();
        return respuesta.getPreguntaEnt().getOpcionPregunta().stream()
                .filter(o -> seleccionadas.contains(o.getId()))
                .map(OpcionPreguntaEntity::getTexto)
                .toList();
    }

}
