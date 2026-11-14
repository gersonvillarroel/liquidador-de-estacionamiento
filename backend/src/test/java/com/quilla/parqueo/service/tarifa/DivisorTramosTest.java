package com.quilla.parqueo.service.tarifa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class DivisorTramosTest {

    private static LocalDateTime t(String fechaHora) {
        return LocalDateTime.parse(fechaHora);
    }

    @Test
    void art2_minutosEnterosEntreEntradaYSalida() {
        assertEquals(80, DivisorTramos.minutos(t("2026-10-12T10:00"), t("2026-10-12T11:20")));
    }

    @Test
    void art3_permanenciaDentroDeUnaFranjaEsUnSoloTramo() {
        List<Tramo> tramos = DivisorTramos.dividir(t("2026-10-12T10:00"), t("2026-10-12T11:20"));

        assertEquals(1, tramos.size());
        assertEquals(Franja.DIURNA, tramos.get(0).franja());
        assertEquals(80, tramos.get(0).minutos());
    }

    @Test
    void art3_unTramoPorCadaFranjaQueAtraviesa() {
        List<Tramo> tramos = DivisorTramos.dividir(t("2026-10-12T21:50"), t("2026-10-13T07:00"));

        assertEquals(3, tramos.size());
        assertEquals(Franja.DIURNA, tramos.get(0).franja());
        assertEquals(10, tramos.get(0).minutos());
        assertEquals(Franja.NOCTURNA, tramos.get(1).franja());
        assertEquals(480, tramos.get(1).minutos());
        assertEquals(Franja.DIURNA, tramos.get(2).franja());
        assertEquals(60, tramos.get(2).minutos());
    }

    @Test
    void art3_cadaTramoPerteneceALaFechaEnQueEmpieza() {
        List<Tramo> tramos = DivisorTramos.dividir(t("2026-10-12T21:50"), t("2026-10-13T07:00"));

        assertEquals(LocalDate.of(2026, 10, 12), tramos.get(1).fecha());   // nocturno 22:00 → 06:00
        assertEquals(LocalDate.of(2026, 10, 13), tramos.get(2).fecha());
    }

    @Test
    void art3_madrugadaEsNocturnaHastaLasSeis() {
        List<Tramo> tramos = DivisorTramos.dividir(t("2026-10-12T05:00"), t("2026-10-12T07:00"));

        assertEquals(2, tramos.size());
        assertEquals(Franja.NOCTURNA, tramos.get(0).franja());
        assertEquals(t("2026-10-12T06:00"), tramos.get(0).fin());
        assertEquals(Franja.DIURNA, tramos.get(1).franja());
    }

    @Test
    void art3_sinPermanenciaNoHayTramos() {
        assertTrue(DivisorTramos.dividir(t("2026-10-12T10:00"), t("2026-10-12T10:00")).isEmpty());
    }
}
