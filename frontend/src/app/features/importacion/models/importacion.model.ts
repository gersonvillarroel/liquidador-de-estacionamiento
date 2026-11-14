// Respuesta de POST /api/importaciones. "fila" es la línea del archivo (la cabecera es la 1).

export interface FilaAceptada {
  fila: number;
  ticket: string;
  total_centimos: number;
}

export interface FilaRechazada {
  fila: number;
  ticket: string;
  codigo: string;
  motivo: string;
}

export interface ImportacionResponse {
  aceptadas: FilaAceptada[];
  rechazadas: FilaRechazada[];
}
