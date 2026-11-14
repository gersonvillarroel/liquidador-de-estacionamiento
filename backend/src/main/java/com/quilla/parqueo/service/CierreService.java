package com.quilla.parqueo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quilla.parqueo.dto.response.CierreResponse;
import com.quilla.parqueo.dto.response.SubtotalResponse;
import com.quilla.parqueo.dto.response.SubtotalTipoResponse;
import com.quilla.parqueo.model.CobroTicket;
import com.quilla.parqueo.model.TipoVehiculo;
import com.quilla.parqueo.repository.CobroTicketRepository;
import com.quilla.parqueo.util.ConversorDatos;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CierreService {

    private final CobroTicketRepository cobroTicketRepository;
    private final ConversorDatos conversor;

    @Transactional(readOnly = true)
    public CierreResponse cerrar(String fecha) {
        LocalDate dia = conversor.fecha(fecha);   // E03 si el formato no es YYYY-MM-DD
        List<CobroTicket> cobros = cobroTicketRepository.findByFechaCierreAndDeletedFalse(dia);

        // Total general y subtotal de tickets perdidos
        long totalCentimos = 0;
        long cantidadPerdidos = 0;
        long totalPerdidos = 0;
        for (CobroTicket cobro : cobros) {
            totalCentimos += cobro.getTotalCentimos();
            if (Boolean.TRUE.equals(cobro.getTicket().getTicketPerdido())) {
                cantidadPerdidos++;
                totalPerdidos += cobro.getTotalCentimos();
            }
        }

        // Un subtotal por cada tipo de vehículo, aunque no tenga tickets (cantidad 0)
        List<SubtotalTipoResponse> porTipo = new ArrayList<>();
        for (TipoVehiculo tipo : TipoVehiculo.values()) {
            long cantidad = 0;
            long total = 0;
            for (CobroTicket cobro : cobros) {
                if (cobro.getTicket().getTipoVehiculo() == tipo) {
                    cantidad++;
                    total += cobro.getTotalCentimos();
                }
            }
            porTipo.add(new SubtotalTipoResponse(tipo.name(), cantidad, total));
        }

        return new CierreResponse(
                dia.toString(),
                cobros.size(),
                totalCentimos,
                porTipo,
                new SubtotalResponse(cantidadPerdidos, totalPerdidos));
    }
}
