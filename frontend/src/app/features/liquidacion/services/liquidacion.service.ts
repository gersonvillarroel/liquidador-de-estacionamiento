import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { LiquidacionRequest, LiquidacionResponse } from '../models/liquidacion.model';

@Injectable({ providedIn: 'root' })
export class LiquidacionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/liquidaciones`;

  // POST /api/liquidaciones
  liquidar(request: LiquidacionRequest): Observable<LiquidacionResponse> {
    return this.http.post<LiquidacionResponse>(this.url, request);
  }
}
