package com.quilla.parqueo.service.tarifa;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.quilla.parqueo.domain.LineaDetalle;
import com.quilla.parqueo.domain.ResultadoLiquidacion;
import com.quilla.parqueo.model.TipoVehiculo;
import com.quilla.parqueo.util.HoraLima;

// Aplica el Reglamento Tarifario en el orden del Art. 12. Todos los montos en céntimos.
@Component
public class CalculadoraTarifa {

    // Art. 4: tarifa diurna normal
    static final long PRIMERA_HORA_NORMAL = 600;
    static final long FRACCION_NORMAL = 150;

    // Art. 5: tarifa diurna especial
    static final long PRIMERA_HORA_ESPECIAL = 500;
    static final long FRACCION_ESPECIAL = 100;

    // Art. 6: tarifa nocturna (por tramo, sin importar su duración)
    static final long TRAMO_NOCTURNO = 1000;

    // Art. 7: tope diario por fecha
    static final long TOPE_DIARIO = 3500;

    // Art. 10: cargo por ticket perdido
    static final long CARGO_TICKET_PERDIDO = 2500;

    // Arts. 4 y 5: duración de la primera hora y de cada fracción
    static final long MINUTOS_PRIMERA_HORA = 60;
    static final long MINUTOS_FRACCION = 15;

    // Art. 11 y Adenda 2: tolerancia
    static final long TOLERANCIA_ORIGINAL = 10;
    static final long TOLERANCIA_ADENDA_2 = 15;
    static final LocalDateTime VIGENCIA_ADENDA_2 = LocalDateTime.of(2026, 10, 1, 0, 0);

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public ResultadoLiquidacion calcular(Instant entrada, Instant salida, TipoVehiculo tipo, boolean ticketPerdido) {
        // Arts. 1 y 2: hora de Lima y sin segundos
        LocalDateTime entradaLima = aHoraLima(entrada);
        LocalDateTime salidaLima = aHoraLima(salida);
        long minutos = DivisorTramos.minutos(entradaLima, salidaLima);

        List<LineaDetalle> detalle = new ArrayList<>();
        long monto = 0;

        // 1. Tolerancia (Art. 11, Adenda 2): si aplica, no se cobran los Arts. 4 a 9
        if (aplicaTolerancia(entradaLima, minutos, ticketPerdido)) {
            detalle.add(lineaTolerancia(entradaLima, minutos));
        } else {
            // 2. Tramos y sus tarifas (Arts. 3 a 6, Adenda 1)
            Map<LocalDate, Long> montoPorFecha = new LinkedHashMap<>();
            boolean primeraHoraCobrada = false;

            for (Tramo tramo : DivisorTramos.dividir(entradaLima, salidaLima)) {
                long montoTramo;
                if (tramo.franja() == Franja.NOCTURNA) {
                    montoTramo = TRAMO_NOCTURNO;
                    detalle.add(new LineaDetalle("Tramo nocturno, " + rango(tramo), "Art. 6", montoTramo));
                } else {
                    boolean conPrimeraHora = !primeraHoraCobrada;   // Adenda 1: solo el primer tramo diurno
                    montoTramo = tarifaDiurna(tramo, conPrimeraHora);
                    detalle.add(lineaDiurna(tramo, conPrimeraHora, montoTramo));
                    primeraHoraCobrada = true;
                }
                montoPorFecha.merge(tramo.fecha(), montoTramo, Long::sum);
                monto += montoTramo;
            }

            // 3. Tope diario por fecha (Art. 7)
            for (Map.Entry<LocalDate, Long> dia : montoPorFecha.entrySet()) {
                long montoDia = dia.getValue();
                if (montoDia > TOPE_DIARIO) {
                    long ajuste = TOPE_DIARIO - montoDia;
                    detalle.add(new LineaDetalle(
                            "Tope diario del " + dia.getKey().format(DIA)
                                    + " (" + soles(montoDia) + " → " + soles(TOPE_DIARIO) + ")",
                            "Art. 7", ajuste));
                    monto += ajuste;
                }
            }

            // 4. Descuento de motocicletas (Art. 8)
            if (tipo == TipoVehiculo.MOTO && monto > 0) {
                long conDescuento = monto / 2;
                detalle.add(new LineaDetalle(
                        "Descuento motocicleta 50% (" + soles(monto) + " → " + soles(conDescuento) + ")",
                        "Art. 8", conDescuento - monto));
                monto = conDescuento;
            }

            // 5. Redondeo hacia abajo al múltiplo de S/ 0.10 (Art. 9)
            long redondeado = monto - (monto % 10);
            if (redondeado != monto) {
                detalle.add(new LineaDetalle(
                        "Redondeo a múltiplo de S/ 0.10 (" + soles(monto) + " → " + soles(redondeado) + ")",
                        "Art. 9", redondeado - monto));
                monto = redondeado;
            }
        }

        // 6. Cargo por ticket perdido (Art. 10): sin descuentos ni redondeo
        if (ticketPerdido) {
            detalle.add(new LineaDetalle("Cargo por ticket perdido", "Art. 10", CARGO_TICKET_PERDIDO));
            monto += CARGO_TICKET_PERDIDO;
        }

        return new ResultadoLiquidacion(minutos, monto, detalle);
    }

