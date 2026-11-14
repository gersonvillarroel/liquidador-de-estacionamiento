package com.quilla.parqueo.dto.response;

// Fila del CSV rechazada, con su código (E01..E05) y el motivo
public record FilaRechazadaResponse(
        int fila,
        String ticket,
        String codigo,
        String motivo) {
}
