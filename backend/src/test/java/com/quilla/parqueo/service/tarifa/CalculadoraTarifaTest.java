package com.quilla.parqueo.service.tarifa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.quilla.parqueo.domain.ResultadoLiquidacion;
import com.quilla.parqueo.model.TipoVehiculo;

// Un test (o más) por artículo del reglamento
class CalculadoraTarifaTest {

    private final CalculadoraTarifa calculadora = new CalculadoraTarifa();

    // "2026-10-12T10:00" en hora de Lima → Instant
    private static Instant lima(String fechaHora) {
        return LocalDateTime.parse(fechaHora).toInstant(ZoneOffset.ofHours(-5));
    }

    private ResultadoLiquidacion calcular(TipoVehiculo tipo, String entrada, String salida, boolean perdido) {
        return calculadora.calcular(lima(entrada), lima(salida), tipo, perdido);
    }

    private long total(TipoVehiculo tipo, String entrada, String salida, boolean perdido) {
        return calcular(tipo, entrada, salida, perdido).totalCentimos();
    }

    private long auto(String entrada, String salida) {
        return total(TipoVehiculo.AUTO, entrada, salida, false);
    }

    @Test
    void art1_fechasConOtraZonaSeConviertenALima() {
        // 15:00Z = 10:00 Lima; 16:20Z = 11:20 Lima → igual que E1
        Instant entrada = Instant.parse("2026-10-12T15:00:00Z");
        Instant salida = Instant.parse("2026-10-12T16:20:00Z");
        assertEquals(900, calculadora.calcular(entrada, salida, TipoVehiculo.AUTO, false).totalCentimos());
    }

    @Test
    void art2_seDescartanLosSegundos() {
        // 10:00:59 → 10:16:00: sin segundos son 16 min (fuera de tolerancia)
        Instant entrada = lima("2026-10-12T10:00").plusSeconds(59);
        ResultadoLiquidacion r = calculadora.calcular(entrada, lima("2026-10-12T10:16"), TipoVehiculo.AUTO, false);
        assertEquals(16, r.minutos());
        assertEquals(600, r.totalCentimos());
    }

    @Test
    void art4_primeraHoraYFraccionesDeQuinceMinutos() {
        assertEquals(600, auto("2026-10-12T10:00", "2026-10-12T11:00"));   // 1 hora exacta
        assertEquals(750, auto("2026-10-12T10:00", "2026-10-12T11:01"));   // 1 min extra = 1 fracción
    }

    @Test
    void art5_feriadoTarifaEspecial() {
        // 08/10/2026 (jueves) es feriado del Anexo A
        assertEquals(500, auto("2026-10-08T10:00", "2026-10-08T11:00"));
        assertEquals(600, auto("2026-10-09T10:00", "2026-10-09T11:00"));   // día siguiente: normal
    }

    @Test
    void art6_tramoNocturnoTarifaPlana() {
        assertEquals(1000, auto("2026-10-12T22:30", "2026-10-13T05:30"));
        assertEquals(1000, auto("2026-10-12T23:00", "2026-10-12T23:30"));
    }

    @Test
    void art7_topeDiarioDeTreintaYCincoSoles() {
        // 06:00–22:00 = 600 + 60 fracciones × 150 = 9600 → tope 3500
        assertEquals(3500, auto("2026-10-12T06:00", "2026-10-12T22:00"));
    }

    @Test
    void art8_motoPagaLaMitad() {
        assertEquals(300, total(TipoVehiculo.MOTO, "2026-10-12T10:00", "2026-10-12T11:00", false));
    }

    @Test
    void art8_descuentoDespuesDelTope() {
        // E5 en moto: 7000 tras el tope → 3500
        assertEquals(3500, total(TipoVehiculo.MOTO, "2026-10-12T07:00", "2026-10-13T20:00", false));
    }

    @Test
    void art9_redondeoHaciaAbajo() {
        // 75 min: 600 + 150 = 750 → moto 375 → 370
        ResultadoLiquidacion r = calcular(TipoVehiculo.MOTO, "2026-10-12T10:00", "2026-10-12T11:15", false);
        assertEquals(370, r.totalCentimos());
        assertEquals("Art. 9", r.detalle().get(r.detalle().size() - 1).articulo());
    }

    @Test
    void art10_cargoPorTicketPerdido() {
        assertEquals(3100, total(TipoVehiculo.AUTO, "2026-10-12T10:00", "2026-10-12T11:00", true));
    }

    @Test
    void art10_cargoSinDescuentoNiRedondeo() {
        // moto 75 min: 370 (con 50% y redondeo) + 2500 completos
        assertEquals(2870, total(TipoVehiculo.MOTO, "2026-10-12T10:00", "2026-10-12T11:15", true));
    }

    @Test
    void art11_toleranciaOriginalDeDiezMinutos() {
        // entrada antes del 01/10/2026: rige el Art. 11 original
        assertEquals(0, auto("2026-09-30T10:00", "2026-09-30T10:10"));
        assertEquals(600, auto("2026-09-30T10:00", "2026-09-30T10:11"));
    }

    @Test
    void art11_toleranciaOriginalCobraElTicketPerdido() {
        assertEquals(2500, total(TipoVehiculo.AUTO, "2026-09-30T10:00", "2026-09-30T10:05", true));
    }

    @Test
    void art12_ordenDeAplicacion() {
        // moto, dos días, perdido: tramos → tope → 50% → redondeo → +25.00
        assertEquals(6000, total(TipoVehiculo.MOTO, "2026-10-12T07:00", "2026-10-13T20:00", true));
    }

    @Test
    void adenda1_primeraHoraSoloEnElPrimerTramoDiurno() {
        // 05:00–07:00: nocturno 1000 + diurno 06:00–07:00 con primera hora 600
        assertEquals(1600, auto("2026-10-12T05:00", "2026-10-12T07:00"));
    }

    @Test
    void adenda1_siguientesTramosDiurnosSinPrimeraHora() {
        // 21:00 → 07:00 del día siguiente: 600 + 1000 + 4 fracciones × 150 = 2200
        assertEquals(2200, auto("2026-10-12T21:00", "2026-10-13T07:00"));
    }

    @Test
    void adenda2_toleranciaDeQuinceMinutos() {
        assertEquals(0, auto("2026-10-01T00:00", "2026-10-01T00:15"));
        assertEquals(1000, auto("2026-10-01T00:00", "2026-10-01T00:16"));   // nocturno
        assertEquals(600, auto("2026-10-12T10:00", "2026-10-12T10:16"));    // 16 min ya se cobra
    }

    @Test
    void adenda2_noAplicaATicketsPerdidos() {
        assertEquals(3100, total(TipoVehiculo.AUTO, "2026-10-12T10:00", "2026-10-12T10:05", true));
    }
}
