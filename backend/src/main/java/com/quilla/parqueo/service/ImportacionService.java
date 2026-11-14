package com.quilla.parqueo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.quilla.parqueo.domain.DatosTicket;
import com.quilla.parqueo.dto.response.FilaAceptadaResponse;
import com.quilla.parqueo.dto.response.FilaRechazadaResponse;
import com.quilla.parqueo.dto.response.ImportacionResponse;
import com.quilla.parqueo.dto.response.LiquidacionResponse;
import com.quilla.parqueo.exception.ReglaNegocioException;
import com.quilla.parqueo.model.TipoVehiculo;
import com.quilla.parqueo.util.ConversorDatos;

import lombok.RequiredArgsConstructor;

// POST /api/importaciones: lee el CSV legado y liquida cada fila válida con el mismo reglamento que el API.
// No es @Transactional a propósito: cada fila se guarda en su propia transacción (LiquidacionService.registrar),
// así una fila rechazada no deshace las aceptadas.
@Service
@RequiredArgsConstructor
public class ImportacionService {

    // Columnas del CSV: ticket,placa,tipo,entrada,salida,perdido
    private static final int COLUMNAS = 6;
    private static final int TICKET = 0;
    private static final int PLACA = 1;
    private static final int TIPO = 2;
    private static final int ENTRADA = 3;
    private static final int SALIDA = 4;
    private static final int PERDIDO = 5;

    private final LiquidacionService liquidacionService;
    private final ConversorDatos conversor;

    public ImportacionResponse importar(MultipartFile archivo) {
        List<FilaCsv> filas = leerFilas(archivo);

        // Ticket repetido: vale la última fila del archivo; las anteriores se ignoran (no cuentan como rechazadas)
        Map<String, Integer> ultimaFilaPorTicket = new HashMap<>();
        for (FilaCsv fila : filas) {
            ultimaFilaPorTicket.put(fila.ticket(), fila.numero());
        }

        List<FilaAceptadaResponse> aceptadas = new ArrayList<>();
        List<FilaRechazadaResponse> rechazadas = new ArrayList<>();

        for (FilaCsv fila : filas) {
            if (fila.numero() != ultimaFilaPorTicket.get(fila.ticket())) {
                continue;
            }
            try {
                LiquidacionResponse liquidacion = liquidacionService.registrar(convertir(fila));
                aceptadas.add(new FilaAceptadaResponse(fila.numero(), fila.ticket(), liquidacion.totalCentimos()));
            } catch (ReglaNegocioException ex) {
                rechazadas.add(new FilaRechazadaResponse(
                        fila.numero(), fila.ticket(), ex.getCodigo().name(), ex.getMessage()));
            }
        }
        return new ImportacionResponse(aceptadas, rechazadas);
    }

    // Orden de validación: tipo (E02) → entrada y salida (E05/E03) → perdido (E04) → salida < entrada (E01, en registrar)
    private DatosTicket convertir(FilaCsv fila) {
        String[] campos = fila.campos();
        TipoVehiculo tipo = conversor.tipo(campos[TIPO]);
        Instant entrada = conversor.fechaHora(campos[ENTRADA]);
        Instant salida = conversor.fechaHora(campos[SALIDA]);
        boolean perdido = conversor.perdido(campos[PERDIDO]);
        String placa = campos[PLACA].isBlank() ? null : campos[PLACA].trim();
        return new DatosTicket(fila.ticket(), placa, tipo, entrada, salida, perdido);
    }

    // Lee el archivo en UTF-8. La línea 1 es la cabecera; las líneas vacías se ignoran.
    private List<FilaCsv> leerFilas(MultipartFile archivo) {
        List<FilaCsv> filas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {

            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                if (numero == 1 || linea.isBlank()) {
                    continue;
                }
                String[] campos = separarCampos(linea);
                filas.add(new FilaCsv(numero, campos[TICKET].trim(), campos));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo", e);
        }
        return filas;
    }

    // Separa por comas y completa con "" si la fila trae menos columnas
    private String[] separarCampos(String linea) {
        String[] partes = linea.split(",", -1);
        String[] campos = new String[COLUMNAS];
        for (int i = 0; i < COLUMNAS; i++) {
            campos[i] = i < partes.length ? partes[i] : "";
        }
        return campos;
    }

    // numero = número de línea en el archivo (la cabecera es la línea 1)
    private record FilaCsv(int numero, String ticket, String[] campos) {
    }
}
