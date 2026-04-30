//package com.udea.skillbridge.mapper;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.ReportingPolicy;
//
//import com.udea.skillbridge.dto.OpcionPregunta;
//import com.udea.skillbridge.persistence.entity.OpcionPreguntaEntity;
//
//@Mapper(
//	    componentModel = "spring",                    // Genera un bean de Spring
//	    unmappedTargetPolicy = ReportingPolicy.WARN  // Advierte si se olvida mapear algo
//)
//public interface IOpcionPreguntaMapper {
//	
//	@Mapping(target = "idOpcPregunta", ignore = true)  // El ID se genera automáticamente
//	OpcionPreguntaEntity toEntity (OpcionPregunta opcionPregunta);
//	
//	OpcionPregunta toDto (OpcionPreguntaEntity opcionPreguntaEnt);
//
//}
