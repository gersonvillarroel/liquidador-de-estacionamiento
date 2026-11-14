package com.quilla.parqueo.domain;

import java.time.Instant;

import com.quilla.parqueo.model.TipoVehiculo;

// Ticket ya convertido y validado, listo para liquidar y guardar
public record DatosTicket(
        String codigo,
        String placa,
        TipoVehiculo tipo,
        Instant entrada,
        Instant salida,
        boolean ticketPerdido) {
}
