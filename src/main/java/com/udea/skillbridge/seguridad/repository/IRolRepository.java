package com.udea.skillbridge.seguridad.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.udea.skillbridge.seguridad.entity.RolEntity;
import com.udea.skillbridge.seguridad.enums.TipoRol;

public interface IRolRepository extends JpaRepository<RolEntity, Long> {
	
	Optional<RolEntity> findByNombre(TipoRol nombre);
	
	Set<RolEntity> findByNombreIn(Set<TipoRol> nombres);

}
