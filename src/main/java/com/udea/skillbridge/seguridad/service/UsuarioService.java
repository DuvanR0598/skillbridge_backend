package com.udea.skillbridge.seguridad.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.seguridad.dto.request.ActualizarUsuariosRolesRequest;
import com.udea.skillbridge.seguridad.dto.response.UsuarioResponse;
import com.udea.skillbridge.seguridad.entity.RolEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.seguridad.mapper.IUsuarioMapper;
import com.udea.skillbridge.seguridad.repository.IRolRepository;
import com.udea.skillbridge.seguridad.repository.IUsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {
	
	private final IUsuarioRepository userRepository;
    private final IRolRepository roleRepository;
    private final IUsuarioMapper userMapper;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findById(Long id) {
        return userMapper.toResponse(findEntityById(id));
    }

    @Transactional
    public UsuarioResponse updateRoles(Long idUsuario, ActualizarUsuariosRolesRequest request) {
        UsuarioEntity usuarioEnt = findEntityById(idUsuario);

        Set<RolEntity> newRoles = roleRepository.findByNombreIn(request.getRoles());

        if (newRoles.size() != request.getRoles().size()) {
            throw new BusinessException(
                "Uno o más roles no existen en el sistema.",
                "INVALID_ROLES"
            );
        }

        // Proteger al último administrador: no permitir quitarle el rol ADMIN
        // si es el único que queda en el sistema.
        boolean eraAdmin   = usuarioEnt.hasRole(TipoRol.ROLE_ADMIN);
        boolean sigueAdmin = request.getRoles().contains(TipoRol.ROLE_ADMIN);

        if (eraAdmin && !sigueAdmin && userRepository.countByRol(TipoRol.ROLE_ADMIN) <= 1) {
            throw new BusinessException(
                "No se puede quitar el rol de administrador: es el último administrador del sistema.",
                "LAST_ADMIN"
            );
        }

        usuarioEnt.getRoles().clear();
        usuarioEnt.getRoles().addAll(newRoles);

        UsuarioEntity guardar = userRepository.save(usuarioEnt);
        log.info("Roles del usuario {} actualizados: {}", idUsuario, request.getRoles());
        return userMapper.toResponse(guardar);
    }

    @Transactional
    public void toggleEnabled(Long userId) {
    	UsuarioEntity usuarioEnt = findEntityById(userId);

        // Proteger al último administrador activo: no permitir deshabilitarlo.
        boolean seVaADeshabilitar = Boolean.TRUE.equals(usuarioEnt.getActivado());
        if (seVaADeshabilitar
                && usuarioEnt.hasRole(TipoRol.ROLE_ADMIN)
                && userRepository.countByRolAndActivadoTrue(TipoRol.ROLE_ADMIN) <= 1) {
            throw new BusinessException(
                "No se puede deshabilitar al último administrador activo del sistema.",
                "LAST_ADMIN"
            );
        }

        usuarioEnt.setActivado(!usuarioEnt.getActivado());
        userRepository.save(usuarioEnt);
        log.info("Usuario {} {}", userId,
            Boolean.TRUE.equals(usuarioEnt.getActivado()) ? "habilitado" : "deshabilitado");
    }

    public UsuarioEntity findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UsuarioEntity", id));
    }

}
