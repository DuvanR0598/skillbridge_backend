package com.udea.skillbridge.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.mapper.IPreguntaMapper;
import com.udea.skillbridge.repository.IDimensionRepository;
import com.udea.skillbridge.repository.IPreguntaRepository;
import com.udea.skillbridge.validation.OpcionOrdenValidador;
import com.udea.skillbridge.validation.PreguntaValidadorFactory;

/**
 * Pruebas unitarias de la validación de imagen de
 * {@link PreguntaServiceImpl#subirImagen} (que invoca al validador privado
 * validarArchivoDeImagen).
 *
 * Reglas: archivo no vacío, formato JPEG/PNG/WebP y tamaño ≤ 5 MB.
 * Los casos de rechazo lanzan la excepción ANTES de escribir en disco.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PreguntaServiceImplSubirImagenTest {

    @Mock private IPreguntaRepository preguntaRepository;
    @Mock private PreguntaValidadorFactory validadorFactory;
    @Mock private OpcionOrdenValidador opcionOrdenValidador;
    @Mock private IPreguntaMapper preguntaMapper;
    @Mock private IDimensionRepository dimensionRepository;

    @InjectMocks private PreguntaServiceImpl service;

    private void assertErrorDeNegocio(ThrowingCallable accion, String codigoEsperado) {
        Throwable lanzada = catchThrowable(accion);
        assertThat(lanzada).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) lanzada).getErrorCode()).isEqualTo(codigoEsperado);
    }

    // ── Casos de error / borde ─────────────────────────────────
    @Test
    @DisplayName("rechaza cuando el archivo es null")
    void rechazaArchivoNull() {
        assertErrorDeNegocio(() -> service.subirImagen(null), "IMAGE_EMPTY");
    }

    @Test
    @DisplayName("rechaza cuando el archivo está vacío")
    void rechazaArchivoVacio() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertErrorDeNegocio(() -> service.subirImagen(file), "IMAGE_EMPTY");
    }

    @Test
    @DisplayName("rechaza cuando el tipo de contenido no es una imagen permitida")
    void rechazaFormatoNoPermitido() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");

        assertErrorDeNegocio(() -> service.subirImagen(file), "INVALID_IMAGE_FORMAT");
    }

    @Test
    @DisplayName("rechaza cuando el tipo de contenido es null")
    void rechazaContentTypeNull() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(null);

        assertErrorDeNegocio(() -> service.subirImagen(file), "INVALID_IMAGE_FORMAT");
    }

    @Test
    @DisplayName("rechaza cuando la imagen supera los 5 MB")
    void rechazaImagenMuyGrande() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(6L * 1024 * 1024); // 6 MB

        assertErrorDeNegocio(() -> service.subirImagen(file), "IMAGE_TOO_LARGE");
    }

    // ── Caso feliz (escribe y limpia el archivo) ───────────────
    @Test
    @DisplayName("guarda la imagen válida y devuelve una URL /uploads/preguntas/*.jpg")
    void guardaImagenValidaYDevuelveUrl() throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("foto.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4 }));

        String url = null;
        try {
            url = service.subirImagen(file);

            assertThat(url).startsWith("/uploads/preguntas/").endsWith(".jpg");
            // El archivo debe existir físicamente tras guardarse.
            Path guardado = Path.of(url.replace("/uploads/", "uploads/"));
            assertThat(Files.exists(guardado)).isTrue();
        } finally {
            // Limpieza: no dejar artefactos en el repositorio.
            if (url != null) {
                Files.deleteIfExists(Path.of(url.replace("/uploads/", "uploads/")));
            }
        }
    }
}
