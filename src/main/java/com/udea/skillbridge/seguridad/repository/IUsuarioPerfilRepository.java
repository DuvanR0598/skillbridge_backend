package com.udea.skillbridge.seguridad.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.seguridad.entity.UsuarioPerfilEntity;

@Repository
public interface IUsuarioPerfilRepository extends JpaRepository<UsuarioPerfilEntity, Long>{
	
    Optional<UsuarioPerfilEntity> findByUsuarioEntId(Long idUsuario);
    
    boolean existsByUsuarioEntId(Long idUsuario);
    
    // Contar perfiles completos (para reportes del admin)
    @Query("""
        SELECT COUNT(p) FROM UsuarioPerfilEntity p
        WHERE p.fechaNacimiento        IS NOT NULL
          AND p.programaIngenieria     IS NOT NULL
          AND p.semestreAcademico      IS NOT NULL
    """)
    Long countPerfilesCompletos();

}
