package com.quilla.parqueo.exception;

import org.springframework.http.HttpStatus;

// Listado de errores del contrato, cada uno con su mensaje y su estado HTTP
public enum CodigoError {
    E01("La salida es anterior a la entrada", HttpStatus.UNPROCESSABLE_CONTENT),
    E02("El tipo de vehículo no es AUTO ni MOTO", HttpStatus.UNPROCESSABLE_CONTENT),
    E03("Una fecha u hora no tiene un formato aceptado", HttpStatus.UNPROCESSABLE_CONTENT),
    E04("El campo perdido no es SI, NO ni vacío", HttpStatus.UNPROCESSABLE_CONTENT);

    private final String mensaje;
    private final HttpStatus status;

    CodigoError(String mensaje, HttpStatus status) {
        this.mensaje = mensaje;
        this.status = status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
