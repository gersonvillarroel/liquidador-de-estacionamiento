package com.quilla.parqueo.service.tarifa;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class FeriadosTest {

    @Test
    void anexoA_fechasDeLaListaSonFeriado() {
        assertTrue(Feriados.esFeriado(LocalDate.of(2026, 1, 1)));
        assertTrue(Feriados.esFeriado(LocalDate.of(2026, 10, 8)));
        assertTrue(Feriados.esFeriado(LocalDate.of(2026, 12, 25)));
    }

    @Test
    void anexoA_soloCuentanLasFechasDeLaLista() {
        assertFalse(Feriados.esFeriado(LocalDate.of(2026, 10, 9)));
        assertFalse(Feriados.esFeriado(LocalDate.of(2026, 6, 7)));    // feriado oficial que no está en la lista
    }

    @Test
    void art5_domingoEsDiaEspecial() {
        assertTrue(Feriados.esDiaEspecial(LocalDate.of(2026, 10, 18)));
    }

    @Test
    void art5_feriadoEsDiaEspecial() {
        assertTrue(Feriados.esDiaEspecial(LocalDate.of(2026, 10, 8)));  // jueves feriado
    }

    @Test
    void art4_lunesASabadoSinFeriadoNoEsEspecial() {
        assertFalse(Feriados.esDiaEspecial(LocalDate.of(2026, 10, 12)));  // lunes
        assertFalse(Feriados.esDiaEspecial(LocalDate.of(2026, 10, 17)));  // sábado
    }
}
