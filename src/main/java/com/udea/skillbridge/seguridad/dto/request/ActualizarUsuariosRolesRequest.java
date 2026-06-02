package com.udea.skillbridge.seguridad.dto.request;

import java.util.Set;

import com.udea.skillbridge.seguridad.enums.TipoRol;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarUsuariosRolesRequest {
	
    @NotEmpty(message = "Debe especificar al menos un rol")
    private Set<TipoRol> roles;

}
