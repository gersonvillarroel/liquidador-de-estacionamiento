package com.quilla.parqueo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "detalle_cobro")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DetalleCobro extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String concepto;

    @Column(nullable = false)
    private String articulo;

    // Puede ser negativo (tope diario, descuento moto, redondeo)
    @Column(nullable = false)
    private Long montoCentimos;

    @Column(nullable = false)
    private Integer orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cobro_ticket_id", nullable = false)
    private CobroTicket cobroTicket;
}
