import { Component, input } from '@angular/core';

import { FilaRechazada } from '../../models/importacion.model';

@Component({
  selector: 'app-filas-rechazadas',
  templateUrl: './filas-rechazadas.html'
})
export class FilasRechazadas {
  readonly filas = input.required<FilaRechazada[]>();
}
