package com.udea.skillbridge.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Envoltura genérica para respuestas paginadas.
 * page es 0-based (igual que Spring Data).
 */
public record PaginaResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PaginaResponse<T> of(Page<T> p) {
        return new PaginaResponse<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.isFirst(),
                p.isLast()
        );
    }
}
