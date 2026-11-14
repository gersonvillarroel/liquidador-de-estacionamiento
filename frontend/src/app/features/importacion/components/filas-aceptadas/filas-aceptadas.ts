import { Component, input } from '@angular/core';

import { SolesPipe } from '../../../../shared/pipes/soles.pipe';
import { FilaAceptada } from '../../models/importacion.model';

@Component({
  selector: 'app-filas-aceptadas',
  imports: [SolesPipe],
  templateUrl: './filas-aceptadas.html'
})
export class FilasAceptadas {
  readonly filas = input.required<FilaAceptada[]>();
}
