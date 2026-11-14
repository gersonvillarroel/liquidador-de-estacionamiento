package com.quilla.parqueo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubtotalResponse(
        long cantidad,
        @JsonProperty("total_centimos") long totalCentimos) {
}
