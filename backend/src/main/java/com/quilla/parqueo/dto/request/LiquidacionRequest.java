package com.quilla.parqueo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record LiquidacionRequest(
        @NotBlank String ticket,
        String tipo,
        String entrada,
        String salida,
        @JsonProperty("ticket_perdido") Boolean ticketPerdido) {
}
