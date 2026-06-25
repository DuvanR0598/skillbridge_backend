package com.udea.skillbridge.dto.response;

import java.time.LocalDateTime;

import com.udea.skillbridge.enums.SkillTipo;

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
public class DimensionResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private SkillTipo skill;
    private LocalDateTime createdAt;
}
