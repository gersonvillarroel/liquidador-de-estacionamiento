package com.quilla.parqueo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.quilla.parqueo.dto.response.ImportacionResponse;
import com.quilla.parqueo.service.ImportacionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/importaciones")
@RequiredArgsConstructor
public class ImportacionController {

    private final ImportacionService importacionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportacionResponse importar(@RequestParam("archivo") MultipartFile archivo) {
        return importacionService.importar(archivo);
    }
}
