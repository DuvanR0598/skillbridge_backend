package com.udea.skillbridge.service;

import java.util.List;

import com.udea.skillbridge.dto.response.ActividadRecienteResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;

public interface IDashboardService {

    /**
     * Actividad reciente del dashboard, derivada según el rol del usuario:
     * estudiante → sus evaluaciones; coordinador/admin → cuestionarios.
     */
    List<ActividadRecienteResponse> getActividadReciente(UsuarioEntity usuario);
}
