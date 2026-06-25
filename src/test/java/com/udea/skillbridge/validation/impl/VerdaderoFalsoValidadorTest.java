package com.udea.skillbridge.validation.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.udea.skillbridge.dto.request.OpcionPreguntaRequest;
import com.udea.skillbridge.dto.request.PreguntaRequest;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.exception.CuestionarioException;

/**
 * Pruebas unitarias de {@link VerdaderoFalsoValidador}.
 *
 * Prueba de lógica pura: el validador no tiene colaboradores, así que no
 * hace falta Mockito.
 * Regla bajo prueba: una pregunta VERDADERO/FALSO debe tener EXACTAMENTE 2 opciones.
 */
class VerdaderoFalsoValidadorTest {

    private final VerdaderoFalsoValidador validador = new VerdaderoFalsoValidador();

    // ── Auxiliares ─────────────────────────────────────────────
    private OpcionPreguntaRequest opcion(String texto, int orden) {
        return OpcionPreguntaRequest.builder()
                .texto(texto)
                .peso(1)
                .ordenVisualizacion(orden)
                .build();
    }

    private PreguntaRequest preguntaConOpciones(List<OpcionPreguntaRequest> opciones) {
        return PreguntaRequest.builder()
                .tipoPregunta(TipoPregunta.VERDADERO_FALSO)
                .texto("¿El cielo es azul?")
                .opcionPreguntaRequest(opciones)
                .build();
    }

    // ── Caso feliz ─────────────────────────────────────────────
    @Test
    @DisplayName("pasa cuando la pregunta tiene exactamente dos opciones")
    void pasaConExactamenteDosOpciones() {
        PreguntaRequest request = preguntaConOpciones(
                List.of(opcion("Verdadero", 1), opcion("Falso", 2)));

        assertThatCode(() -> validador.validador(request)).doesNotThrowAnyException();
    }

    // ── Casos de error / borde ─────────────────────────────────
    @Test
    @DisplayName("lanza excepción cuando la lista de opciones es null")
    void lanzaCuandoLasOpcionesSonNull() {
        PreguntaRequest request = preguntaConOpciones(null);

        assertThatThrownBy(() -> validador.validador(request))
                .isInstanceOf(CuestionarioException.class)
                .hasMessageContaining("exactamente 2 opciones");
    }

    @Test
    @DisplayName("lanza excepción cuando solo hay una opción")
    void lanzaConUnaOpcion() {
        PreguntaRequest request = preguntaConOpciones(List.of(opcion("Verdadero", 1)));

        assertThatThrownBy(() -> validador.validador(request))
                .isInstanceOf(CuestionarioException.class);
    }

    @Test
    @DisplayName("lanza excepción cuando hay tres opciones")
    void lanzaConTresOpciones() {
        PreguntaRequest request = preguntaConOpciones(
                List.of(opcion("Verdadero", 1), opcion("Falso", 2), opcion("Quizás", 3)));

        assertThatThrownBy(() -> validador.validador(request))
                .isInstanceOf(CuestionarioException.class);
    }

    @Test
    @DisplayName("lanza excepción cuando la lista de opciones está vacía")
    void lanzaConOpcionesVacias() {
        PreguntaRequest request = preguntaConOpciones(List.of());

        assertThatThrownBy(() -> validador.validador(request))
                .isInstanceOf(CuestionarioException.class);
    }
}
