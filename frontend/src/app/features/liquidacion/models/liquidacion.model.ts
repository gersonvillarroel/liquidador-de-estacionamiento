import { TipoVehiculo } from '../../../core/models/tipo-vehiculo.model';

// Los nombres de los campos son los del contrato del backend (snake_case)

export interface LiquidacionRequest {
  ticket: string;
  tipo: TipoVehiculo;
  entrada: string;
  salida: string;
  ticket_perdido: boolean;
}

export interface LineaDetalle {
  concepto: string;
  articulo: string;
  monto_centimos: number;
}

export interface LiquidacionResponse {
  ticket: string;
  minutos: number;
  total_centimos: number;
  detalle: LineaDetalle[];
}
