package com.udea.skillbridge.seguridad.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.udea.skillbridge.seguridad.entity.RefreshTokenEntity;

public interface IRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long>{
	
	Optional<RefreshTokenEntity> findByToken(String token);
	
    // Revocar todos los tokens de un usuario (logout de todos los dispositivos)
    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revocado = true WHERE r.usuario.id = :idUsuario")
    void revocarAllByUsuarioId(Long idUsuario);
    
    // Limpiar tokens expirados (tarea programada)
    @Modifying
    @Query("""
        DELETE FROM RefreshTokenEntity r
        WHERE r.expiresAt < CURRENT_TIMESTAMP
           OR r.revocado   = true
    """)
    void eliminarExpiredAndRevocado();

}
