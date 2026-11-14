package com.quilla.parqueo.dto.response;

import java.util.List;

// Respuesta de POST /api/importaciones
public record ImportacionResponse(
        List<FilaAceptadaResponse> aceptadas,
        List<FilaRechazadaResponse> rechazadas) {
}
