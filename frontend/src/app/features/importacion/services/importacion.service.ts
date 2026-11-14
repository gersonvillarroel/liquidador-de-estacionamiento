import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ImportacionResponse } from '../models/importacion.model';

@Injectable({ providedIn: 'root' })
export class ImportacionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/importaciones`;

  // POST /api/importaciones (multipart/form-data, campo "archivo")
  importar(archivo: File): Observable<ImportacionResponse> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<ImportacionResponse>(this.url, formData);
  }
}
