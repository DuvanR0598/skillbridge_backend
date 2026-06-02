package com.udea.skillbridge.seguridad.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.seguridad.entity.UsuarioEntity;

@Repository
public interface IUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
	
	Optional<UsuarioEntity> findByEmail(String email);
	
	Optional<UsuarioEntity> findByIdGoogle(String idGoogle);
	
	boolean existsByEmail(String email);
	
    @Modifying
    @Query("UPDATE UsuarioEntity u SET u.ultimoLoginAt = :ultimoLogin WHERE u.id = :idUsuario")
    void actualizarUltimoLogin(Long idUsuario, LocalDateTime ultimoLogin);

}
