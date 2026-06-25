package com.udea.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udea.skillbridge.entity.DimensionEntity;
import com.udea.skillbridge.enums.SkillTipo;

@Repository
public interface IDimensionRepository extends JpaRepository<DimensionEntity, Long> {

    List<DimensionEntity> findBySkill(SkillTipo skill);

    boolean existsBySkillAndNombreIgnoreCase(SkillTipo skill, String nombre);

    boolean existsBySkillAndNombreIgnoreCaseAndIdNot(SkillTipo skill, String nombre, Long id);
}
