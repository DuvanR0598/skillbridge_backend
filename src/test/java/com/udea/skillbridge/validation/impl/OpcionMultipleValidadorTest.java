package com.udea.skillbridge.validation.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.udea.skillbridge.dto.request.OpcionPreguntaRequest;
import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.exception.CuestionarioException;

/**
 * Pruebas unitarias de {@link OpcionMultipleValidador}.
 *
 * Regla única: una pregunta de OPCIÓN MÚLTIPLE debe tener al menos 2 opciones.
 */
class OpcionMultipleValidadorTest {

    private final OpcionMultipleValidador validador = new OpcionMultipleValidador();

    // ── Auxiliares ─────────────────────────────────────────────
    private List<OpcionPreguntaRequest> opciones(int n) {
        List<OpcionPreguntaRequest> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            list.add(OpcionPreguntaRequest.builder()
                    .texto("Opción " + i)
                    .peso(i)
                    .ordenVisualizacion(i)
                    .build());
        }
        return list;
    }

    private PreguntaRequest preguntaCon(List<OpcionPreguntaRequest> opciones) {
        return PreguntaRequest.builder()
                .tipoPregunta(TipoPregunta.OPCION_MULTIPLE)
                .texto("Selecciona todas las que apliquen")
                .opcionPreguntaRequest(opciones)
                .build();
    }

    // ── Casos felices ──────────────────────────────────────────
    @Test
    @DisplayName("pasa con exactamente dos opciones (mínimo)")
    void pasaConDosOpciones() {
        assertThatCode(() -> validador.validador(preguntaCon(opciones(2)))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("pasa con varias opciones (más del mínimo)")
    void pasaConVariasOpciones() {
        assertThatCode(() -> validador.validador(preguntaCon(opciones(5)))).doesNotThrowAnyException();
    }

    // ── Casos de error / borde ─────────────────────────────────
    @Test
    @DisplayName("lanza excepción con una sola opción")
    void lanzaConUnaOpcion() {
        assertThatThrownBy(() -> validador.validador(preguntaCon(opciones(1))))
                .isInstanceOf(CuestionarioException.class);
    }

    @Test
    @DisplayName("lanza excepción cuando la lista de opciones es null")
    void lanzaConOpcionesNull() {
        assertThatThrownBy(() -> validador.validador(preguntaCon(null)))
                .isInstanceOf(CuestionarioException.class);
    }

    @Test
    @DisplayName("lanza excepción cuando la lista de opciones está vacía")
    void lanzaConOpcionesVacias() {
        assertThatThrownBy(() -> validador.validador(preguntaCon(List.of())))
                .isInstanceOf(CuestionarioException.class);
    }
}
