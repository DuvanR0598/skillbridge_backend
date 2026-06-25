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
 * Pruebas unitarias de {@link LikertValidador}.
 *
 * Regla: una pregunta LIKERT debe tener entre 2 y 5 opciones (inclusive).
 */
class LikertValidadorTest {

    private final LikertValidador validador = new LikertValidador();

    // ── Auxiliares ─────────────────────────────────────────────
    private List<OpcionPreguntaRequest> opciones(int n) {
        List<OpcionPreguntaRequest> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            list.add(OpcionPreguntaRequest.builder()
                    .texto("Nivel " + i)
                    .peso(i)
                    .ordenVisualizacion(i)
                    .build());
        }
        return list;
    }

    private PreguntaRequest preguntaCon(List<OpcionPreguntaRequest> opciones) {
        return PreguntaRequest.builder()
                .tipoPregunta(TipoPregunta.LIKERT)
                .texto("¿Qué tan de acuerdo estás?")
                .opcionPreguntaRequest(opciones)
                .build();
    }

    // ── Casos felices (límites 2 y 5) ──────────────────────────
    @Test
    @DisplayName("pasa con dos opciones (límite inferior)")
    void pasaConDosOpciones() {
        assertThatCode(() -> validador.validador(preguntaCon(opciones(2)))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("pasa con cinco opciones (límite superior)")
    void pasaConCincoOpciones() {
        assertThatCode(() -> validador.validador(preguntaCon(opciones(5)))).doesNotThrowAnyException();
    }

    // ── Casos de error / borde ─────────────────────────────────
    @Test
    @DisplayName("lanza excepción con una sola opción (por debajo del mínimo)")
    void lanzaConUnaOpcion() {
        assertThatThrownBy(() -> validador.validador(preguntaCon(opciones(1))))
                .isInstanceOf(CuestionarioException.class);
    }

    @Test
    @DisplayName("lanza excepción con seis opciones (por encima del máximo)")
    void lanzaConSeisOpciones() {
        assertThatThrownBy(() -> validador.validador(preguntaCon(opciones(6))))
                .isInstanceOf(CuestionarioException.class);
    }

    @Test
    @DisplayName("lanza excepción cuando la lista de opciones es null")
    void lanzaConOpcionesNull() {
        assertThatThrownBy(() -> validador.validador(preguntaCon(null)))
                .isInstanceOf(CuestionarioException.class);
    }
}
