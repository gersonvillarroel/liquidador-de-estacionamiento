package com.quilla.parqueo.domain;

import java.util.List;

// Lo que devuelve la calculadora
public record ResultadoLiquidacion(long minutos, long totalCentimos, List<LineaDetalle> detalle) {
}
