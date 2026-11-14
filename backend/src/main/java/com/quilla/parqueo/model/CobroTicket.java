package com.quilla.parqueo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "cobro_ticket")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CobroTicket extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long minutos;

    @Column(nullable = false)
    private Long totalCentimos;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    // Dueño del 1 a 1: unique = true garantiza un solo cobro por ticket
    @OneToOne(optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    // 1 a muchos: un cobro tiene varias líneas de detalle
    @OneToMany(mappedBy = "cobroTicket")
    @OrderBy("orden ASC")
    private List<DetalleCobro> detalles = new ArrayList<>();
}
