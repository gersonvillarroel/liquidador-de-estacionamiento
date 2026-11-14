# Decisiones

Puntos del reglamento y del contrato que me parecieron ambiguos, cómo los interpreté y por qué. Al final, las decisiones técnicas.

---

## 1. Reglamento tarifario

### 1.1 Fracciones de 15 minutos (Arts. 4 y 5)
**Interpretación:** una fracción incompleta se cobra entera. Si la permanencia dura exactamente 60 minutos, solo se cobra la primera hora; con 61 minutos se agrega una fracción.
**Por qué:** el reglamento dice "primera hora **o fracción**" y "cada fracción de 15 minutos", lo que indica que no se prorratea. E1 (80 min → S/ 9.00 = 6.00 + 2 × 1.50) lo confirma.

### 1.2 Primera hora cuando la permanencia empieza de noche (Adenda 1)
**Interpretación:** la primera hora se cobra en el **primer tramo diurno**, aunque no sea el primer tramo de la permanencia. Por ejemplo, 05:00–07:00 = nocturno S/ 10.00 + diurno 06:00–07:00 con primera hora S/ 6.00. La tarifa de esa primera hora (normal o especial) es la del día de ese tramo.
**Por qué:** la Adenda 1 dice literalmente "en el primer tramo diurno", no "en el primer tramo".

### 1.3 Tramo nocturno que empieza después de medianoche (Art. 3)
**Interpretación:** si la entrada es, por ejemplo, a las 00:30 del 13/10, el tramo nocturno 00:30–06:00 pertenece al **13/10**, y no a la noche que empezó el 12/10.
**Por qué:** el Art. 3 dice que cada tramo pertenece a la fecha calendario **en que empieza**, y ese tramo empieza el 13/10. Esto afecta al tope diario (Art. 7).

### 1.4 Vigencia de la Adenda 2
**Interpretación:** se decide por la **fecha de entrada** en hora de Lima. Una entrada del 30/09/2026 usa la tolerancia original (10 min) aunque la salida sea del 01/10/2026.
**Por qué:** la adenda dice "vigente para **entradas** desde el 01/10/2026 a las 00:00".

### 1.5 Tolerancia y ticket perdido
- **Entradas antes del 01/10/2026:** si la permanencia es de 10 minutos o menos, solo se cobra el cargo del Art. 10 (S/ 25.00).
- **Entradas desde el 01/10/2026:** la tolerancia no aplica a los tickets perdidos, así que se cobra la tarifa completa más S/ 25.00 (ejemplo del CSV: T-1007 → S/ 28.00).

### 1.6 Línea de tolerancia en el desglose
**Interpretación:** cuando aplica la tolerancia se agrega una línea con monto 0, por ejemplo "Tolerancia: 15 min (hasta 15 min no se cobra)", con el artículo "Art. 11, Adenda 2".
**Por qué:** el contrato pide una línea por cada paso que cambió el monto. La tolerancia es el paso que hace que no se cobren los Arts. 4 a 9, y sin esa línea el operador vería un total de S/ 0.00 sin explicación.

### 1.7 Descuento de moto con céntimos impares (Art. 8)
**Interpretación:** el 50% se redondea hacia abajo al céntimo antes del Art. 9. En la práctica no ocurre, porque todos los montos posibles son múltiplos de S/ 0.50.

### 1.8 Permanencia de 0 minutos
**Interpretación:** si la entrada y la salida coinciden (sin segundos), es válido: 0 minutos, que entra en la tolerancia. No es E01, porque la salida no es anterior a la entrada.

### 1.9 E01 y los segundos
**Interpretación:** E01 compara las marcas de hora completas, con segundos. Por ejemplo, 10:00:30 → 10:00:10 es E01, aunque al descartar los segundos (Art. 2) ambas queden en 10:00.
**Por qué:** E01 es una validación del dato recibido ("la salida es anterior a la entrada") y se hace antes de aplicar el reglamento.

### 1.10 Feriados de otros años
**Interpretación:** solo cuentan las fechas del Anexo A (2026). Para otros años no hay feriados, pero los domingos siguen con tarifa especial.
**Por qué:** el Anexo A dice "solo cuentan estas fechas, aunque exista otro feriado oficial".

---

## 2. Importación del CSV

