package com.udea.skillbridge.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.udea.skillbridge.entity.CuestionarioEntity;
import com.udea.skillbridge.entity.DetalleRespuestaEntity;
import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;
import com.udea.skillbridge.entity.OpcionPreguntaEntity;
import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.entity.PuntuacionMatrixEntity;
import com.udea.skillbridge.entity.PuntuacionResultadoEntity;
import com.udea.skillbridge.enums.SkillNivel;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.repository.IPuntuacionMatrixRepository;

/**
 * Pruebas unitarias de {@link MotorDePuntuacion#calcular}.
 *
 * Los métodos de cálculo son privados, así que se prueban a través del método
 * público calcular(...). Se mockea el repositorio de la matriz y el grafo de
 * entidades de entrada; se afirma sobre el {@link PuntuacionResultadoEntity}
 * que el motor construye.
 *
 * Estrictez LENIENT: los helpers stubbean getters que no siempre se usan
 * (p. ej. getMaxOpciones / getDimension en evaluaciones globales).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MotorDePuntuacionTest {

    private static final Long ID_CUEST = 1L;

    @Mock private IPuntuacionMatrixRepository puntuacionMatrixrepository;

    @InjectMocks private MotorDePuntuacion motor;

    // ── Auxiliares ─────────────────────────────────────────────
    private OpcionPreguntaEntity opcion(long id, int peso) {
        OpcionPreguntaEntity o = mock(OpcionPreguntaEntity.class);
        when(o.getId()).thenReturn(id);
        when(o.getPeso()).thenReturn(peso);
        return o;
    }

    private PreguntaEntity pregunta(long id, TipoPregunta tipo, Integer maxOpciones,
            List<OpcionPreguntaEntity> opciones) {
        PreguntaEntity p = mock(PreguntaEntity.class);
        when(p.getIdPregunta()).thenReturn(id);
        when(p.getTipoPregunta()).thenReturn(tipo);
        when(p.getOpcionPregunta()).thenReturn(opciones);
        when(p.getMaxOpciones()).thenReturn(maxOpciones);
        when(p.getDimension()).thenReturn(null); // pruebas globales (sin dimensión)
        return p;
    }

    private DetalleRespuestaEntity respuesta(PreguntaEntity preg, List<Long> idsSeleccionadas) {
        DetalleRespuestaEntity r = mock(DetalleRespuestaEntity.class);
        when(r.getPreguntaEnt()).thenReturn(preg);
        when(r.getIdsOpcionesSeleccionadas()).thenReturn(idsSeleccionadas);
        return r;
    }

    /** Entrada de matriz GLOBAL (sin pregunta ni dimensión). */
    private PuntuacionMatrixEntity matrizGlobal(SkillTipo skill, int min, int max, SkillNivel nivel) {
        PuntuacionMatrixEntity m = mock(PuntuacionMatrixEntity.class);
        when(m.getSkill()).thenReturn(skill);
        when(m.getDimensionEnt()).thenReturn(null);
        when(m.getPreguntaEnt()).thenReturn(null);
        when(m.getMinPuntaje()).thenReturn(min);
        when(m.getMaxPuntaje()).thenReturn(max);
        when(m.getNivel()).thenReturn(nivel);
        return m;
    }

    private EvaluacionEstudianteEntity evaluacion() {
        CuestionarioEntity c = mock(CuestionarioEntity.class);
        when(c.getIdCuestionario()).thenReturn(ID_CUEST);
        EvaluacionEstudianteEntity e = mock(EvaluacionEstudianteEntity.class);
        when(e.getCuestionarioEnt()).thenReturn(c);
        return e;
    }

    // ── Caso borde: sin matriz ─────────────────────────────────
    @Test
    @DisplayName("devuelve lista vacía cuando el cuestionario no tiene matriz configurada")
    void devuelveListaVaciaSinMatriz() {
        when(puntuacionMatrixrepository.findByCuestionarioEntIdCuestionario(anyLong()))
                .thenReturn(List.of());

        List<PuntuacionResultadoEntity> resultado = motor.calcular(evaluacion(), List.of());

        assertThat(resultado).isEmpty();
    }

    // ── Caso feliz: OPCION_UNICA ───────────────────────────────
    @Test
    @DisplayName("OPCION_UNICA: el máximo es el mayor peso y el nivel sale de la matriz")
    void calculaOpcionUnicaPuntajeMaximoYNivel() {
        // Opciones con pesos 1, 2, 3; el estudiante elige la de peso 3
        OpcionPreguntaEntity o1 = opcion(1L, 1);
        OpcionPreguntaEntity o2 = opcion(2L, 2);
        OpcionPreguntaEntity o3 = opcion(3L, 3);
        PreguntaEntity p = pregunta(100L, TipoPregunta.OPCION_UNICA, null, List.of(o1, o2, o3));
        DetalleRespuestaEntity r = respuesta(p, List.of(3L));

        // Importante: crear el mock de la matriz ANTES del when(repo...) para no
        // anidar stubbing dentro de .thenReturn(...) (UnfinishedStubbingException).
        PuntuacionMatrixEntity matriz = matrizGlobal(SkillTipo.PENSAMIENTO_CRITICO, 2, 3, SkillNivel.AVANZADO);
        when(puntuacionMatrixrepository.findByCuestionarioEntIdCuestionario(anyLong()))
                .thenReturn(List.of(matriz));

        List<PuntuacionResultadoEntity> resultado = motor.calcular(evaluacion(), List.of(r));

        assertThat(resultado).hasSize(1);
        PuntuacionResultadoEntity res = resultado.get(0);
        assertThat(res.getSkill()).isEqualTo(SkillTipo.PENSAMIENTO_CRITICO);
        assertThat(res.getTotalPuntaje()).isEqualTo(3);
        assertThat(res.getMaxPuntuacionPosible()).isEqualTo(3); // mayor peso
        assertThat(res.getPorcentajePuntuacion()).isEqualByComparingTo("100.00");
        assertThat(res.getNivel()).isEqualTo(SkillNivel.AVANZADO);
    }

    // ── Caso clave: OPCION_MULTIPLE con límite ─────────────────
    @Test
    @DisplayName("OPCION_MULTIPLE: el máximo es la suma de los N pesos más altos (N = maxOpciones)")
    void calculaOpcionMultipleMaximoComoSumaDeTopN() {
        // Pesos 5, 3, 2, 1 con maxOpciones = 2  → máximo = 5 + 3 = 8
        OpcionPreguntaEntity o1 = opcion(1L, 5);
        OpcionPreguntaEntity o2 = opcion(2L, 3);
        OpcionPreguntaEntity o3 = opcion(3L, 2);
        OpcionPreguntaEntity o4 = opcion(4L, 1);
        PreguntaEntity p = pregunta(200L, TipoPregunta.OPCION_MULTIPLE, 2, List.of(o1, o2, o3, o4));
        // El estudiante elige las opciones de peso 5 y 2 → total = 7
        DetalleRespuestaEntity r = respuesta(p, List.of(1L, 3L));

        PuntuacionMatrixEntity matriz = matrizGlobal(SkillTipo.ADAPTABILIDAD, 5, 8, SkillNivel.INTERMEDIO);
        when(puntuacionMatrixrepository.findByCuestionarioEntIdCuestionario(anyLong()))
                .thenReturn(List.of(matriz));

        List<PuntuacionResultadoEntity> resultado = motor.calcular(evaluacion(), List.of(r));

        assertThat(resultado).hasSize(1);
        PuntuacionResultadoEntity res = resultado.get(0);
        assertThat(res.getSkill()).isEqualTo(SkillTipo.ADAPTABILIDAD);
        assertThat(res.getTotalPuntaje()).isEqualTo(7);
        assertThat(res.getMaxPuntuacionPosible()).isEqualTo(8); // suma top 2
        assertThat(res.getPorcentajePuntuacion()).isEqualByComparingTo("87.50");
        assertThat(res.getNivel()).isEqualTo(SkillNivel.INTERMEDIO);
    }

    // ── Caso borde: sin selección ──────────────────────────────
    @Test
    @DisplayName("sin opciones seleccionadas, el puntaje es 0 y el porcentaje 0")
    void asignaPuntajeCeroSinSeleccion() {
        OpcionPreguntaEntity o1 = opcion(1L, 1);
        OpcionPreguntaEntity o2 = opcion(2L, 2);
        PreguntaEntity p = pregunta(300L, TipoPregunta.OPCION_UNICA, null, List.of(o1, o2));
        DetalleRespuestaEntity r = respuesta(p, List.of()); // nada seleccionado

        PuntuacionMatrixEntity matriz = matrizGlobal(SkillTipo.PENSAMIENTO_CRITICO, 0, 1, SkillNivel.BAJO);
        when(puntuacionMatrixrepository.findByCuestionarioEntIdCuestionario(anyLong()))
                .thenReturn(List.of(matriz));

        List<PuntuacionResultadoEntity> resultado = motor.calcular(evaluacion(), List.of(r));

        assertThat(resultado).hasSize(1);
        PuntuacionResultadoEntity res = resultado.get(0);
        assertThat(res.getTotalPuntaje()).isEqualTo(0);
        assertThat(res.getMaxPuntuacionPosible()).isEqualTo(2); // mayor peso disponible
        assertThat(res.getPorcentajePuntuacion()).isEqualByComparingTo("0");
        assertThat(res.getNivel()).isEqualTo(SkillNivel.BAJO);
    }
}
