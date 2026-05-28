package com.udea.skillbridge.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.udea.skillbridge.dto.response.EvaluacionEstudianteResponse;
import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;

@Mapper(
	    componentModel = "spring",
	    uses = { IDetalleRespuestaMapper.class, IPuntuacionResultadoMapper.class }
	)
public interface IEvaluacionEstudianteMapper {
	
    @Mapping(source = "cuestionarioEnt.idCuestionario",   target = "idCuestionario")
    @Mapping(source = "cuestionarioEnt.nombre",           target = "nombreCuestionario")
    @Mapping(
        target = "totalRespuestas",
        expression = "java(evaluacion.getRespuestas().size())"
    )
    EvaluacionEstudianteResponse toResponse(EvaluacionEstudianteEntity evaluacion);

}
