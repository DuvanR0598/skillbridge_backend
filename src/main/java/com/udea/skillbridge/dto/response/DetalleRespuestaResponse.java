package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleRespuestaResponse {
	
    private Long id;
    private Long idPregunta;
    private String textoPregunta;
    private List<Long> idsOpcionesSeleccionadas;
    private String respuestaAbierta;
    private Integer puntajeObtenido;
    private LocalDateTime answeredAt;

}
