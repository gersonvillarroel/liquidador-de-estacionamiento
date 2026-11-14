package com.quilla.parqueo.service.tarifa;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Arts. 2 y 3: permanencia en minutos y división en tramos por franja.
// Recibe horas de Lima ya sin segundos.
public final class DivisorTramos {

    // Art. 3: la franja diurna va de 06:00 a 22:00; el resto es nocturna
    static final LocalTime INICIO_DIURNO = LocalTime.of(6, 0);
    static final LocalTime INICIO_NOCTURNO = LocalTime.of(22, 0);

    private DivisorTramos() {
    }

    // Art. 2: minutos enteros entre la entrada y la salida
    public static long minutos(LocalDateTime entrada, LocalDateTime salida) {
        return Duration.between(entrada, salida).toMinutes();
    }

    // Art. 3: un tramo por cada franja que atraviesa la permanencia, en orden
    public static List<Tramo> dividir(LocalDateTime entrada, LocalDateTime salida) {
        List<Tramo> tramos = new ArrayList<>();
        LocalDateTime inicio = entrada;

        while (inicio.isBefore(salida)) {
            Franja franja = franjaDe(inicio);
            LocalDateTime finDeFranja = finDeFranja(inicio, franja);
            LocalDateTime fin = finDeFranja.isBefore(salida) ? finDeFranja : salida;

            tramos.add(new Tramo(inicio, fin, franja));
            inicio = fin;
        }
        return tramos;
    }

    // Diurna si la hora está en [06:00, 22:00); nocturna en cualquier otro caso
    private static Franja franjaDe(LocalDateTime momento) {
        LocalTime hora = momento.toLocalTime();
        boolean diurna = !hora.isBefore(INICIO_DIURNO) && hora.isBefore(INICIO_NOCTURNO);
        return diurna ? Franja.DIURNA : Franja.NOCTURNA;
    }

    // Momento en que termina la franja que contiene a "momento"
    private static LocalDateTime finDeFranja(LocalDateTime momento, Franja franja) {
        if (franja == Franja.DIURNA) {
            return momento.toLocalDate().atTime(INICIO_NOCTURNO);              // hoy 22:00
        }
        if (momento.toLocalTime().isBefore(INICIO_DIURNO)) {
            return momento.toLocalDate().atTime(INICIO_DIURNO);                // hoy 06:00 (madrugada)
        }
        return momento.toLocalDate().plusDays(1).atTime(INICIO_DIURNO);        // mañana 06:00
    }
}
