package com.udea.skillbridge.seguridad.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.udea.skillbridge.seguridad.dto.response.UsuarioResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.enums.TipoRol;

@Mapper(componentModel = "spring")
public interface IUsuarioMapper {

	@Mapping(target = "roles",    expression = "java(mapRoles(usuarioEnt))")
	@Mapping(target = "permisos", expression = "java(mapPermisos(usuarioEnt))")
	@Mapping(
		target = "visualizacionTipoIdentificacion",
		expression = "java(usuarioEnt.getTipoIdentificacion() != null ? usuarioEnt.getTipoIdentificacion().getDisplayName() : null)"
	)
	UsuarioResponse toResponse(UsuarioEntity usuarioEnt);

	default Set<TipoRol> mapRoles(UsuarioEntity usuarioEnt) {
		return usuarioEnt.getRoles().stream()
				.map(r -> r.getNombre())
				.collect(Collectors.toSet());
	}
	
    default Set<String> mapPermisos(UsuarioEntity usuarioEnt) {
        return usuarioEnt.getRoles().stream()
                .flatMap(r -> r.getPermisos().stream())
                .map(p -> p.getNombre().name())
                .collect(Collectors.toSet());
    }

}
