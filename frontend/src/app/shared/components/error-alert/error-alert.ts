import { Component, input } from '@angular/core';

import { ApiError } from '../../../core/models/api-error.model';

// Muestra un error del backend con su código, p. ej. "E01 La salida es anterior a la entrada"
@Component({
  selector: 'app-error-alert',
  templateUrl: './error-alert.html',
  styleUrl: './error-alert.css'
})
export class ErrorAlert {
  readonly error = input<ApiError | null>(null);
}
