import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiError } from '../../../../core/models/api-error.model';
import { ErrorAlert } from '../../../../shared/components/error-alert/error-alert';
import { SolesPipe } from '../../../../shared/pipes/soles.pipe';
import { Subtotales } from '../../components/subtotales/subtotales';
import { CierreResponse } from '../../models/cierre.model';
import { CierreService } from '../../services/cierre.service';

@Component({
  selector: 'app-cierre-page',
  imports: [ReactiveFormsModule, ErrorAlert, SolesPipe, Subtotales],
  templateUrl: './cierre-page.html',
  styleUrl: './cierre-page.css'
})
export class CierrePage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly cierreService = inject(CierreService);

  // <input type="date"> usa el formato "2026-10-12", el mismo que espera el backend
  protected readonly form = this.fb.group({
    fecha: [hoyEnLima(), Validators.required],
  });

  protected readonly resultado = signal<CierreResponse | null>(null);
  protected readonly error = signal<ApiError | null>(null);
  protected readonly cargando = signal(false);

  protected consultar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);
    this.resultado.set(null);

    this.cierreService
      .obtener(this.form.getRawValue().fecha)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (respuesta) => this.resultado.set(respuesta),
        error: (err: ApiError) => this.error.set(err),
      });
  }

  // "2026-10-12" → "12/10/2026" (sin pasar por Date para no depender de la zona del navegador)
  protected fechaLegible(fecha: string): string {
    const [anio, mes, dia] = fecha.split('-');
    return `${dia}/${mes}/${anio}`;
  }
}

// Fecha de hoy en hora de Lima, en formato YYYY-MM-DD ("en-CA" usa ese formato)
function hoyEnLima(): string {
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Lima' }).format(new Date());
}
