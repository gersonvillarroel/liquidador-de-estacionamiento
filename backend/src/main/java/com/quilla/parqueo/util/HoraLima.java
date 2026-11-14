package com.quilla.parqueo.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class HoraLima {

    public static final ZoneOffset ZONA = ZoneOffset.ofHours(-5);

    private HoraLima() {
    }

    // Fecha calendario de Lima de un instante
    public static LocalDate fecha(Instant instante) {
        return LocalDate.ofInstant(instante, ZONA);
    }
}
