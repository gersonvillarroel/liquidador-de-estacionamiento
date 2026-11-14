package com.quilla.parqueo.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CierreResponse(
        String fecha,
        long cantidad,
        @JsonProperty("total_centimos") long totalCentimos,
        @JsonProperty("por_tipo") List<SubtotalTipoResponse> porTipo,
        SubtotalResponse perdidos
) {}
