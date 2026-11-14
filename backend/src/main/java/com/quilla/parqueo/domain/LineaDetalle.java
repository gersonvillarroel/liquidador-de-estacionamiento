package com.quilla.parqueo.domain;

// Un paso del reglamento que cambió el monto. montoCentimos puede ser negativo (tope, descuento, redondeo).
public record LineaDetalle(String concepto, String articulo, long montoCentimos) {
}
