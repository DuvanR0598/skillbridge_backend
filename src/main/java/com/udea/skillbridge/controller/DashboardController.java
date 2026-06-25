package com.udea.skillbridge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.dto.response.ActividadRecienteResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.service.IDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    /**
     * Actividad reciente del usuario autenticado (derivada según su rol).
     */
    @GetMapping("/actividad-reciente")
    public ResponseEntity<ApiResponse<List<ActividadRecienteResponse>>> getActividadReciente(
            @AuthenticationPrincipal UsuarioEntity usuarioActual) {
        return ResponseEntity.ok(ApiResponse.ok(
            dashboardService.getActividadReciente(usuarioActual)
        ));
    }
}
