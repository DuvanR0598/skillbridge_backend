package com.udea.skillbridge.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.ActualizarCuestionarioRequest;
import com.udea.skillbridge.dto.response.CuestionarioResponse;
import com.udea.skillbridge.entity.CuestionarioEntity;
import com.udea.skillbridge.mapper.ICuestionarioMapper;
import com.udea.skillbridge.repository.ICondicionPreguntaRepository;
import com.udea.skillbridge.repository.ICuestionarioRepository;
import com.udea.skillbridge.repository.IPreguntaCuestionarioRepository;
import com.udea.skillbridge.repository.IPreguntaRepository;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioPerfilEntity;
import com.udea.skillbridge.seguridad.enums.ProgramaIngenieria;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.seguridad.repository.IUsuarioPerfilRepository;

/**
 * Pruebas unitarias de {@link CuestionarioServiceImpl}.
 *
 * Se cubren dos comportamientos clave:
 *  - listarCuestionariosActivos: filtrado por programa según el rol.
 *  - actualizarCuestionario: regla de "solo editable en BORRADOR".
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CuestionarioServiceImplTest {

    private static final Long ID_EST = 10L;

    @Mock private ICuestionarioRepository cuestionarioRepository;
    @Mock private ICuestionarioMapper cuestionarioMapper;
    @Mock private IPreguntaRepository preguntaRepository;
    @Mock private IPreguntaCuestionarioRepository pqRepository;
    @Mock private ICondicionPreguntaRepository condicionPreguntaRepository;
    @Mock private IUsuarioPerfilRepository usuarioPerfilRepository;

    @InjectMocks private CuestionarioServiceImpl service;

    // ── Auxiliares ─────────────────────────────────────────────
    private CuestionarioEntity cuestionarioConPrograma(ProgramaIngenieria programa) {
        CuestionarioEntity c = mock(CuestionarioEntity.class);
        when(c.getProgramaObjetivo()).thenReturn(programa);
        return c;
    }

    private UsuarioEntity estudiante() {
        UsuarioEntity u = mock(UsuarioEntity.class);
        when(u.hasRole(any())).thenReturn(false); // ni ADMIN ni COORDINADOR
        when(u.getId()).thenReturn(ID_EST);
        return u;
    }

    private void assertErrorDeNegocio(ThrowingCallable accion, String codigoEsperado) {
        Throwable lanzada = catchThrowable(accion);
        assertThat(lanzada).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) lanzada).getErrorCode()).isEqualTo(codigoEsperado);
    }

    // ── listarCuestionariosActivos ─────────────────────────────
    @Test
    @DisplayName("ADMIN/COORDINADOR ve todos los cuestionarios sin consultar el perfil")
    void gestorVeTodosLosCuestionarios() {
        UsuarioEntity admin = mock(UsuarioEntity.class);
        when(admin.hasRole(TipoRol.ROLE_ADMIN)).thenReturn(true);

        when(cuestionarioRepository.findAllActivos())
                .thenReturn(List.of(mock(CuestionarioEntity.class), mock(CuestionarioEntity.class)));
        when(cuestionarioMapper.toResponse(any())).thenReturn(mock(CuestionarioResponse.class));

        List<CuestionarioResponse> resultado = service.listarCuestionariosActivos(admin);

        assertThat(resultado).hasSize(2);
        verify(usuarioPerfilRepository, never()).findByUsuarioEntId(anyLong());
    }

    @Test
    @DisplayName("el estudiante ve los generales y los de su propio programa")
    void estudianteVeGeneralesYDeSuPrograma() {
        CuestionarioEntity general = cuestionarioConPrograma(null);
        CuestionarioEntity propio = cuestionarioConPrograma(ProgramaIngenieria.INGENIERIA_DE_SISTEMAS);
        CuestionarioEntity ajeno = cuestionarioConPrograma(ProgramaIngenieria.INGENIERIA_DE_MATERIALES);

        when(cuestionarioRepository.findAllActivos()).thenReturn(List.of(general, propio, ajeno));

        UsuarioPerfilEntity perfil = mock(UsuarioPerfilEntity.class);
        when(perfil.getProgramaIngenieria()).thenReturn(ProgramaIngenieria.INGENIERIA_DE_SISTEMAS);
        when(usuarioPerfilRepository.findByUsuarioEntId(ID_EST)).thenReturn(Optional.of(perfil));
        when(cuestionarioMapper.toResponse(any())).thenReturn(mock(CuestionarioResponse.class));

        List<CuestionarioResponse> resultado = service.listarCuestionariosActivos(estudiante());

        assertThat(resultado).hasSize(2);
        verify(cuestionarioMapper).toResponse(general);
        verify(cuestionarioMapper).toResponse(propio);
        verify(cuestionarioMapper, never()).toResponse(ajeno);
    }

    @Test
    @DisplayName("el estudiante sin programa solo ve los cuestionarios generales")
    void estudianteSinProgramaSoloVeGenerales() {
        CuestionarioEntity general = cuestionarioConPrograma(null);
        CuestionarioEntity dirigido = cuestionarioConPrograma(ProgramaIngenieria.INGENIERIA_DE_SISTEMAS);

        when(cuestionarioRepository.findAllActivos()).thenReturn(List.of(general, dirigido));
        when(usuarioPerfilRepository.findByUsuarioEntId(ID_EST)).thenReturn(Optional.empty());
        when(cuestionarioMapper.toResponse(any())).thenReturn(mock(CuestionarioResponse.class));

        List<CuestionarioResponse> resultado = service.listarCuestionariosActivos(estudiante());

        assertThat(resultado).hasSize(1);
        verify(cuestionarioMapper).toResponse(general);
        verify(cuestionarioMapper, never()).toResponse(dirigido);
    }

    @Test
    @DisplayName("con usuario null no se filtra (devuelve todos)")
    void usuarioNullDevuelveTodos() {
        when(cuestionarioRepository.findAllActivos())
                .thenReturn(List.of(mock(CuestionarioEntity.class), mock(CuestionarioEntity.class)));
        when(cuestionarioMapper.toResponse(any())).thenReturn(mock(CuestionarioResponse.class));

        List<CuestionarioResponse> resultado = service.listarCuestionariosActivos(null);

        assertThat(resultado).hasSize(2);
        verify(usuarioPerfilRepository, never()).findByUsuarioEntId(anyLong());
    }

    // ── actualizarCuestionario ─────────────────────────────────
    @Test
    @DisplayName("aplica el programa objetivo y guarda cuando el cuestionario es editable")
    void actualizaYAplicaProgramaObjetivoCuandoEsEditable() {
        CuestionarioEntity ent = mock(CuestionarioEntity.class);
        when(ent.isEditable()).thenReturn(true);
        when(cuestionarioRepository.findActivoById(1L)).thenReturn(Optional.of(ent));
        when(cuestionarioRepository.save(ent)).thenReturn(ent);

        CuestionarioResponse esperada = mock(CuestionarioResponse.class);
        when(cuestionarioMapper.toResponse(ent)).thenReturn(esperada);

        ActualizarCuestionarioRequest request = ActualizarCuestionarioRequest.builder()
                .nombre("Nuevo nombre")
                .programaObjetivo(ProgramaIngenieria.INGENIERIA_DE_SISTEMAS)
                .build();

        CuestionarioResponse resultado = service.actualizarCuestionario(1L, request);

        assertThat(resultado).isSameAs(esperada);
        verify(ent).setProgramaObjetivo(ProgramaIngenieria.INGENIERIA_DE_SISTEMAS);
        verify(cuestionarioRepository).save(ent);
    }

    @Test
    @DisplayName("rechaza la edición cuando el cuestionario no es editable (no está en BORRADOR)")
    void rechazaActualizarCuandoNoEsEditable() {
        CuestionarioEntity ent = mock(CuestionarioEntity.class);
        when(ent.isEditable()).thenReturn(false);
        when(cuestionarioRepository.findActivoById(1L)).thenReturn(Optional.of(ent));

        ActualizarCuestionarioRequest request = ActualizarCuestionarioRequest.builder()
                .nombre("Intento de cambio")
                .build();

        assertErrorDeNegocio(() -> service.actualizarCuestionario(1L, request), "NOT_EDITABLE");
        verify(cuestionarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("lanza ResourceNotFoundException al actualizar un cuestionario inexistente")
    void lanzaCuandoElCuestionarioNoExiste() {
        when(cuestionarioRepository.findActivoById(99L)).thenReturn(Optional.empty());

        ActualizarCuestionarioRequest request = ActualizarCuestionarioRequest.builder().build();

        Throwable lanzada = catchThrowable(() -> service.actualizarCuestionario(99L, request));

        assertThat(lanzada).isInstanceOf(ResourceNotFoundException.class);
        verify(cuestionarioRepository, never()).save(any());
    }
}
