package com.udea.skillbridge.seguridad.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.enums.TipoRol;

@Repository
public interface IUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

	Optional<UsuarioEntity> findByEmail(String email);

	Optional<UsuarioEntity> findByIdGoogle(String idGoogle);

	boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE UsuarioEntity u SET u.ultimoLoginAt = :ultimoLogin WHERE u.id = :idUsuario")
    void actualizarUltimoLogin(Long idUsuario, LocalDateTime ultimoLogin);

    /** Cuántos usuarios tienen un rol dado. */
    @Query("SELECT COUNT(u) FROM UsuarioEntity u JOIN u.roles r WHERE r.nombre = :rol")
    long countByRol(TipoRol rol);

    /** Cuántos usuarios activos tienen un rol dado. */
    @Query("SELECT COUNT(u) FROM UsuarioEntity u JOIN u.roles r WHERE r.nombre = :rol AND u.activado = true")
    long countByRolAndActivadoTrue(TipoRol rol);

}