### 2.1 T-1008 (salida vacía)
**Decisión:** se rechaza con un código nuevo, **E05 "Falta una fecha u hora obligatoria"**.
**Por qué:**
- Sin salida no existe permanencia que liquidar (Art. 2) ni fecha de cierre, porque el cierre se asigna por la fecha de salida. El vehículo probablemente sigue en el estacionamiento.
- No usé E03 porque describe una fecha con **formato** no aceptado, no la **ausencia** del dato. Un código propio le dice al operador exactamente qué corregir.
- E05 se aplica en todos los endpoints: entrada o salida ausentes en `POST /api/liquidaciones`, o `fecha` vacía en el cierre.

### 2.2 Ticket repetido cuya última fila es inválida
**Interpretación:** si la última fila de un ticket repetido es inválida, el ticket se **rechaza**, aunque una fila anterior fuera válida.
**Por qué:** el formato dice "vale la última fila del archivo con ese ticket". Las filas anteriores se ignoran: no se aceptan ni se cuentan como rechazadas.

### 2.3 Número de fila
**Interpretación:** es el número de línea del archivo, contando la cabecera como línea 1.
**Por qué:** así coincide con el enunciado ("T-1001: vale la fila 7").

### 2.4 Orden de validación de una fila
**Interpretación:** tipo (E02) → entrada (E05/E03) → salida (E05/E03) → perdido (E04) → salida anterior a la entrada (E01). Si una fila tiene varios errores, se informa el primero.

### 2.5 Formatos de fecha aceptados
- **Con zona:** `2026-10-12T13:00:00Z`, `2026-10-12T08:00:00-05:00`, `2026-10-12T14:00+01:00`. Se convierten a hora de Lima.
- **Sin zona:** `2026-10-12 08:15`, `2026-10-12 08:15:00`, `2026-10-12T08:15`, `2026-10-12T08:15:00`. Se interpretan como hora de Lima.
- **Validación estricta:** una fecha que no existe (`2026-02-30`) se rechaza con E03.
- **Mismos formatos en el API:** el API acepta los mismos formatos que el CSV, para que ambas rutas apliquen exactamente las mismas reglas.

### 2.6 Estructura del archivo
- Las líneas vacías se ignoran.
- Si una fila trae menos de 6 columnas, las que faltan se toman como vacías. Lo que pase de la sexta columna se ignora.
- No se soportan campos entre comillas, porque el formato del sistema legado no los usa.
- Una placa vacía se guarda como nula. La placa no se valida, porque el reglamento no la menciona.

### 2.7 Sin historial de importaciones
**Decisión:** no se guardan tablas de importaciones ni de filas importadas. Solo se persisten los tickets aceptados, igual que los del API.
**Por qué:** el reto solo exige que los tickets liquidados e importados sobrevivan a un reinicio. La respuesta del endpoint ya informa qué filas se aceptaron y cuáles se rechazaron.

---

## 3. Contrato del API

### 3.1 Ticket repetido en el API (o en otra importación)
**Decisión:** si llega un ticket con un código que ya existe, se **actualiza** el ticket y su cobro con la nueva liquidación. El desglose anterior queda marcado como eliminado (eliminación lógica).
**Por qué:** es coherente con la regla del CSV ("vale la última"), y permite corregir un ticket mal ingresado.

### 3.2 `ticket_perdido` ausente
**Interpretación:** se toma como `false`, igual que el campo `perdido` vacío en el CSV, que equivale a NO.

### 3.3 Errores que el contrato no define
- **Falta el campo `ticket`:** `400` con el formato `{codigo, mensaje}`.
- **JSON mal formado, parámetro faltante o ruta inexistente:** se mantiene el estado HTTP estándar (400, 404, …) con el mismo formato, y `codigo` es el estado (`"400"`).
- **Error no previsto:** `500` con el mensaje "Error interno del servidor".

**Por qué:** el contrato pide el mismo formato de error en todos los endpoints, pero solo define los códigos E01 a E04 con estado 422. **E05 no está en el contrato**: lo agregué yo, también con estado 422 (ver 2.1).

### 3.4 Cierre de caja
- **Día del cobro:** cada cobro pertenece al cierre del **día de su salida** en hora de Lima. Un vehículo que entra el 31/10 a las 20:00 y sale el 01/11 a las 09:00 se suma al cierre del 01/11.
- **Fecha inválida:** responde E03; si está vacía, E05. Mismo criterio que en la liquidación.
- **`perdidos`:** suma el **total completo** que pagaron esos tickets (tarifa + cargo del Art. 10), no solo los S/ 25.00 del cargo.
- **`por_tipo`:** siempre incluye AUTO y MOTO, aunque tengan 0 tickets, para que la pantalla muestre los dos.

