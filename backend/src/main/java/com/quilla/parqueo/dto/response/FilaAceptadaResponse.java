package com.quilla.parqueo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

// Fila del CSV que se liquidó. "fila" es el número de línea del archivo (la cabecera es la 1).
public record FilaAceptadaResponse(
        int fila,
        String ticket,
        @JsonProperty("total_centimos") long totalCentimos) {
}
