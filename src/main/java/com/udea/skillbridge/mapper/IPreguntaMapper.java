package com.udea.skillbridge.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.udea.skillbridge.dto.request.OpcionPreguntaRequest;
import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.dto.response.DimensionResponse;
import com.udea.skillbridge.dto.response.OpcionPreguntaAdminResponse;
import com.udea.skillbridge.dto.response.PreguntaResponse;
import com.udea.skillbridge.entity.DimensionEntity;
import com.udea.skillbridge.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.entity.PreguntaEntity;

@Mapper(
	    componentModel = "spring",                    // Genera un bean de Spring
	    unmappedTargetPolicy = ReportingPolicy.WARN  // Advierte si se olvida mapear algo
)
public interface IPreguntaMapper {
	
	@Mapping(target = "idPregunta",              ignore = true)
	@Mapping(target = "opcionPregunta",          ignore = true)
	@Mapping(target = "createdAt",               ignore = true)
	@Mapping(target = "updatedAt",               ignore = true)
	@Mapping(target = "dimension",               ignore = true)
	@Mapping(target = "preguntaCuestionarioEnt", ignore = true)
	@Mapping(target = "puntuacionMatrices",      ignore = true)
	PreguntaEntity toEntity (PreguntaRequest preguntaRequest);
	
	@Mapping(target = "id",          ignore = true)
    @Mapping(target = "preguntaEnt", ignore = true)
    OpcionPreguntaEntity toOpcionPreguntaEntity(OpcionPreguntaRequest request);
	
	PreguntaResponse toResponse(PreguntaEntity response);

	// Opción para la vista de admin (incluye isCorrecta y peso).
	@Mapping(target = "idOpcion", source = "id")
	OpcionPreguntaAdminResponse toOpcionAdmin(OpcionPreguntaEntity entity);

	// Mapea la dimensión asociada (usado al construir PreguntaResponse.dimension).
	DimensionResponse toDimensionResponse(DimensionEntity entity);

}
