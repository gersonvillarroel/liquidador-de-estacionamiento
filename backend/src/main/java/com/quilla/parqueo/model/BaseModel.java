package com.quilla.parqueo.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Atributos de auditoría comunes a todas las entidades.
 * @MappedSuperclass: no crea tabla propia, sus columnas se agregan a la tabla de cada hija.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseModel {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Lombok genera isDeleted() y setDeleted(boolean)
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "restored_at")
    private Instant restoredAt;
}
