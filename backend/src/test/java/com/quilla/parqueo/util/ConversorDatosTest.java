package com.quilla.parqueo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.quilla.parqueo.exception.CodigoError;
import com.quilla.parqueo.exception.ReglaNegocioException;
import com.quilla.parqueo.model.TipoVehiculo;

class ConversorDatosTest {

    private final ConversorDatos conversor = new ConversorDatos();

    // 2026-10-12 10:00 en Lima = 15:00 UTC
    private static final Instant DIEZ_LIMA = Instant.parse("2026-10-12T15:00:00Z");

    private void assertError(CodigoError esperado, Executable accion) {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, accion);
        assertEquals(esperado, ex.getCodigo());
    }

    @Test
    void fechaConZonaSeRespeta() {
        assertEquals(DIEZ_LIMA, conversor.fechaHora("2026-10-12T10:00:00-05:00"));
        assertEquals(DIEZ_LIMA, conversor.fechaHora("2026-10-12T15:00:00Z"));
        assertEquals(DIEZ_LIMA, conversor.fechaHora("2026-10-12T16:00:00+01:00"));
    }

    @Test
    void fechaSinZonaSeInterpretaEnHoraDeLima() {
        assertEquals(DIEZ_LIMA, conversor.fechaHora("2026-10-12 10:00"));
        assertEquals(DIEZ_LIMA, conversor.fechaHora("2026-10-12T10:00:00"));
        assertEquals(DIEZ_LIMA, conversor.fechaHora(" 2026-10-12 10:00 "));
    }

    @Test
    void e03_formatoDeFechaNoAceptado() {
        assertError(CodigoError.E03, () -> conversor.fechaHora("12/10/2026 09:00"));
        assertError(CodigoError.E03, () -> conversor.fechaHora("hoy"));
    }

    @Test
    void e03_fechaQueNoExiste() {
        assertError(CodigoError.E03, () -> conversor.fechaHora("2026-02-30 10:00"));
        assertError(CodigoError.E03, () -> conversor.fecha("2026-02-30"));
    }

    @Test
    void e05_fechaVaciaOAusente() {
        assertError(CodigoError.E05, () -> conversor.fechaHora(""));
        assertError(CodigoError.E05, () -> conversor.fechaHora("   "));
        assertError(CodigoError.E05, () -> conversor.fechaHora(null));
        assertError(CodigoError.E05, () -> conversor.fecha(""));
    }

    @Test
    void fechaDeCierre() {
        assertEquals(LocalDate.of(2026, 10, 12), conversor.fecha("2026-10-12"));
        assertError(CodigoError.E03, () -> conversor.fecha("12/10/2026"));
    }

    @Test
    void tipoSinDistinguirMayusculasNiEspacios() {
        assertEquals(TipoVehiculo.AUTO, conversor.tipo("AUTO"));
        assertEquals(TipoVehiculo.MOTO, conversor.tipo("moto "));
        assertEquals(TipoVehiculo.MOTO, conversor.tipo(" Moto"));
    }

    @Test
    void e02_tipoNoEsAutoNiMoto() {
        assertError(CodigoError.E02, () -> conversor.tipo("CAMION"));
        assertError(CodigoError.E02, () -> conversor.tipo(""));
        assertError(CodigoError.E02, () -> conversor.tipo(null));
    }

    @Test
    void perdidoSiNoOVacio() {
        assertTrue(conversor.perdido("SI"));
        assertTrue(conversor.perdido("si"));
        assertFalse(conversor.perdido("NO"));
        assertFalse(conversor.perdido(" no "));
        assertFalse(conversor.perdido(""));
    }

    @Test
    void e04_perdidoConOtroValor() {
        assertError(CodigoError.E04, () -> conversor.perdido("TAL VEZ"));
        assertError(CodigoError.E04, () -> conversor.perdido("1"));
    }
}
