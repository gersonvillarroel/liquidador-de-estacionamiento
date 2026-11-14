package com.quilla.parqueo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quilla.parqueo.model.DetalleCobro;

@Repository
public interface DetalleCobroRepository extends JpaRepository<DetalleCobro, Long> {

    List<DetalleCobro> findByCobroTicketIdAndDeletedFalseOrderByOrdenAsc(Long cobroTicketId);
}
