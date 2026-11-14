package com.quilla.parqueo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quilla.parqueo.dto.request.LiquidacionRequest;
import com.quilla.parqueo.dto.response.LiquidacionResponse;
import com.quilla.parqueo.service.LiquidacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/liquidaciones")
@RequiredArgsConstructor
public class LiquidacionController {

    private final LiquidacionService liquidacionService;

    @PostMapping
    public LiquidacionResponse liquidar(@Valid @RequestBody LiquidacionRequest request) {
        return liquidacionService.liquidar(request);
    }
}
