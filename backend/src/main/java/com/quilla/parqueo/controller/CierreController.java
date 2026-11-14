package com.quilla.parqueo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quilla.parqueo.dto.response.CierreResponse;
import com.quilla.parqueo.service.CierreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cierre")
@RequiredArgsConstructor
public class CierreController {

    private final CierreService cierreService;

    @GetMapping
    public CierreResponse cerrar(@RequestParam String fecha) {
        return cierreService.cerrar(fecha);
    }
}
