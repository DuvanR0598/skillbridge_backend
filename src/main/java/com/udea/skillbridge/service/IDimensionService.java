package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.request.DimensionRequest;
import com.udea.skillbridge.dto.response.DimensionResponse;
import com.udea.skillbridge.enums.SkillTipo;

public interface IDimensionService {

    DimensionResponse crear(DimensionRequest request);

    DimensionResponse actualizar(Long id, DimensionRequest request);

    /** Lista todas las dimensiones, o solo las de un skill si se indica. */
    List<DimensionResponse> listar(SkillTipo skill);

    DimensionResponse findById(Long id);

    void eliminar(Long id);
}
