import { Pipe, PipeTransform } from '@angular/core';

// Céntimos → soles:  900 → "S/ 9.00"   -6500 → "−S/ 65.00"
@Pipe({ name: 'soles' })
export class SolesPipe implements PipeTransform {
  transform(centimos: number | null | undefined): string {
    if (centimos === null || centimos === undefined) {
      return '';
    }
    const signo = centimos < 0 ? '−' : '';
    const absoluto = Math.abs(centimos);
    const soles = Math.floor(absoluto / 100);
    const resto = String(absoluto % 100).padStart(2, '0');
    return `${signo}S/ ${soles}.${resto}`;
  }
}
