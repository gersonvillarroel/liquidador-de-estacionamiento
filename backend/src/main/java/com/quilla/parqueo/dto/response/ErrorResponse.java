package com.quilla.parqueo.dto.response;

// Cuerpo de error del contrato: { "codigo": "E01", "mensaje": "..." }
public record ErrorResponse(String codigo, String mensaje) {}
