package com.quilla.parqueo.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LiquidacionResponse(
        String ticket,
        long minutos,
        @JsonProperty("total_centimos") long totalCentimos,
        List<DetalleResponse> detalle
) {}
