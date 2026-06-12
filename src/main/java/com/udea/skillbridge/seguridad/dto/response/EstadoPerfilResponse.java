package com.udea.skillbridge.seguridad.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Respuesta del endpoint /usuarios/me/perfil/estado.
 * El frontend usa esto para saber exactamente qué le falta al usuario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoPerfilResponse {
	
    private boolean perfilCompleto;
    private int porcentajeCompleto;

    // Campos mínimos obligatorios que aún faltan
    private List<String> camposObligatoriosFaltantes;

    // Campos opcionales que aún no se han llenado
    private List<String> camposOpcionalesFaltantes;

    // Mensaje sugerido para mostrar al usuario
    private String mensaje;

}
