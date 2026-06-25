package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;

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
public class CondicionPreguntaResponse {
	
	private Long id;
    private Long idCuestionario;
    private Long triggerIdPregunta;
    private String triggerTextoPregunta;
    private Long triggerIdOpcion;
    private String triggerTextoOpcion;
    private Long targetIdPregunta;
    private String targetTextopregunta;
    private LocalDateTime createdAt;
}
