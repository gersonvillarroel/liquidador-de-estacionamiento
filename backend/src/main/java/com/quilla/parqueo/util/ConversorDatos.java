package com.quilla.parqueo.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.quilla.parqueo.exception.CodigoError;
import com.quilla.parqueo.exception.ReglaNegocioException;
import com.quilla.parqueo.model.TipoVehiculo;

// Convierte los textos de entrada a tipos de Java, lanzando E02/E03 si no son válidos
@Component
public class ConversorDatos {

    // "2026-10-12 08:15" o "2026-10-12 08:15:00". STRICT rechaza fechas que no existen (2026-02-30).
    private static final DateTimeFormatter SIN_ZONA_CON_ESPACIO =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm[:ss]").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Fecha y hora a Instant.
     * - Con zona ("...Z", "...-05:00", "...+01:00"): se respeta su zona.
     * - Sin zona ("2026-10-12 08:15", "2026-10-12T08:15:00"): se interpreta como hora de Lima.
     * - Cualquier otro formato, vacío o fecha inexistente: E03.
     */
    public Instant fechaHora(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(CodigoError.E03);
        }
        String texto = valor.trim();

        try {
            return OffsetDateTime.parse(texto, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        } catch (DateTimeParseException ignorada) {
            // no tiene zona: se prueban los formatos sin zona
        }
        try {
            return LocalDateTime.parse(texto, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(HoraLima.ZONA);
        } catch (DateTimeParseException ignorada) {
            // no es "2026-10-12T08:15[:00]"
        }
        try {
            return LocalDateTime.parse(texto, SIN_ZONA_CON_ESPACIO).toInstant(HoraLima.ZONA);
        } catch (DateTimeParseException ignorada) {
            throw new ReglaNegocioException(CodigoError.E03);
        }
    }

    // Fecha calendario "2026-10-12" (cierre de caja). Otro formato o fecha inexistente: E03.
    public LocalDate fecha(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(CodigoError.E03);
        }
        try {
            return LocalDate.parse(valor.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException(CodigoError.E03);
        }
    }

    // AUTO o MOTO, sin distinguir mayúsculas ni espacios alrededor. Otro valor: E02.
    public TipoVehiculo tipo(String valor) {
        if (valor == null) {
            throw new ReglaNegocioException(CodigoError.E02);
        }
        try {
            return TipoVehiculo.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException(CodigoError.E02);
        }
    }
}
