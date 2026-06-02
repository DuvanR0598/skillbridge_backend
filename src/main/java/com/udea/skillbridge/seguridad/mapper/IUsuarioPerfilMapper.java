package com.udea.skillbridge.seguridad.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.udea.skillbridge.seguridad.dto.request.CompletarPerfilRequest;
import com.udea.skillbridge.seguridad.dto.response.UsuarioPerfilResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioPerfilEntity;

@Mapper(componentModel = "spring")
public interface IUsuarioPerfilMapper {
	
    @Mapping(source = "usuarioEnt.id", target = "idUsuario")
    @Mapping(
        target = "visualizacionGenero",
        expression = "java(perfil.getGenero() != null ? perfil.getGenero().getDisplayName() : null)"
    )
    @Mapping(
        target = "visualizacionProgramaIngenieria",
        expression = "java(perfil.getProgramaIngenieria() != null ? perfil.getProgramaIngenieria().getDisplayName() : null)"
    )
    @Mapping(
        target = "perfilCompleto",
        expression = "java(perfil.isCompleto())"
    )
    @Mapping(
        target = "porcentajeCompleto",
        expression = "java(perfil.porcentajeCompleto())"
    )
    UsuarioPerfilResponse toResponse(UsuarioPerfilEntity perfil);
    
    /**
     * Actualización parcial: solo pisa los campos que llegan con valor.
     * avatarUrl se maneja aparte (endpoint dedicado de subida de foto).
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "usuarioEnt",  ignore = true)
    @Mapping(target = "avatarUrl",   ignore = true)  // se maneja en el endpoint de avatar
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    void updateFromRequest(@MappingTarget UsuarioPerfilEntity perfil, CompletarPerfilRequest request);

}
