package com.udea.skillbridge.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.IniciarEvaluacionRequest;
import com.udea.skillbridge.dto.response.EvaluacionEstudianteResponse;
import com.udea.skillbridge.entity.CuestionarioEntity;
import com.udea.skillbridge.entity.EvaluacionEstudianteEntity;
import com.udea.skillbridge.enums.EstadoCuestionario;
import com.udea.skillbridge.enums.EvaluacionEstado;
import com.udea.skillbridge.enums.EvaluacionFase;
import com.udea.skillbridge.mapper.IDetalleRespuestaMapper;
import com.udea.skillbridge.mapper.IEvaluacionEstudianteMapper;
import com.udea.skillbridge.mapper.IPlanFortalecimientoMapper;
import com.udea.skillbridge.repository.ICuestionarioRepository;
import com.udea.skillbridge.repository.IDetalleRespuestaRepository;
import com.udea.skillbridge.repository.IEvaluacionEstudianteRepository;
import com.udea.skillbridge.repository.IPreguntaRepository;
import com.udea.skillbridge.repository.IPuntuacionResultadoRepository;
import com.udea.skillbridge.seguridad.entity.UsuarioPerfilEntity;
import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;
import com.udea.skillbridge.seguridad.repository.IUsuarioPerfilRepository;

/**
 * Pruebas unitarias de {@link EvaluacionEstudianteServiceImpl#iniciar}.
 *
 * Todos los colaboradores se mockean con Mockito. Usamos estrictez LENIENT
 * porque cada rama corta en un punto distinto, así que algunos stubs
 * compartidos quedan sin usar en los tests de salida temprana; lenient evita
 * el ruido de UnnecessaryStubbingException.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvaluacionEstudianteServiceImplTest {

    private static final Long ID_CUEST = 1L;
    private static final Long ID_EST = 10L;

    // ── Mocks (todas las dependencias del constructor) ─────────
    @Mock private ICuestionarioRepository cuestionarioRepository;
    @Mock private IEvaluacionEstudianteRepository evaluacionRepository;
    @Mock private IEvaluacionEstudianteMapper evaluacionMapper;
    @Mock private IPreguntaRepository preguntaRepository;
    @Mock private IDetalleRespuestaRepository detalleRespuestaRepository;
    @Mock private IDetalleRespuestaMapper detalleRespuestaMapper;
    @Mock private MotorDePuntuacion motorPuntuacion;
    @Mock private IPuntuacionResultadoRepository puntuacionResultadoRepository;
    @Mock private IPlanFortalecimientoMapper planMapper;
    @Mock private IUsuarioPerfilRepository usuarioPerfilRepository;

    @InjectMocks private EvaluacionEstudianteServiceImpl service;

    // ── Auxiliares ─────────────────────────────────────────────
    private IniciarEvaluacionRequest request(EvaluacionFase fase) {
        return IniciarEvaluacionRequest.builder()
                .idEstudiante(ID_EST)
                .evaluacionFase(fase)
                .build();
    }

    /** Cuestionario PUBLICADO, dentro de la ventana y registrado en el repo. */
    private CuestionarioEntity publicadoEnVentana() {
        CuestionarioEntity c = mock(CuestionarioEntity.class);
        when(c.getEstadoCuestionario()).thenReturn(EstadoCuestionario.PUBLICADO);
        when(c.ventanaVigente(any())).thenReturn(true);
        when(cuestionarioRepository.findActivoById(ID_CUEST)).thenReturn(Optional.of(c));
        return c;
    }

    /** No hay sesión EN_PROGRESO ni intento previo. */
    private void stubSinConflictos() {
        when(evaluacionRepository
                .findByIdEstudianteAndCuestionarioEntIdCuestionarioAndEstado(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());
        when(evaluacionRepository
                .findTopByIdEstudianteAndCuestionarioEntIdCuestionarioAndEvaluacionFaseOrderByNumeroIntentoDesc(
                        anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());
    }

    private void assertErrorDeNegocio(ThrowingCallable accion, String codigoEsperado) {
        Throwable lanzada = catchThrowable(accion);
        assertThat(lanzada).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) lanzada).getErrorCode()).isEqualTo(codigoEsperado);
    }

    // ── Caso feliz ─────────────────────────────────────────────
    @Test
    @DisplayName("inicia una sesión PRE_TEST para un cuestionario general y devuelve la respuesta mapeada")
    void iniciaPreTestParaCuestionarioGeneral() {
        publicadoEnVentana(); // programaObjetivo = null (general) por defecto
        stubSinConflictos();

        EvaluacionEstudianteEntity guardada = mock(EvaluacionEstudianteEntity.class);
        EvaluacionEstudianteResponse esperada = mock(EvaluacionEstudianteResponse.class);
        when(evaluacionRepository.save(any())).thenReturn(guardada);
        when(evaluacionMapper.toResponse(guardada)).thenReturn(esperada);

        EvaluacionEstudianteResponse resultado = service.iniciar(ID_CUEST, request(EvaluacionFase.PRE_TEST));

        assertThat(resultado).isSameAs(esperada);
        verify(evaluacionRepository).save(any(EvaluacionEstudianteEntity.class));
    }

    @Test
    @DisplayName("permite iniciar cuando el cuestionario va dirigido al programa del estudiante")
    void permiteCuandoElProgramaCoincide() {
        CuestionarioEntity c = publicadoEnVentana();
        when(c.getProgramaObjetivo()).thenReturn(ProgramaIngenieria.INGENIERIA_DE_SISTEMAS);

        UsuarioPerfilEntity perfil = mock(UsuarioPerfilEntity.class);
        when(perfil.getProgramaIngenieria()).thenReturn(ProgramaIngenieria.INGENIERIA_DE_SISTEMAS);
        when(usuarioPerfilRepository.findByUsuarioEntId(ID_EST)).thenReturn(Optional.of(perfil));

        stubSinConflictos();
        EvaluacionEstudianteResponse esperada = mock(EvaluacionEstudianteResponse.class);
        when(evaluacionRepository.save(any())).thenReturn(mock(EvaluacionEstudianteEntity.class));
        when(evaluacionMapper.toResponse(any())).thenReturn(esperada);

        EvaluacionEstudianteResponse resultado = service.iniciar(ID_CUEST, request(EvaluacionFase.PRE_TEST));

        assertThat(resultado).isSameAs(esperada);
    }

    @Test
    @DisplayName("incrementa el número de intento a partir del intento anterior")
    void incrementaElNumeroDeIntento() {
        publicadoEnVentana();
        when(evaluacionRepository
                .findByIdEstudianteAndCuestionarioEntIdCuestionarioAndEstado(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        EvaluacionEstudianteEntity anterior = mock(EvaluacionEstudianteEntity.class);
        when(anterior.getNumeroIntento()).thenReturn(2);
        when(evaluacionRepository
                .findTopByIdEstudianteAndCuestionarioEntIdCuestionarioAndEvaluacionFaseOrderByNumeroIntentoDesc(
                        anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(anterior));

        when(evaluacionRepository.save(any())).thenReturn(mock(EvaluacionEstudianteEntity.class));
        when(evaluacionMapper.toResponse(any())).thenReturn(mock(EvaluacionEstudianteResponse.class));

        service.iniciar(ID_CUEST, request(EvaluacionFase.PRE_TEST));

        ArgumentCaptor<EvaluacionEstudianteEntity> captor =
                ArgumentCaptor.forClass(EvaluacionEstudianteEntity.class);
        verify(evaluacionRepository).save(captor.capture());
        EvaluacionEstudianteEntity construida = captor.getValue();

        assertThat(construida.getNumeroIntento()).isEqualTo(3);
        assertThat(construida.getEstado()).isEqualTo(EvaluacionEstado.EN_PROGRESO);
        assertThat(construida.getEvaluacionFase()).isEqualTo(EvaluacionFase.PRE_TEST);
    }

    // ── Casos de error / borde ─────────────────────────────────
    @Test
    @DisplayName("lanza ResourceNotFoundException cuando el cuestionario no existe")
    void lanzaCuandoElCuestionarioNoExiste() {
        when(cuestionarioRepository.findActivoById(ID_CUEST)).thenReturn(Optional.empty());

        Throwable lanzada = catchThrowable(() -> service.iniciar(ID_CUEST, request(EvaluacionFase.PRE_TEST)));

        assertThat(lanzada).isInstanceOf(ResourceNotFoundException.class);
        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza cuando el cuestionario no está PUBLICADO")
    void rechazaCuandoNoEstaPublicado() {
        CuestionarioEntity c = mock(CuestionarioEntity.class);
        when(c.getEstadoCuestionario()).thenReturn(EstadoCuestionario.BORRADOR);
        when(cuestionarioRepository.findActivoById(ID_CUEST)).thenReturn(Optional.of(c));

        assertErrorDeNegocio(
                () -> service.iniciar(ID_CUEST, request(EvaluacionFase.PRE_TEST)),
                "QUESTIONNAIRE_NOT_PUBLISHED");
    }

    @Test
    @DisplayName("rechaza cuando el cuestionario va dirigido a otro programa")
    void rechazaCuandoElProgramaNoCoincide() {
        CuestionarioEntity c = publicadoEnVentana();
        when(c.getProgramaObjetivo()).thenReturn(ProgramaIngenieria.INGENIERIA_DE_SISTEMAS);
        // El estudiante no tiene perfil/programa -> null -> no coincide
        when(usuarioPerfilRepository.findByUsuarioEntId(ID_EST)).thenReturn(Optional.empty());

        assertErrorDeNegocio(
                () -> service.iniciar(ID_CUEST, request(EvaluacionFase.PRE_TEST)),
                "PROGRAM_NOT_ALLOWED");
    }

    @Test
    @DisplayName("rechaza cuando la ventana de disponibilidad no está vigente")
    void rechazaCuandoEstaFueraDeVentana() {
        CuestionarioEntity c = mock(CuestionarioEntity.class);
        when(c.getEstadoCuestionario()).thenReturn(EstadoCuestionario.PUBLICADO);
        when(c.ventanaVigente(any())).thenReturn(false);
        when(cuestionarioRepository.findActivoById(ID_CUEST)).thenReturn(Optional.of(c));

        assertErrorDeNegocio(
                () -> service.iniciar(ID_CUEST, request(EvaluacionFase.PRE_TEST)),
                "OUT_OF_WINDOW");
    }

    @Test
    @DisplayName("rechaza cuando el estudiante ya tiene una sesión en progreso")
    void rechazaCuandoYaHaySesionEnProgreso() {
        publicadoEnVentana();
        when(evaluacionRepository
                .findByIdEstudianteAndCuestionarioEntIdCuestionarioAndEstado(anyLong(), anyLong(), any()))
                .thenReturn(Optional.of(mock(EvaluacionEstudianteEntity.class)));

        assertErrorDeNegocio(
                () -> service.iniciar(ID_CUEST, request(EvaluacionFase.PRE_TEST)),
                "SESSION_ALREADY_IN_PROGRESS");
    }

    @Test
    @DisplayName("rechaza POST_TEST cuando el PRE_TEST no se ha completado")
    void rechazaPostTestSinPreTestCompletado() {
        publicadoEnVentana();
        when(evaluacionRepository
                .findByIdEstudianteAndCuestionarioEntIdCuestionarioAndEstado(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());
        when(evaluacionRepository
                .existsByIdEstudianteAndCuestionarioEntIdCuestionarioAndEvaluacionFaseAndEstado(
                        anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        assertErrorDeNegocio(
                () -> service.iniciar(ID_CUEST, request(EvaluacionFase.POST_TEST)),
                "PRE_TEST_NOT_COMPLETED");
    }

    @Test
    @DisplayName("no persiste nada cuando falla una regla de negocio")
    void noGuardaCuandoSeViolaUnaRegla() {
        CuestionarioEntity c = mock(CuestionarioEntity.class);
        when(c.getEstadoCuestionario()).thenReturn(EstadoCuestionario.ARCHIVADO);
        when(cuestionarioRepository.findActivoById(ID_CUEST)).thenReturn(Optional.of(c));

        assertThatCode(() -> service.iniciar(ID_CUEST, request(EvaluacionFase.PRE_TEST)))
                .isInstanceOf(BusinessException.class);
        verify(evaluacionRepository, never()).save(any());
    }
}
