package com.udea.skillbridge.seguridad.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.seguridad.dto.request.CambiarContrasenaRequest;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.mapper.IUsuarioMapper;
import com.udea.skillbridge.seguridad.repository.IRolRepository;
import com.udea.skillbridge.seguridad.repository.IUsuarioRepository;

/**
 * Pruebas unitarias de {@link UsuarioService#cambiarContrasena}.
 *
 * Reglas bajo prueba:
 *  - Cuenta sin contraseña local (Google) → no se puede cambiar.
 *  - La contraseña actual debe coincidir.
 *  - La nueva no puede ser igual a la actual.
 *  - Camino feliz: codifica y persiste.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceCambiarContrasenaTest {

    private static final Long ID = 5L;
    private static final String HASH = "$2a$10$hashExistente";
    private static final String ACTUAL = "ActualClave1";
    private static final String NUEVA = "NuevaClave1";

    @Mock private IUsuarioRepository userRepository;
    @Mock private IRolRepository roleRepository;
    @Mock private IUsuarioMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UsuarioService service;

    // ── Auxiliares ─────────────────────────────────────────────
    private CambiarContrasenaRequest request(String actual, String nueva) {
        return CambiarContrasenaRequest.builder()
                .contrasenaActual(actual)
                .contrasenaNueva(nueva)
                .build();
    }

    private UsuarioEntity usuarioConHash(String hash) {
        UsuarioEntity u = mock(UsuarioEntity.class);
        when(u.getPasswordHash()).thenReturn(hash);
        return u;
    }

    private void assertErrorDeNegocio(ThrowingCallable accion, String codigoEsperado) {
        Throwable lanzada = catchThrowable(accion);
        assertThat(lanzada).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) lanzada).getErrorCode()).isEqualTo(codigoEsperado);
    }

    // ── Caso feliz ─────────────────────────────────────────────
    @Test
    @DisplayName("codifica y guarda la nueva contraseña cuando todo es válido")
    void cambiaContrasenaCorrectamente() {
        UsuarioEntity u = usuarioConHash(HASH);
        when(userRepository.findById(ID)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(ACTUAL, HASH)).thenReturn(true);   // actual correcta
        when(passwordEncoder.matches(NUEVA, HASH)).thenReturn(false);   // nueva distinta
        when(passwordEncoder.encode(NUEVA)).thenReturn("nuevoHash");

        service.cambiarContrasena(ID, request(ACTUAL, NUEVA));

        verify(u).setPasswordHash("nuevoHash");
        verify(userRepository).save(u);
    }

    // ── Casos de error / borde ─────────────────────────────────
    @Test
    @DisplayName("rechaza cuando la cuenta no tiene contraseña local (Google)")
    void rechazaCuandoNoHayContrasenaLocal() {
        UsuarioEntity u = usuarioConHash(null);
        when(userRepository.findById(ID)).thenReturn(Optional.of(u));

        assertErrorDeNegocio(
                () -> service.cambiarContrasena(ID, request(ACTUAL, NUEVA)),
                "NO_LOCAL_PASSWORD");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza cuando la contraseña local está vacía/en blanco")
    void rechazaCuandoLaContrasenaLocalEstaEnBlanco() {
        UsuarioEntity u = usuarioConHash("   ");
        when(userRepository.findById(ID)).thenReturn(Optional.of(u));

        assertErrorDeNegocio(
                () -> service.cambiarContrasena(ID, request(ACTUAL, NUEVA)),
                "NO_LOCAL_PASSWORD");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza cuando la contraseña actual es incorrecta")
    void rechazaCuandoLaContrasenaActualEsIncorrecta() {
        UsuarioEntity u = usuarioConHash(HASH);
        when(userRepository.findById(ID)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(ACTUAL, HASH)).thenReturn(false);

        assertErrorDeNegocio(
                () -> service.cambiarContrasena(ID, request(ACTUAL, NUEVA)),
                "INVALID_CURRENT_PASSWORD");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("rechaza cuando la nueva contraseña es igual a la actual")
    void rechazaCuandoLaNuevaEsIgualALaActual() {
        UsuarioEntity u = usuarioConHash(HASH);
        when(userRepository.findById(ID)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(ACTUAL, HASH)).thenReturn(true);   // actual correcta
        when(passwordEncoder.matches(NUEVA, HASH)).thenReturn(true);    // nueva == actual

        assertErrorDeNegocio(
                () -> service.cambiarContrasena(ID, request(ACTUAL, NUEVA)),
                "SAME_PASSWORD");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("lanza ResourceNotFoundException cuando el usuario no existe")
    void lanzaCuandoElUsuarioNoExiste() {
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        Throwable lanzada = catchThrowable(
                () -> service.cambiarContrasena(ID, request(ACTUAL, NUEVA)));

        assertThat(lanzada).isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).save(any());
    }
}