---

## 4. Decisiones técnicas

| Decisión | Por qué |
|---|---|
| Las fechas se guardan como `Instant` (UTC, columnas `timestamptz`), y el cálculo se hace en hora de Lima | Un `Instant` es un momento exacto, sin depender de la zona del servidor ni de la base. Al calcular, todo se convierte a Lima (Art. 1), porque las franjas, los días de la semana, los feriados, el tope por fecha y el cierre dependen de la hora local |
| `fecha_cierre` se guarda como `LocalDate` | Es una fecha del calendario de Lima (el "día de caja"), no un momento. El cierre queda como un filtro directo por fecha |
| Montos en céntimos (`long`) | Se evitan los errores de redondeo de `double`. El contrato ya trabaja en céntimos |
| Lógica de cobro aislada en `service/tarifa` | La calculadora no conoce la base de datos ni los DTOs, así que se prueba con tests unitarios y una adenda nueva se aplica en un solo lugar |
| Un código de error define su estado HTTP (`CodigoError`) | Agregar un error nuevo es una línea, sin tocar el manejador global |
| Eliminación lógica (`is_deleted`) en lugar de borrar filas | Se conserva el historial de desgloses anteriores cuando un ticket se vuelve a liquidar |
| Cada fila del CSV se guarda en su propia transacción | Una fila rechazada no deshace las filas aceptadas |
| PostgreSQL publicado en el puerto 5433 | Para no chocar con una instalación local de PostgreSQL en el 5432. Dentro de Docker se usa el 5432 |
| Sin `cascade`: el `Ticket`, el `CobroTicket` y sus `DetalleCobro` se guardan por separado | El orden de guardado queda explícito en el servicio y no depende de la configuración de JPA |
| No hay un endpoint para "crear ticket": liquidar lo crea o lo actualiza | El reto solo pide liquidar, importar y cerrar caja. El ticket lo emite otro sistema en la entrada, y en la caja se recibe ya impreso. Un flujo en dos pasos (registrar la entrada y cobrar después) queda como mejora futura |
| Adminer incluido en `docker compose` (puerto 8081) y siempre activo | Permite revisar la base desde el navegador sin instalar ningún cliente |
| `BackendApplicationTests` necesita PostgreSQL en el puerto 5433 | Es el test que levanta todo el contexto de Spring, incluida la conexión a la base. Los demás tests no necesitan base de datos |

---

## 5. Frontend

| Decisión | Por qué |
|---|---|
| La URL del API está en `environment.ts` / `environment.development.ts`, y el backend habilita CORS para `http://localhost:4200` | Angular se ejecuta en el navegador y llama directo al backend. Así no hace falta un proxy y funciona igual con `ng serve` y con Docker |
| Entrada y salida se envían **sin zona** (`2026-10-12T07:00`), tal como las devuelve `<input type="datetime-local">` | El operador copia la hora impresa en el ticket, que es hora de Lima, y el backend la interpreta así. Si se convirtiera con `Date`, una PC en otra zona horaria (por ejemplo UTC−4) correría la hora y cambiaría el cobro |
| La fecha del cierre empieza en "hoy en Lima" (`Intl.DateTimeFormat` con `America/Lima`) y se muestra sin pasar por `Date` | Por la misma razón: en otra zona horaria podría mostrarse otro día |
| El tipo de vehículo se elige en un `<select>` (AUTO o MOTO) | Evita errores de escritura. Por eso E02 no se puede provocar desde la pantalla; se prueba con el CSV o con el API |
| Todos los errores HTTP pasan por un interceptor que los convierte a `{codigo, mensaje}` | Las pantallas muestran siempre el mismo recuadro de error, venga de una regla de negocio (E01–E05), de un error de Spring o de que el backend no responde (`SIN_CONEXION`) |
| Montos en céntimos hasta la vista, y un pipe `soles` los formatea (`900` → `S/ 9.00`) | El frontend no hace cálculos de dinero; solo muestra lo que devuelve el backend |
| Componentes standalone, signals y cada pantalla cargada bajo demanda (`loadChildren`) | Es el estilo recomendado en Angular 21, y cada feature queda independiente (rutas, modelos, servicio, página y componentes) |
