package com.udea.skillbridge.seguridad.oauth2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioPerfilEntity;
import com.udea.skillbridge.seguridad.enums.AuthProvider;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.seguridad.repository.IRolRepository;
import com.udea.skillbridge.seguridad.repository.IUsuarioPerfilRepository;
import com.udea.skillbridge.seguridad.repository.IUsuarioRepository;
import com.udea.skillbridge.seguridad.service.JwtService;
import com.udea.skillbridge.seguridad.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Se ejecuta cuando Google confirma la identidad del usuario.
 * Busca o crea el usuario en la BD y genera el JWT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	
	private final IUsuarioRepository usuarioRepository;
    private final IRolRepository rolRepository;
    private final IUsuarioPerfilRepository usuarioPerfilRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /** URL del frontend a donde se redirige tras el login con Google. */
    @Value("${app.oauth2.redirect-uri:http://localhost:4200/oauth2/callback}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        GoogleOAuth2UserInfo usuarioInfo = new GoogleOAuth2UserInfo(oidcUser.getAttributes());

        // Buscar usuario existente por idGoogle o email
        UsuarioEntity usuarioEnt = usuarioRepository.findByIdGoogle(usuarioInfo.getId())
                .orElseGet(() -> usuarioRepository.findByEmail(usuarioInfo.getEmail())
                        .map(existente -> {
                            // El usuario existía con LOCAL — vincular Google
                        	log.info("Vinculando cuenta local con Google ID: {}", existente.getEmail());
                            existente.setIdGoogle(usuarioInfo.getId());
                            existente.setAuthProvider(AuthProvider.GOOGLE);
                            existente.setEmailVerificado(true);
                            if (existente.getAvatarUrl() == null) {
                                existente.setAvatarUrl(usuarioInfo.getAvatarUrl());
                            }
                            UsuarioEntity guardado = usuarioRepository.save(existente);

                            // Sincronizar avatar de Google en usuario_perfil si aún no tiene uno
                            if (usuarioInfo.getAvatarUrl() != null) {
                                UsuarioPerfilEntity perfil = usuarioPerfilRepository
                                        .findByUsuarioEntId(guardado.getId())
                                        .orElseGet(() -> {
                                            UsuarioPerfilEntity nuevo = UsuarioPerfilEntity.builder()
                                                    .usuarioEnt(guardado).build();
                                            return usuarioPerfilRepository.save(nuevo);
                                        });
                                if (perfil.getAvatarUrl() == null) {
                                    perfil.setAvatarUrl(usuarioInfo.getAvatarUrl());
                                    usuarioPerfilRepository.save(perfil);
                                }
                            }

                            return guardado;
                        })
                        .orElseGet(() -> createNewGoogleUser(usuarioInfo))
                );

        // Si la cuenta está deshabilitada, NO se permite el ingreso (igual que en
        // el login local). Se redirige al frontend con un error y sin tokens.
        if (!usuarioEnt.isEnabled()) {
            log.warn("Login con Google rechazado: cuenta deshabilitada ({})", usuarioEnt.getEmail());
            String errorUrl = UriComponentsBuilder
                    .fromUriString(frontendRedirectUri)
                    .queryParam("error", "account_disabled")
                    .build(true)
                    .toUriString();
            response.sendRedirect(errorUrl);
            return;
        }

        // Actualizar último login
        usuarioEnt.setUltimoLoginAt(LocalDateTime.now());
        usuarioRepository.save(usuarioEnt);

        // Generar tokens
        String accessToken  = jwtService.generateAccessToken(usuarioEnt);
        String refreshToken = refreshTokenService
                .create(usuarioEnt, request.getHeader("User-Agent"))
                .getToken();

        boolean perfilCompletado = Boolean.TRUE.equals(usuarioEnt.getPerfilCompleto());

        // Redirigir al frontend con los tokens en los query params.
        // El componente Oauth2Callback de Angular los lee y completa el login.
        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendRedirectUri)
                .queryParam("token",   URLEncoder.encode(accessToken,  StandardCharsets.UTF_8))
                .queryParam("refresh", URLEncoder.encode(refreshToken, StandardCharsets.UTF_8))
                .queryParam("profileCompleted", perfilCompletado)
                .build(true)
                .toUriString();

        log.info("Usuario {} autenticado con Google — redirigiendo al frontend", usuarioEnt.getEmail());

        response.sendRedirect(targetUrl);
    }

    private UsuarioEntity createNewGoogleUser(GoogleOAuth2UserInfo info) {
        var studentRole = rolRepository.findByNombre(TipoRol.ROLE_ESTUDIANTE)
                .orElseThrow(() -> new IllegalStateException(
                    "Rol ROLE_ESTUDIANTE no encontrado. Verifique el DataInitializer."
                ));

        UsuarioEntity newUsuario = UsuarioEntity.builder()
                .nombre(info.getNombre())
                .apellido(info.getApellido())
                .email(info.getEmail())
                .idGoogle(info.getId())
                .avatarUrl(info.getAvatarUrl())
                .authProvider(AuthProvider.GOOGLE)
                .emailVerificado(info.isVerificado())
                .activado(true)
                .roles(Set.of(studentRole))
                .build();

        UsuarioEntity guardar = usuarioRepository.save(newUsuario);
        log.info("Nuevo usuario creado via Google: {}", guardar.getEmail());
        
        // Google nos da el avatar directamente
        UsuarioPerfilEntity googlePerfil = UsuarioPerfilEntity.builder()
                .usuarioEnt(guardar)
                .avatarUrl(info.getAvatarUrl())        // Google lo proporciona
                .build();
        usuarioPerfilRepository.save(googlePerfil);
        return guardar;
    }

}
