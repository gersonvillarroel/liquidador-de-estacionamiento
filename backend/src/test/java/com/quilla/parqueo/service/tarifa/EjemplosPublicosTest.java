package com.quilla.parqueo.service.tarifa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.quilla.parqueo.domain.LineaDetalle;
import com.quilla.parqueo.domain.ResultadoLiquidacion;
import com.quilla.parqueo.model.TipoVehiculo;

// Ejemplos públicos E1 a E5 del enunciado
class EjemplosPublicosTest {

    private final CalculadoraTarifa calculadora = new CalculadoraTarifa();

    private static Instant lima(String fechaHora) {
        return LocalDateTime.parse(fechaHora).toInstant(ZoneOffset.ofHours(-5));
    }

    private ResultadoLiquidacion calcular(TipoVehiculo tipo, String entrada, String salida, boolean perdido) {
        return calculadora.calcular(lima(entrada), lima(salida), tipo, perdido);
    }

    @Test
    void e1_autoLunesDiurno() {
        assertEquals(900, calcular(TipoVehiculo.AUTO, "2026-10-12T10:00", "2026-10-12T11:20", false).totalCentimos());
    }

    @Test
    void e2_toleranciaQuinceMinutos() {
        assertEquals(0, calcular(TipoVehiculo.AUTO, "2026-10-12T10:00", "2026-10-12T10:15", false).totalCentimos());
    }

    @Test
    void e3_domingoTarifaEspecial() {
        assertEquals(1000, calcular(TipoVehiculo.AUTO, "2026-10-18T09:00", "2026-10-18T11:01", false).totalCentimos());
    }

    @Test
    void e4_motoConTicketPerdido() {
        assertEquals(3100, calcular(TipoVehiculo.MOTO, "2026-10-13T10:00", "2026-10-13T12:00", true).totalCentimos());
    }

    @Test
    void e5_dosDiasConTopeDiario() {
        ResultadoLiquidacion r = calcular(TipoVehiculo.AUTO, "2026-10-12T07:00", "2026-10-13T20:00", false);

        assertEquals(7000, r.totalCentimos());
        List<LineaDetalle> d = r.detalle();
        assertEquals(5, d.size());
        assertLinea(d.get(0), "Art. 4", 9000);
        assertLinea(d.get(1), "Art. 6", 1000);
        assertLinea(d.get(2), "Art. 4, Adenda 1", 8400);
        assertLinea(d.get(3), "Art. 7", -6500);
        assertLinea(d.get(4), "Art. 7", -4900);
    }

    private void assertLinea(LineaDetalle linea, String articulo, long monto) {
        assertEquals(articulo, linea.articulo());
        assertEquals(monto, linea.montoCentimos());
    }
}
