package com.udea.skillbridge.seguridad.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.seguridad.dto.request.LoginRequest;
import com.udea.skillbridge.seguridad.dto.request.RefreshTokenRequest;
import com.udea.skillbridge.seguridad.dto.request.RegistrarRequest;
import com.udea.skillbridge.seguridad.dto.response.AuthResponse;
import com.udea.skillbridge.seguridad.entity.RefreshTokenEntity;
import com.udea.skillbridge.seguridad.entity.RolEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioPerfilEntity;
import com.udea.skillbridge.seguridad.enums.AuthProvider;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.seguridad.mapper.IUsuarioMapper;
import com.udea.skillbridge.seguridad.repository.IRefreshTokenRepository;
import com.udea.skillbridge.seguridad.repository.IRolRepository;
import com.udea.skillbridge.seguridad.repository.IUsuarioPerfilRepository;
import com.udea.skillbridge.seguridad.repository.IUsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final IUsuarioRepository userRepository;
    private final IRolRepository roleRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final IUsuarioMapper usuarioMapper;
    private final IUsuarioPerfilRepository usuarioPerfilRepository;

    // ── Registro ────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegistrarRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                "El email " + request.getEmail() + " ya está registrado.",
                "EMAIL_ALREADY_EXISTS"
            );
        }

        // Por defecto todo registro es ESTUDIANTE
        RolEntity rolEstudiante = roleRepository.findByNombre(TipoRol.ROLE_ESTUDIANTE)
                .orElseThrow(() -> new IllegalStateException(
                    "Rol ROLE_STUDENT no encontrado."
                ));

        UsuarioEntity usuarioEnt = UsuarioEntity.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .authProvider(AuthProvider.LOCAL)
                .emailVerificado(false)
                .perfilCompleto(false)
                .activado(true)
                .roles(Set.of(rolEstudiante))
                .build();

        UsuarioEntity guardar = userRepository.save(usuarioEnt);
        log.info("Usuario registrado: {}", guardar.getEmail());
        
         // Crear perfil vacío inmediatamente
        UsuarioPerfilEntity perfilVacio = UsuarioPerfilEntity.builder()
                .usuarioEnt(guardar)
                .build();
        usuarioPerfilRepository.save(perfilVacio);

        log.info("Usuario registrado con perfil vacío: {}", guardar.getEmail());

        return buildAuthResponse(guardar, null);
    }

    // ── Login ───────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {

        // Spring Security valida email + password
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException ex) {
            throw new BusinessException(
                "Email o contraseña incorrectos.",
                "INVALID_CREDENTIALS"
            );
        } catch (DisabledException ex) {
            throw new BusinessException(
                "La cuenta está deshabilitada. Contacte al administrador.",
                "ACCOUNT_DISABLED"
            );
        }

        UsuarioEntity usuarioEnt = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                    "Usuario no encontrado.", "USER_NOT_FOUND"
                ));

        // Actualizar último login
        userRepository.actualizarUltimoLogin(usuarioEnt.getId(), LocalDateTime.now());

        log.info("Login exitoso: {}", usuarioEnt.getEmail());
        return buildAuthResponse(usuarioEnt, request.getDeviceInfo());
    }

    // ── Refresh token ───────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {

        RefreshTokenEntity storedToken = refreshTokenService.validate(request.getRefreshToken());
        UsuarioEntity usuarioEnt = storedToken.getUsuario();

        // Rotar el refresh token (invalidar el viejo, generar uno nuevo)
        storedToken.setRevocado(true);
        refreshTokenRepository.save(storedToken);

        return buildAuthResponse(usuarioEnt, storedToken.getDeviceInfo());
    }

    // ── Logout ──────────────────────────────────────────────────────

    @Transactional
    public void logout(Long idUsuario) {
        refreshTokenService.revokeAll(idUsuario);
        log.info("Logout: todos los tokens del usuario {} revocados", idUsuario);
    }

    // ── Helper ──────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(UsuarioEntity usuarioEnt, String deviceInfo) {
        String accessToken = jwtService.generateAccessToken(usuarioEnt);
        String refreshToken = refreshTokenService
                .create(usuarioEnt, deviceInfo)
                .getToken();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tipoToken("Bearer")
                .expiraEn(jwtService.getAccessTokenExpiration())
                .perfilCompletado(usuarioEnt.getPerfilCompleto())
                .usuario(usuarioMapper.toResponse(usuarioEnt))
                .build();
    }

}
