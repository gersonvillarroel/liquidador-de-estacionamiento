package com.quilla.parqueo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.quilla.parqueo.dto.response.FilaAceptadaResponse;
import com.quilla.parqueo.dto.response.FilaRechazadaResponse;
import com.quilla.parqueo.dto.response.ImportacionResponse;
import com.quilla.parqueo.repository.CobroTicketRepository;
import com.quilla.parqueo.repository.DetalleCobroRepository;
import com.quilla.parqueo.repository.TicketRepository;
import com.quilla.parqueo.service.tarifa.CalculadoraTarifa;
import com.quilla.parqueo.util.ConversorDatos;

// Importación del CSV de ejemplo del enunciado. La base de datos se simula con Mockito.
class ImportacionServiceTest {

    private static final String CSV_EJEMPLO = """
            ticket,placa,tipo,entrada,salida,perdido
            T-1001,ABC-123,AUTO,2026-10-12 08:15,2026-10-12 10:40,NO
            T-1002,M1-456,moto ,2026-10-12T13:00:00Z,2026-10-12T15:10:00Z,
            T-1003,XYZ-789,CAMION,2026-10-12 09:00,2026-10-12 10:00,NO
            T-1004,DEF-321,AUTO,12/10/2026 09:00,2026-10-12 10:00,NO
            T-1005,GHI-654,AUTO,2026-10-12 11:00,2026-10-12 10:30,NO
            T-1001,ABC-123,AUTO,2026-10-12 08:15,2026-10-12 11:40,NO
            T-1006,JKL-987,AUTO,2026-10-12 21:50,2026-10-12 22:10,NO
            T-1007,M2-111,MOTO,2026-10-12 07:00,2026-10-12 07:14,SI
            T-1008,MNO-222,AUTO,2026-10-12 18:00,,NO
            """;

    private ImportacionService importacionService;

    @BeforeEach
    void preparar() {
        TicketRepository ticketRepository = mock(TicketRepository.class);
        CobroTicketRepository cobroTicketRepository = mock(CobroTicketRepository.class);
        DetalleCobroRepository detalleCobroRepository = mock(DetalleCobroRepository.class);
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cobroTicketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ConversorDatos conversor = new ConversorDatos();
        LiquidacionService liquidacionService = new LiquidacionService(
                ticketRepository, cobroTicketRepository, detalleCobroRepository, new CalculadoraTarifa(), conversor);
        importacionService = new ImportacionService(liquidacionService, conversor);
    }

    private ImportacionResponse importar(String csv) {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "legado.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        return importacionService.importar(archivo);
    }

    @Test
    void filasAceptadasConSuTotal() {
        Map<String, Long> aceptadas = importar(CSV_EJEMPLO).aceptadas().stream()
                .collect(Collectors.toMap(FilaAceptadaResponse::ticket, FilaAceptadaResponse::totalCentimos));

        assertEquals(Map.of(
                "T-1001", 2100L,
                "T-1002", 670L,
                "T-1006", 1600L,
                "T-1007", 2800L), aceptadas);
    }

    @Test
    void filasRechazadasConSuCodigo() {
        Map<String, String> rechazadas = importar(CSV_EJEMPLO).rechazadas().stream()
                .collect(Collectors.toMap(FilaRechazadaResponse::ticket, FilaRechazadaResponse::codigo));

        assertEquals(Map.of(
                "T-1003", "E02",
                "T-1004", "E03",
                "T-1005", "E01",
                "T-1008", "E05"), rechazadas);
    }

    @Test
    void ticketRepetidoValeLaUltimaFila() {
        ImportacionResponse r = importar(CSV_EJEMPLO);

        FilaAceptadaResponse t1001 = r.aceptadas().stream()
                .filter(fila -> fila.ticket().equals("T-1001")).findFirst().orElseThrow();
        assertEquals(7, t1001.fila());
        // la fila 2 (primer T-1001) no cuenta como rechazada
        assertTrue(r.rechazadas().stream().noneMatch(fila -> fila.ticket().equals("T-1001")));
    }

    @Test
    void e04_campoPerdidoInvalido() {
        String csv = """
                ticket,placa,tipo,entrada,salida,perdido
                T-2001,ABC-123,AUTO,2026-10-12 10:00,2026-10-12 11:00,QUIZAS
                """;
        FilaRechazadaResponse fila = importar(csv).rechazadas().get(0);

        assertEquals(2, fila.fila());
        assertEquals("E04", fila.codigo());
    }

    @Test
    void archivoSoloConCabeceraNoTieneFilas() {
        ImportacionResponse r = importar("ticket,placa,tipo,entrada,salida,perdido\n");

        assertTrue(r.aceptadas().isEmpty());
        assertTrue(r.rechazadas().isEmpty());
    }
}
