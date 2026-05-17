package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.udea.skillbridge.enums.TipoPregunta;

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
public class PreguntaResponse {

	private Long idPregunta;
	private TipoPregunta tipoPregunta;
	private String texto;
	private String imagenUrl;
	private String ayuda;
	private Integer maxOpciones;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<OpcionPreguntaResponse> opcionPregunta;
}
