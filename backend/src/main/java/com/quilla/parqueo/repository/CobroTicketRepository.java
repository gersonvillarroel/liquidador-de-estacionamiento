package com.quilla.parqueo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quilla.parqueo.model.CobroTicket;

@Repository
public interface CobroTicketRepository extends JpaRepository<CobroTicket, Long> {

    Optional<CobroTicket> findByTicketIdAndDeletedFalse(Long ticketId);

    List<CobroTicket> findByFechaCierreAndDeletedFalse(LocalDate fechaCierre);
}
