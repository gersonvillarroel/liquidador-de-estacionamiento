package com.quilla.parqueo.service.tarifa;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

// Anexo A: feriados 2026 (solo cuentan los de la lista)
public final class Feriados {

    private static final Set<LocalDate> FERIADOS_2026 = Set.of(
            LocalDate.of(2026, 1, 1),    // Año Nuevo
            LocalDate.of(2026, 4, 2),    // Jueves Santo
            LocalDate.of(2026, 4, 3),    // Viernes Santo
            LocalDate.of(2026, 5, 1),    // Día del Trabajo
            LocalDate.of(2026, 6, 29),   // San Pedro y San Pablo
            LocalDate.of(2026, 7, 28),   // Fiestas Patrias
            LocalDate.of(2026, 7, 29),   // Fiestas Patrias
            LocalDate.of(2026, 8, 30),   // Santa Rosa de Lima
            LocalDate.of(2026, 10, 8),   // Combate de Angamos
            LocalDate.of(2026, 11, 1),   // Todos los Santos
            LocalDate.of(2026, 12, 8),   // Inmaculada Concepción
            LocalDate.of(2026, 12, 25)); // Navidad

    private Feriados() {
    }

    // Anexo A: true si la fecha está en la lista de feriados 2026
    public static boolean esFeriado(LocalDate fecha) {
        return FERIADOS_2026.contains(fecha);
    }

    // Art. 5: domingo o feriado del Anexo A (tarifa diurna especial)
    public static boolean esDiaEspecial(LocalDate fecha) {
        return fecha.getDayOfWeek() == DayOfWeek.SUNDAY || esFeriado(fecha);
    }
}
