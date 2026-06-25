package com.udea.skillbridge.repository;

import org.springframework.data.jpa.domain.Specification;

import com.udea.skillbridge.entity.PreguntaEntity;
import com.udea.skillbridge.enums.SkillTipo;
import com.udea.skillbridge.enums.TipoPregunta;

public final class PreguntaSpec {

    private PreguntaSpec() {}

    public static Specification<PreguntaEntity> porTipo(TipoPregunta tipo) {
        return (root, query, cb) ->
            tipo == null ? cb.conjunction() : cb.equal(root.get("tipoPregunta"), tipo);
    }

    public static Specification<PreguntaEntity> porTexto(String texto) {
        return (root, query, cb) -> {
            if (texto == null || texto.isBlank()) return cb.conjunction();
            String patron = "%" + texto.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("texto")), patron);
        };
    }

    public static Specification<PreguntaEntity> porSkill(SkillTipo skill) {
        return (root, query, cb) -> {
            if (skill == null) return cb.conjunction();
            var joinDim = root.join("dimension", jakarta.persistence.criteria.JoinType.LEFT);
            return cb.equal(joinDim.get("skill"), skill);
        };
    }

    public static Specification<PreguntaEntity> porDimension(Long idDimension) {
        return (root, query, cb) -> {
            if (idDimension == null) return cb.conjunction();
            var joinDim = root.join("dimension", jakarta.persistence.criteria.JoinType.LEFT);
            return cb.equal(joinDim.get("id"), idDimension);
        };
    }
}
