package com.udea.skillbridge.mapper;

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.udea.skillbridge.dto.response.PlanFortalecimientoResponse;
import com.udea.skillbridge.dto.response.PuntuacionResultadoResponse;
import com.udea.skillbridge.entity.PuntuacionMatrixEntity;
import com.udea.skillbridge.entity.PuntuacionResultadoEntity;

@Mapper(
	    componentModel = "spring",
	    uses = { IPlanFortalecimientoMapper.class }
	)
public interface IPuntuacionResultadoMapper {
	
    @Mapping(target = "descripcionNivel",           source = "puntuacionMatrizEnt.descripcion")
    @Mapping(target = "caracteristicasObservables", source = "puntuacionMatrizEnt.caracteristicasObservables")
    @Mapping(target = "planesAsignados",            expression = "java(toPlanesResponse(resultado.getPuntuacionMatrizEnt()))")
    PuntuacionResultadoResponse toResponse(PuntuacionResultadoEntity resultado);
    
    default List<PlanFortalecimientoResponse> toPlanesResponse(PuntuacionMatrixEntity matriz) {
        if (matriz == null || matriz.getPlanFortalecimientoEnt() == null) {
            return Collections.emptyList();
        }
        return matriz.getPlanFortalecimientoEnt().stream()
                .map(IPlanFortalecimientoMapper.INSTANCE::toResponse)
                .toList();
    }   

}
