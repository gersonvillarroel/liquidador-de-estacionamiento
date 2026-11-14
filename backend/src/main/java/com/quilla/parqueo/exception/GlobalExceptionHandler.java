package com.quilla.parqueo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.quilla.parqueo.dto.response.ErrorResponse;

// Atrapa las excepciones de todos los controllers y responde con el formato del contrato.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Errores del contrato (E01..E04): el estado HTTP lo define cada código
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException ex) {
        ErrorResponse body = new ErrorResponse(ex.getCodigo().name(), ex.getMessage());
        return ResponseEntity.status(ex.getCodigo().getStatus()).body(body);
    }

    // 2. Errores propios de Spring (JSON mal formado, falta un parámetro, ruta inexistente,
    //    método no permitido, ...). Se mantiene el estado HTTP estándar que Spring asigna
    //    y solo se cambia el cuerpo al formato { codigo, mensaje }.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {

        String mensaje = (body instanceof ProblemDetail detalle && detalle.getDetail() != null)
                ? detalle.getDetail()
                : ex.getMessage();

        ErrorResponse error = new ErrorResponse(String.valueOf(statusCode.value()), mensaje);
        return ResponseEntity.status(statusCode).headers(headers).body(error);
    }

    // 3. Cualquier otro error no previsto: 500 sin exponer detalles internos al cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarInesperado(Exception ex) {
        log.error("Error inesperado", ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse body = new ErrorResponse(String.valueOf(status.value()), "Error interno del servidor");
        return ResponseEntity.status(status).body(body);
    }
}
