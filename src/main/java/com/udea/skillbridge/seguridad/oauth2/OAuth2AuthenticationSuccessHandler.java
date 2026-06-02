package com.udea.skillbridge.seguridad.oauth2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.skillbridge.common.response.ApiResponse;
import com.udea.skillbridge.seguridad.dto.response.AuthResponse;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioPerfilEntity;
import com.udea.skillbridge.seguridad.enums.AuthProvider;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.seguridad.mapper.IUsuarioMapper;
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
    private final IUsuarioMapper usuarioMapper;
    private final ObjectMapper objectMapper;

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
                            return usuarioRepository.save(existente);
                        })
                        .orElseGet(() -> createNewGoogleUser(usuarioInfo))
                );

        // Actualizar último login
        usuarioEnt.setUltimoLoginAt(LocalDateTime.now());
        usuarioRepository.save(usuarioEnt);

        // Generar tokens
        String accessToken  = jwtService.generateAccessToken(usuarioEnt);
        String refreshToken = refreshTokenService
                .create(usuarioEnt, request.getHeader("User-Agent"))
                .getToken();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tipoToken("Bearer")
                .expiraEn(jwtService.getAccessTokenExpiration())
                .usuario(usuarioMapper.toResponse(usuarioEnt))
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);

        objectMapper.writeValue(
            response.getWriter(),
            ApiResponse.ok(authResponse, "Login con Google exitoso")
        );

        log.info("Usuario {} autenticado con Google", usuarioEnt.getEmail());
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
