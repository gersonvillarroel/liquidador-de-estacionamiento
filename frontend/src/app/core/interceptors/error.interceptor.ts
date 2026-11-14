import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { ApiError } from '../models/api-error.model';

// Convierte cualquier error HTTP en un ApiError { codigo, mensaje },
// así las pantallas siempre reciben el mismo formato.
export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(catchError((err: HttpErrorResponse) => throwError(() => aApiError(err))));

function aApiError(err: HttpErrorResponse): ApiError {
  // status 0: el navegador no pudo conectarse (backend apagado, CORS, sin red)
  if (err.status === 0) {
    return {
      codigo: 'SIN_CONEXION',
      mensaje: 'No se pudo conectar con el servidor. Verifique que el backend esté levantado.',
    };
  }
  // Errores del backend: 422 (E01..E05), 400, 500... ya vienen con { codigo, mensaje }
  if (esApiError(err.error)) {
    return { codigo: err.error.codigo, mensaje: err.error.mensaje };
  }
  return { codigo: String(err.status), mensaje: err.message };
}

function esApiError(body: unknown): body is ApiError {
  return (
    typeof body === 'object' &&
    body !== null &&
    typeof (body as ApiError).codigo === 'string' &&
    typeof (body as ApiError).mensaje === 'string'
  );
}
