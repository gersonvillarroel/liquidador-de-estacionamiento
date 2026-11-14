import { Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';

import { ApiError } from '../../../../core/models/api-error.model';
import { ErrorAlert } from '../../../../shared/components/error-alert/error-alert';
import { FilasAceptadas } from '../../components/filas-aceptadas/filas-aceptadas';
import { FilasRechazadas } from '../../components/filas-rechazadas/filas-rechazadas';
import { ImportacionResponse } from '../../models/importacion.model';
import { ImportacionService } from '../../services/importacion.service';

@Component({
  selector: 'app-importacion-page',
  imports: [ErrorAlert, FilasAceptadas, FilasRechazadas],
  templateUrl: './importacion-page.html'
})
export class ImportacionPage {
  private readonly importacionService = inject(ImportacionService);

  protected readonly archivo = signal<File | null>(null);
  protected readonly resultado = signal<ImportacionResponse | null>(null);
  protected readonly error = signal<ApiError | null>(null);
  protected readonly cargando = signal(false);

  protected seleccionarArchivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.archivo.set(input.files?.[0] ?? null);
    this.resultado.set(null);
    this.error.set(null);
  }

  protected importar(): void {
    const archivo = this.archivo();
    if (!archivo) {
      return;
    }

    this.cargando.set(true);
    this.error.set(null);
    this.resultado.set(null);

    this.importacionService
      .importar(archivo)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (respuesta) => this.resultado.set(respuesta),
        error: (err: ApiError) => this.error.set(err),
      });
  }
}
