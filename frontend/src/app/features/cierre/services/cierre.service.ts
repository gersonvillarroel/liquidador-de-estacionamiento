import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { CierreResponse } from '../models/cierre.model';

@Injectable({ providedIn: 'root' })
export class CierreService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/cierre`;

  // GET /api/cierre?fecha=2026-10-12
  obtener(fecha: string): Observable<CierreResponse> {
    return this.http.get<CierreResponse>(this.url, { params: { fecha } });
  }
}
