import { Routes } from '@angular/router';

// Cada feature se carga solo cuando se visita su ruta (lazy loading)
export const routes: Routes = [
  { path: '', redirectTo: 'liquidacion', pathMatch: 'full' },
  {
    path: 'liquidacion',
    loadChildren: () =>
      import('./features/liquidacion/liquidacion.routes').then((m) => m.LIQUIDACION_ROUTES),
  },
  {
    path: 'importacion',
    loadChildren: () =>
      import('./features/importacion/importacion.routes').then((m) => m.IMPORTACION_ROUTES),
  },
  {
    path: 'cierre',
    loadChildren: () => import('./features/cierre/cierre.routes').then((m) => m.CIERRE_ROUTES),
  },
  { path: '**', redirectTo: 'liquidacion' },
];
