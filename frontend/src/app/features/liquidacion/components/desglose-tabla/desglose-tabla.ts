import { Component, input } from '@angular/core';

import { SolesPipe } from '../../../../shared/pipes/soles.pipe';
import { LiquidacionResponse } from '../../models/liquidacion.model';

// Tabla del desglose: cada línea con el artículo del reglamento que la justifica
@Component({
  selector: 'app-desglose-tabla',
  imports: [SolesPipe],
  templateUrl: './desglose-tabla.html',
  styleUrl: './desglose-tabla.css'
})
export class DesgloseTabla {
  readonly resultado = input.required<LiquidacionResponse>();
}
