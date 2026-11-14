package com.quilla.parqueo.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quilla.parqueo.domain.DatosTicket;
import com.quilla.parqueo.domain.LineaDetalle;
import com.quilla.parqueo.domain.ResultadoLiquidacion;
import com.quilla.parqueo.dto.request.LiquidacionRequest;
import com.quilla.parqueo.dto.response.DetalleResponse;
import com.quilla.parqueo.dto.response.LiquidacionResponse;
import com.quilla.parqueo.exception.CodigoError;
import com.quilla.parqueo.exception.ReglaNegocioException;
import com.quilla.parqueo.model.CobroTicket;
import com.quilla.parqueo.model.DetalleCobro;
import com.quilla.parqueo.model.Ticket;
import com.quilla.parqueo.model.TipoVehiculo;
import com.quilla.parqueo.repository.CobroTicketRepository;
import com.quilla.parqueo.repository.DetalleCobroRepository;
import com.quilla.parqueo.repository.TicketRepository;
import com.quilla.parqueo.service.tarifa.CalculadoraTarifa;
import com.quilla.parqueo.util.ConversorDatos;
import com.quilla.parqueo.util.HoraLima;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LiquidacionService {

    private final TicketRepository ticketRepository;
    private final CobroTicketRepository cobroTicketRepository;
    private final DetalleCobroRepository detalleCobroRepository;
    private final CalculadoraTarifa calculadoraTarifa;
    private final ConversorDatos conversor;

    // POST /api/liquidaciones: convierte el request, liquida y guarda
    @Transactional
    public LiquidacionResponse liquidar(LiquidacionRequest request) {
        TipoVehiculo tipo = conversor.tipo(request.tipo());                 // E02
        Instant entrada = conversor.fechaHora(request.entrada());           // E03
        Instant salida = conversor.fechaHora(request.salida());             // E03
        boolean perdido = Boolean.TRUE.equals(request.ticketPerdido());     // null = false

        DatosTicket datos = new DatosTicket(request.ticket().trim(), null, tipo, entrada, salida, perdido);
        return registrar(datos);
    }

    // Liquida y guarda un ticket ya convertido (público para reutilizarlo después en la importación)
    @Transactional
    public LiquidacionResponse registrar(DatosTicket datos) {
        if (datos.salida().isBefore(datos.entrada())) {
            throw new ReglaNegocioException(CodigoError.E01);
        }

        ResultadoLiquidacion resultado = calculadoraTarifa.calcular(
                datos.entrada(), datos.salida(), datos.tipo(), datos.ticketPerdido());

        Ticket ticket = guardarTicket(datos);
        CobroTicket cobro = guardarCobro(ticket, resultado);
        guardarDetalles(cobro, resultado.detalle());

        return aResponse(ticket.getCodigo(), resultado);
    }

    // Si ya existe un ticket activo con ese código, se actualiza; si no, se crea
    private Ticket guardarTicket(DatosTicket datos) {
        Ticket ticket = ticketRepository.findByCodigoAndDeletedFalse(datos.codigo())
                .orElseGet(Ticket::new);

        ticket.setCodigo(datos.codigo());
        ticket.setPlaca(datos.placa());
        ticket.setTipoVehiculo(datos.tipo());
        ticket.setEntrada(datos.entrada());
        ticket.setSalida(datos.salida());
        ticket.setTicketPerdido(datos.ticketPerdido());
        return ticketRepository.save(ticket);
    }

    // Un ticket tiene un solo cobro: si ya existía se actualiza y su desglose anterior se elimina lógicamente
    private CobroTicket guardarCobro(Ticket ticket, ResultadoLiquidacion resultado) {
        CobroTicket cobro = cobroTicketRepository.findByTicketIdAndDeletedFalse(ticket.getId())
                .orElseGet(CobroTicket::new);

        if (cobro.getId() != null) {
            List<DetalleCobro> anteriores =
                    detalleCobroRepository.findByCobroTicketIdAndDeletedFalseOrderByOrdenAsc(cobro.getId());
            anteriores.forEach(detalle -> detalle.setDeleted(true));
            detalleCobroRepository.saveAll(anteriores);
        }

        cobro.setTicket(ticket);
        cobro.setMinutos(resultado.minutos());
        cobro.setTotalCentimos(resultado.totalCentimos());
        cobro.setFechaCierre(HoraLima.fecha(ticket.getSalida()));   // cierre = fecha de salida en Lima
        return cobroTicketRepository.save(cobro);
    }

    private void guardarDetalles(CobroTicket cobro, List<LineaDetalle> lineas) {
        List<DetalleCobro> detalles = new ArrayList<>();
        int orden = 1;
        for (LineaDetalle linea : lineas) {
            DetalleCobro detalle = new DetalleCobro();
            detalle.setCobroTicket(cobro);
            detalle.setConcepto(linea.concepto());
            detalle.setArticulo(linea.articulo());
            detalle.setMontoCentimos(linea.montoCentimos());
            detalle.setOrden(orden++);
            detalles.add(detalle);
        }
        detalleCobroRepository.saveAll(detalles);
    }

    private LiquidacionResponse aResponse(String codigo, ResultadoLiquidacion resultado) {
        List<DetalleResponse> detalle = resultado.detalle().stream()
                .map(linea -> new DetalleResponse(linea.concepto(), linea.articulo(), linea.montoCentimos()))
                .toList();
        return new LiquidacionResponse(codigo, resultado.minutos(), resultado.totalCentimos(), detalle);
    }
}
