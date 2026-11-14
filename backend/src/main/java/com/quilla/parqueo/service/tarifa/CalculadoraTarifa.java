package com.quilla.parqueo.service.tarifa;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.quilla.parqueo.domain.ResultadoLiquidacion;
import com.quilla.parqueo.model.TipoVehiculo;

// Aplica el Reglamento Tarifario
@Component
public class CalculadoraTarifa {

    public ResultadoLiquidacion calcular(Instant entrada, Instant salida, TipoVehiculo tipo, boolean ticketPerdido) {
        // TODO: implementar el reglamento (Arts. 1 a 12, Adendas 1 y 2)
        throw new UnsupportedOperationException("Lógica de tarifas pendiente");
    }
}
