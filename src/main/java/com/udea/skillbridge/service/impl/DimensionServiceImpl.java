package com.udea.skillbridge.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.common.exception.ResourceNotFoundException;
import com.udea.skillbridge.dto.request.DimensionRequest;
import com.udea.skillbridge.dto.response.DimensionResponse;
import com.udea.skillbridge.entity.DimensionEntity;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.repository.IDimensionRepository;
import com.udea.skillbridge.service.IDimensionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DimensionServiceImpl implements IDimensionService {

    private final IDimensionRepository dimensionRepository;

    @Override
    @Transactional
    public DimensionResponse crear(DimensionRequest request) {
        validarNombreUnico(request.getSkill(), request.getNombre(), null);

        DimensionEntity dimension = DimensionEntity.builder()
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .skill(request.getSkill())
                .build();

        DimensionEntity guardar = dimensionRepository.save(dimension);
        log.info("Dimensión creada: '{}' para skill {}", guardar.getNombre(), guardar.getSkill());
        return toResponse(guardar);
    }

    @Override
    @Transactional
    public DimensionResponse actualizar(Long id, DimensionRequest request) {
        DimensionEntity dimension = buscar(id);
        validarNombreUnico(request.getSkill(), request.getNombre(), id);

        dimension.setNombre(request.getNombre().trim());
        dimension.setDescripcion(request.getDescripcion());
        dimension.setSkill(request.getSkill());

        return toResponse(dimensionRepository.save(dimension));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DimensionResponse> listar(SkillTipo skill) {
        List<DimensionEntity> dimensiones = (skill != null)
                ? dimensionRepository.findBySkill(skill)
                : dimensionRepository.findAll();
        return dimensiones.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DimensionResponse findById(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        DimensionEntity dimension = buscar(id);
        dimensionRepository.delete(dimension);
        log.info("Dimensión [{}] eliminada", id);
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private DimensionEntity buscar(Long id) {
        return dimensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dimensión", id));
    }

    private void validarNombreUnico(SkillTipo skill, String nombre, Long idExcluir) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        boolean existe = (idExcluir == null)
                ? dimensionRepository.existsBySkillAndNombreIgnoreCase(skill, nombreLimpio)
                : dimensionRepository.existsBySkillAndNombreIgnoreCaseAndIdNot(skill, nombreLimpio, idExcluir);
        if (existe) {
            throw new BusinessException(
                "Ya existe una dimensión con el nombre '" + nombreLimpio + "' para este skill.",
                "DIMENSION_DUPLICADA"
            );
        }
    }

    private DimensionResponse toResponse(DimensionEntity d) {
        return DimensionResponse.builder()
                .id(d.getId())
                .nombre(d.getNombre())
                .descripcion(d.getDescripcion())
                .skill(d.getSkill())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
