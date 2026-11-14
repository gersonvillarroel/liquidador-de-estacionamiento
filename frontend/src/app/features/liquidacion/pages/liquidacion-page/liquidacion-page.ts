import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { ApiError } from '../../../../core/models/api-error.model';
import { TIPOS_VEHICULO, TipoVehiculo } from '../../../../core/models/tipo-vehiculo.model';
import { ErrorAlert } from '../../../../shared/components/error-alert/error-alert';
import { DesgloseTabla } from '../../components/desglose-tabla/desglose-tabla';
import { LiquidacionResponse } from '../../models/liquidacion.model';
import { LiquidacionService } from '../../services/liquidacion.service';

@Component({
  selector: 'app-liquidacion-page',
  imports: [ReactiveFormsModule, DesgloseTabla, ErrorAlert],
  templateUrl: './liquidacion-page.html',
  styleUrl: './liquidacion-page.css'
})
export class LiquidacionPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly liquidacionService = inject(LiquidacionService);

  protected readonly tipos = TIPOS_VEHICULO;

  // entrada y salida usan <input type="datetime-local">: "2026-10-12T10:00" (sin zona).
  // El backend interpreta las fechas sin zona como hora de Lima.
  protected readonly form = this.fb.group({
    ticket: ['', Validators.required],
    tipo: this.fb.control<TipoVehiculo>('AUTO'),
    entrada: ['', Validators.required],
    salida: ['', Validators.required],
    ticketPerdido: [false],
  });

  protected readonly resultado = signal<LiquidacionResponse | null>(null);
  protected readonly error = signal<ApiError | null>(null);
  protected readonly cargando = signal(false);

  protected liquidar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valores = this.form.getRawValue();

    this.cargando.set(true);
    this.error.set(null);
    this.resultado.set(null);

    this.liquidacionService
      .liquidar({
        ticket: valores.ticket.trim(),
        tipo: valores.tipo,
        entrada: valores.entrada,
        salida: valores.salida,
        ticket_perdido: valores.ticketPerdido,
      })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (respuesta) => this.resultado.set(respuesta),
        error: (err: ApiError) => this.error.set(err),
      });
  }

  protected campoInvalido(nombre: 'ticket' | 'entrada' | 'salida'): boolean {
    const control = this.form.controls[nombre];
    return control.invalid && control.touched;
  }
}
