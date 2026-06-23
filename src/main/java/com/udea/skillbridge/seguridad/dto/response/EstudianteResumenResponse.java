package com.udea.skillbridge.seguridad.dto.response;

import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;
import com.udea.skillbridge.seguridad.enums.TipoIdentificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resumen de un estudiante para la vista de solo lectura del coordinador.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteResumenResponse {

    private Long idUsuario;

    private TipoIdentificacion tipoIdentificacion;
    private String numeroIdentificacion;

    private String nombre;
    private String apellido;
    private String email;

    private ProgramaIngenieria programaIngenieria;
    private String programaNombre;   // displayName, ej. "Ingeniería de Sistemas"
    private String codigoPrograma;   // ej. "504"

    private Integer semestreAcademico;
    private Boolean activado;
}
