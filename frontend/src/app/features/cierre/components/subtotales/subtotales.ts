import { Component, input } from '@angular/core';

import { SolesPipe } from '../../../../shared/pipes/soles.pipe';
import { Subtotal, SubtotalTipo } from '../../models/cierre.model';

// Subtotales del cierre: por tipo de vehículo y por tickets perdidos
@Component({
  selector: 'app-subtotales',
  imports: [SolesPipe],
  templateUrl: './subtotales.html'
})
export class Subtotales {
  readonly porTipo = input.required<SubtotalTipo[]>();
  readonly perdidos = input.required<Subtotal>();
}
