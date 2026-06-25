package com.udea.skillbridge.seguridad.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.seguridad.entity.RefreshTokenEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.repository.IRefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	
	private final IRefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-token-expiration-days}")
    private Integer refreshTokenExpirationDays;

    @Transactional
    public RefreshTokenEntity create(UsuarioEntity usuarioEnt, String deviceInfo) {
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .token(UUID.randomUUID().toString())
                .usuario(usuarioEnt)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .revocado(false)
                .deviceInfo(deviceInfo)
                .build();

        return refreshTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public RefreshTokenEntity validate(String token) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(
                    "Refresh token inválido o no encontrado.",
                    "INVALID_REFRESH_TOKEN"
                ));

        if (!refreshToken.isValid()) {
            throw new BusinessException(
                "El refresh token ha expirado o fue revocado. " +
                "Por favor inicie sesión nuevamente.",
                "REFRESH_TOKEN_EXPIRED"
            );
        }

        return refreshToken;
    }

    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenRepository.revocarAllByUsuarioId(userId);
        log.info("Todos los refresh tokens del usuario {} revocados", userId);
    }

    /**
     * Limpieza automática de tokens expirados/revocados.
     * Se ejecuta todos los días a las 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.eliminarExpiredAndRevocado();
        log.info("Limpieza de refresh tokens expirados completada");
    }

}