    // Arts. 1 y 2
    private LocalDateTime aHoraLima(Instant instante) {
        return LocalDateTime.ofInstant(instante, HoraLima.ZONA).truncatedTo(ChronoUnit.MINUTES);
    }

    // Art. 11: hasta 10 min. Adenda 2 (entradas desde el 01/10/2026): hasta 15 min y no aplica a tickets perdidos
    private boolean aplicaTolerancia(LocalDateTime entradaLima, long minutos, boolean ticketPerdido) {
        if (entradaLima.isBefore(VIGENCIA_ADENDA_2)) {
            return minutos <= TOLERANCIA_ORIGINAL;
        }
        return !ticketPerdido && minutos <= TOLERANCIA_ADENDA_2;
    }

    // Arts. 4 y 5 con Adenda 1: la primera hora solo se cobra en el primer tramo diurno
    private long tarifaDiurna(Tramo tramo, boolean conPrimeraHora) {
        boolean especial = Feriados.esDiaEspecial(tramo.fecha());
        long primeraHora = especial ? PRIMERA_HORA_ESPECIAL : PRIMERA_HORA_NORMAL;
        long fraccion = especial ? FRACCION_ESPECIAL : FRACCION_NORMAL;

        if (conPrimeraHora) {
            long minutosRestantes = Math.max(0, tramo.minutos() - MINUTOS_PRIMERA_HORA);
            return primeraHora + fracciones(minutosRestantes) * fraccion;
        }
        return fracciones(tramo.minutos()) * fraccion;
    }

    // Cantidad de fracciones de 15 minutos (una fracción incompleta cuenta entera)
    private long fracciones(long minutos) {
        return (minutos + MINUTOS_FRACCION - 1) / MINUTOS_FRACCION;
    }

    private LineaDetalle lineaDiurna(Tramo tramo, boolean conPrimeraHora, long monto) {
        boolean especial = Feriados.esDiaEspecial(tramo.fecha());
        String concepto = "Tramo diurno " + (especial ? "especial" : "normal") + ", " + rango(tramo)
                + (conPrimeraHora ? "" : ", sin primera hora");
        String articulo = (especial ? "Art. 5" : "Art. 4") + (conPrimeraHora ? "" : ", Adenda 1");
        return new LineaDetalle(concepto, articulo, monto);
    }

    private LineaDetalle lineaTolerancia(LocalDateTime entradaLima, long minutos) {
        boolean adenda2 = !entradaLima.isBefore(VIGENCIA_ADENDA_2);
        long limite = adenda2 ? TOLERANCIA_ADENDA_2 : TOLERANCIA_ORIGINAL;
        return new LineaDetalle(
                "Tolerancia: " + minutos + " min (hasta " + limite + " min no se cobra)",
                adenda2 ? "Art. 11, Adenda 2" : "Art. 11", 0);
    }

    // "12/10 07:00–22:00" o, si cruza la medianoche, "12/10 22:00 – 13/10 06:00"
    private String rango(Tramo tramo) {
        if (tramo.inicio().toLocalDate().equals(tramo.fin().toLocalDate())) {
            return tramo.inicio().format(DIA) + " " + tramo.inicio().format(HORA) + "–" + tramo.fin().format(HORA);
        }
        return tramo.inicio().format(DIA) + " " + tramo.inicio().format(HORA)
                + " – " + tramo.fin().format(DIA) + " " + tramo.fin().format(HORA);
    }

    // 9000 → "S/ 90.00"
    private String soles(long centimos) {
        return String.format(Locale.ROOT, "S/ %d.%02d", centimos / 100, centimos % 100);
    }
}
