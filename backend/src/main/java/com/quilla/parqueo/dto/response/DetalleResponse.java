package com.quilla.parqueo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DetalleResponse(
        String concepto,
        String articulo,
        @JsonProperty("monto_centimos") long montoCentimos) {
}
