package com.udea.skillbridge.seguridad.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udea.skillbridge.common.exception.BusinessException;
import com.udea.skillbridge.seguridad.entity.UsuarioEntity;
import com.udea.skillbridge.seguridad.entity.UsuarioPerfilEntity;
import com.udea.skillbridge.seguridad.enums.TipoRol;
import com.udea.skillbridge.seguridad.repository.IUsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Genera un archivo XLSX con la información de todos los usuarios.
 */
@Service
@RequiredArgsConstructor
public class UsuarioExportService {

    private final IUsuarioRepository usuarioRepository;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLUMNAS = {
            "Tipo de identificación", "Número de identificación", "Nombres", "Apellidos",
            "Fecha de nacimiento", "Género", "Programa", "Código de programa", "Semestre",
            "Correo electrónico", "Rol", "Estado"
    };

    @Transactional(readOnly = true)
    public byte[] exportarUsuariosXlsx() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Usuarios");

            // Estilo de encabezado en negrita
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Encabezados
            Row header = sheet.createRow(0);
            for (int i = 0; i < COLUMNAS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(COLUMNAS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Filas de datos
            int rowIdx = 1;
            for (UsuarioEntity u : usuarioRepository.findAll()) {
                UsuarioPerfilEntity perfil = u.getPerfil();
                Row row = sheet.createRow(rowIdx++);

                int c = 0;
                row.createCell(c++).setCellValue(
                        u.getTipoIdentificacion() != null ? u.getTipoIdentificacion().getDisplayName() : "");
                row.createCell(c++).setCellValue(nullSafe(u.getNumeroIdentificacion()));
                row.createCell(c++).setCellValue(nullSafe(u.getNombre()));
                row.createCell(c++).setCellValue(nullSafe(u.getApellido()));
                row.createCell(c++).setCellValue(
                        perfil != null && perfil.getFechaNacimiento() != null
                                ? perfil.getFechaNacimiento().format(FECHA) : "");
                row.createCell(c++).setCellValue(
                        perfil != null && perfil.getGenero() != null
                                ? perfil.getGenero().getDisplayName() : "");
                row.createCell(c++).setCellValue(
                        perfil != null && perfil.getProgramaIngenieria() != null
                                ? perfil.getProgramaIngenieria().getDisplayName() : "");
                row.createCell(c++).setCellValue(
                        perfil != null && perfil.getProgramaIngenieria() != null
                                ? perfil.getProgramaIngenieria().getCodigo() : "");
                row.createCell(c++).setCellValue(
                        perfil != null && perfil.getSemestreAcademico() != null
                                ? String.valueOf(perfil.getSemestreAcademico()) : "");
                row.createCell(c++).setCellValue(nullSafe(u.getEmail()));
                row.createCell(c++).setCellValue(rolesLabel(u));
                row.createCell(c++).setCellValue(Boolean.TRUE.equals(u.getActivado()) ? "Activo" : "Inactivo");
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < COLUMNAS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new BusinessException(
                    "No se pudo generar el archivo de usuarios.", "EXPORT_FAILED");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Exportación de estudiantes (vista del coordinador, con filtros)
    // ─────────────────────────────────────────────────────────────

    private static final String[] COLUMNAS_EST = {
            "Tipo de identificación", "Número de identificación", "Nombres", "Apellidos",
            "Programa", "Código de programa", "Semestre", "Correo electrónico", "Estado"
    };

    /**
     * Genera un XLSX con los estudiantes, aplicando los mismos filtros que la
     * vista del coordinador: búsqueda por nombre y filtro por programa.
     *
     * @param search   texto a buscar en nombre + apellido (null/vacío = sin filtro)
     * @param programa nombre del enum ProgramaIngenieria (null/"ALL" = todos)
     */
    @Transactional(readOnly = true)
    public byte[] exportarEstudiantesXlsx(String search, String programa) {
        String term = normalizar(search);
        String[] palabras = term.isBlank() ? new String[0] : term.split("\\s+");
        boolean filtraPrograma = programa != null && !programa.isBlank() && !"ALL".equalsIgnoreCase(programa);

        List<UsuarioEntity> estudiantes = usuarioRepository.findByRol(TipoRol.ROLE_ESTUDIANTE).stream()
                .filter(u -> {
                    // Filtro por programa
                    if (filtraPrograma) {
                        var prog = u.getPerfil() != null ? u.getPerfil().getProgramaIngenieria() : null;
                        if (prog == null || !prog.name().equals(programa)) return false;
                    }
                    // Filtro por nombre (todas las palabras presentes)
                    if (palabras.length > 0) {
                        String nombre = normalizar(nullSafe(u.getNombre()) + " " + nullSafe(u.getApellido()));
                        for (String w : palabras) {
                            if (!nombre.contains(w)) return false;
                        }
                    }
                    return true;
                })
                .toList();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Estudiantes");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < COLUMNAS_EST.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(COLUMNAS_EST[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (UsuarioEntity u : estudiantes) {
                UsuarioPerfilEntity perfil = u.getPerfil();
                var prog = perfil != null ? perfil.getProgramaIngenieria() : null;
                Row row = sheet.createRow(rowIdx++);

                int c = 0;
                row.createCell(c++).setCellValue(
                        u.getTipoIdentificacion() != null ? u.getTipoIdentificacion().getDisplayName() : "");
                row.createCell(c++).setCellValue(nullSafe(u.getNumeroIdentificacion()));
                row.createCell(c++).setCellValue(nullSafe(u.getNombre()));
                row.createCell(c++).setCellValue(nullSafe(u.getApellido()));
                row.createCell(c++).setCellValue(prog != null ? prog.getDisplayName() : "");
                row.createCell(c++).setCellValue(prog != null ? prog.getCodigo() : "");
                row.createCell(c++).setCellValue(
                        perfil != null && perfil.getSemestreAcademico() != null
                                ? String.valueOf(perfil.getSemestreAcademico()) : "");
                row.createCell(c++).setCellValue(nullSafe(u.getEmail()));
                row.createCell(c++).setCellValue(Boolean.TRUE.equals(u.getActivado()) ? "Activo" : "Inactivo");
            }

            for (int i = 0; i < COLUMNAS_EST.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new BusinessException(
                    "No se pudo generar el archivo de estudiantes.", "EXPORT_FAILED");
        }
    }

    /** Quita acentos y pasa a minúsculas para comparar sin distinción. */
    private String normalizar(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.trim().toLowerCase();
    }

    private String nullSafe(String s) {
        return s != null ? s : "";
    }

    /** Roles del usuario en texto legible, separados por coma. */
    private String rolesLabel(UsuarioEntity u) {
        return u.getRoles().stream()
                .map(r -> traducirRol(r.getNombre()))
                .collect(Collectors.joining(", "));
    }

    private String traducirRol(TipoRol rol) {
        return switch (rol) {
            case ROLE_ADMIN -> "Administrador";
            case ROLE_COORDINADOR -> "Coordinador";
            case ROLE_ESTUDIANTE -> "Estudiante";
        };
    }
}
