package com.quilla.parqueo.service.tarifa;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Art. 3: parte de la permanencia dentro de una sola franja. Horas en hora de Lima.
public record Tramo(
        LocalDateTime inicio,
        LocalDateTime fin,
        Franja franja) {

    // Art. 3: cada tramo pertenece a la fecha calendario en que empieza
    public LocalDate fecha() {
        return inicio.toLocalDate();
    }

    public long minutos() {
        return Duration.between(inicio, fin).toMinutes();
    }
}
