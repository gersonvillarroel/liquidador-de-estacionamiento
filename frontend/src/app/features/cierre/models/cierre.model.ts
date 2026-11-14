import { TipoVehiculo } from '../../../core/models/tipo-vehiculo.model';

// Respuesta de GET /api/cierre?fecha=YYYY-MM-DD

export interface SubtotalTipo {
  tipo: TipoVehiculo;
  cantidad: number;
  total_centimos: number;
}

export interface Subtotal {
  cantidad: number;
  total_centimos: number;
}

export interface CierreResponse {
  fecha: string;
  cantidad: number;
  total_centimos: number;
  por_tipo: SubtotalTipo[];
  perdidos: Subtotal;
}
