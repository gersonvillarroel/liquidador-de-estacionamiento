// Formato de error del backend en todos los endpoints: { "codigo": "E01", "mensaje": "..." }
export interface ApiError {
  codigo: string;
  mensaje: string;
}
