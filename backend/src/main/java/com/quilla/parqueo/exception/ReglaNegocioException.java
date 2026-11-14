package com.quilla.parqueo.exception;

import lombok.Getter;

// Se lanza cuando un dato no cumple con el reglamento
@Getter
public class ReglaNegocioException extends RuntimeException {

    private final CodigoError codigo;

    public ReglaNegocioException(CodigoError codigo) {
        super(codigo.getMensaje());
        this.codigo = codigo;
    }
}
