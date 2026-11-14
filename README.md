# Liquidador de Estacionamiento — Parqueo Quilla

Sistema fullstack que calcula el cobro de los tickets de Parqueo Quilla según el **Reglamento Tarifario (versión 3)**: liquidación de tickets, importación del CSV legado y cierre de caja.

| Capa | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Data JPA |
| Frontend | Angular 21 (standalone) |
| Base de datos | PostgreSQL 17 |
| Infraestructura | Docker Compose |

---

## Levantar el proyecto (un solo comando)

Requisito: **Docker** con Docker Compose.

```bash
docker compose up --build
```

| Servicio | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend (API) | http://localhost:8080/api |
| Adminer (visor de la base de datos) | http://localhost:8081 |
| PostgreSQL | `localhost:5433` (base, usuario y contraseña: `parqueo`) |

Para entrar a Adminer: sistema **PostgreSQL**, servidor `db`, usuario/contraseña/base `parqueo`.

Los datos se guardan en el volumen `pgdata`, así que sobreviven a un reinicio. Para detener todo: `docker compose down` (con `-v` también se borran los datos).

## Levantar cada parte por separado (desarrollo)

Requisitos: Java 21, Maven, Node 22.12 o superior.

```bash
# 1. Base de datos (y Adminer)
docker compose up -d db adminer

# 2. Backend → http://localhost:8080
cd backend
mvn spring-boot:run

# 3. Frontend → http://localhost:4200
cd frontend
npm install
npm start
```

El backend se conecta por defecto a `localhost:5433`. Se puede cambiar con las variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` y `CORS_ALLOWED_ORIGINS`.

---

## Uso del frontend

| Pantalla | Ruta | Qué hace |
|---|---|---|
| Liquidación | `/liquidacion` | Se escriben los datos del ticket (código, tipo, entrada, salida, ticket perdido) y muestra el total con su desglose por artículo del reglamento. Si el ticket no existe, se crea; si existe, se vuelve a liquidar |
| Importación | `/importacion` | Se sube el CSV del sistema legado y muestra las filas aceptadas (con su total) y las rechazadas (con su código de error) |
| Cierre de caja | `/cierre` | Se elige una fecha (por defecto, hoy en Lima) y muestra la cantidad de tickets, el total y los subtotales por tipo y de tickets perdidos |

Entrada y salida se escriben **tal como figuran en el ticket** (hora de Lima). La zona horaria de la computadora no afecta el cálculo.

### Archivos de ejemplo

En la carpeta [`ejemplos/`](ejemplos/) hay CSV listos para probar la importación:

| Archivo | Qué prueba |
|---|---|
| `importacion_1_valido.csv` | Filas válidas con distintos formatos de fecha, tolerancia, tope diario, ticket perdido y moto |
| `importacion_2_errores.csv` | Un caso de cada error (E01 a E05) y un ticket repetido (vale la última fila) |
| `importacion_3_noche_y_feriado.csv` | Permanencias que cruzan la medianoche, domingo/feriado (01/11/2026) y tope en dos días |

Después de importarlos, el cierre de `2026-11-01`, `2026-11-10` y `2026-11-11` muestra los totales de esas fechas.

---

## Tests

### Backend

```bash
# 1. Desde la raíz: levantar la base de datos (la necesita BackendApplicationTests)
docker compose up -d db

# 2. Correr todos los tests
cd backend
mvn test
```

`BackendApplicationTests` verifica que la aplicación completa arranque, por lo que necesita PostgreSQL en `localhost:5433`. No hace falta levantar el backend. El resto de los tests no usan la base de datos; para correr solo esos:

```bash
mvn test -Dtest="DivisorTramosTest,FeriadosTest,CalculadoraTarifaTest,EjemplosPublicosTest,ConversorDatosTest,ImportacionServiceTest"
```

| Clase de test | Qué cubre |
|---|---|
| `CalculadoraTarifaTest` | Al menos un test por artículo del reglamento (Arts. 1 a 12, Adendas 1 y 2) |
| `DivisorTramosTest` | Permanencia (Art. 2) y división en tramos por franja (Art. 3) |
| `FeriadosTest` | Feriados del Anexo A y día especial (Art. 5) |
| `EjemplosPublicosTest` | Ejemplos públicos E1 a E5, incluido el desglose de E5 |
| `ConversorDatosTest` | Formatos de fecha aceptados y errores E02 a E05 |
| `ImportacionServiceTest` | CSV de ejemplo del enunciado: filas aceptadas, rechazadas y tickets repetidos |

### Frontend

```bash
cd frontend
npm install
npm test -- --watch=false
```

Verifica que la aplicación se cree y muestre la barra de navegación. Las reglas de cobro se prueban en el backend; el frontend no hace cálculos.

---

## API

Todos los montos van en **céntimos enteros**. Todas las horas se interpretan en **hora de Lima (UTC−05:00)**.

### Liquidar — `POST /api/liquidaciones`

```json
{
  "ticket": "T-1001",
  "tipo": "AUTO",
  "entrada": "2026-10-12T10:00:00-05:00",
  "salida": "2026-10-12T11:20:00-05:00",
  "ticket_perdido": false
}
```

Respuesta `200`:

```json
{
  "ticket": "T-1001",
  "minutos": 80,
  "total_centimos": 900,
  "detalle": [
    { "concepto": "Tramo diurno normal, 12/10 10:00–11:20", "articulo": "Art. 4", "monto_centimos": 900 }
  ]
}
```

Si ya existe un ticket con el mismo código, se vuelve a liquidar y se reemplaza su cobro.

### Importar CSV — `POST /api/importaciones`

Recibe el archivo como `multipart/form-data` en el campo **`archivo`**. Formato: UTF-8, separado por comas, con cabecera `ticket,placa,tipo,entrada,salida,perdido`.

El archivo de ejemplo del enunciado está en `backend/legado_ejemplo.csv`. Desde la raíz del proyecto:

```bash
curl -F "archivo=@backend/legado_ejemplo.csv" http://localhost:8080/api/importaciones
```

Respuesta `200` (con el CSV de ejemplo del enunciado):

```json
{
  "aceptadas": [
    { "fila": 3, "ticket": "T-1002", "total_centimos": 670 },
    { "fila": 7, "ticket": "T-1001", "total_centimos": 2100 },
    { "fila": 8, "ticket": "T-1006", "total_centimos": 1600 },
    { "fila": 9, "ticket": "T-1007", "total_centimos": 2800 }
  ],
  "rechazadas": [
    { "fila": 4, "ticket": "T-1003", "codigo": "E02", "motivo": "El tipo de vehículo no es AUTO ni MOTO" },
    { "fila": 5, "ticket": "T-1004", "codigo": "E03", "motivo": "Una fecha u hora no tiene un formato aceptado" },
    { "fila": 6, "ticket": "T-1005", "codigo": "E01", "motivo": "La salida es anterior a la entrada" },
    { "fila": 10, "ticket": "T-1008", "codigo": "E05", "motivo": "Falta una fecha u hora obligatoria" }
  ]
}
```

| Campo | Significado |
|---|---|
| `fila` | Número de línea en el archivo (la cabecera es la línea 1) |
| `ticket` | Código del ticket |
| `total_centimos` | Total liquidado (solo en aceptadas) |
| `codigo`, `motivo` | Código de error y su descripción (solo en rechazadas) |

Si un ticket se repite, solo se procesa su última fila; las anteriores no aparecen en ninguna lista.

### Cierre de caja — `GET /api/cierre?fecha=YYYY-MM-DD`

Un ticket pertenece al cierre de la fecha de su **salida, en hora de Lima**.

```bash
curl "http://localhost:8080/api/cierre?fecha=2026-10-12"
```

Respuesta `200` (tras importar el CSV de ejemplo):

```json
{
  "fecha": "2026-10-12",
  "cantidad": 4,
  "total_centimos": 7170,
  "por_tipo": [
    { "tipo": "AUTO", "cantidad": 2, "total_centimos": 3700 },
    { "tipo": "MOTO", "cantidad": 2, "total_centimos": 3470 }
  ],
  "perdidos": { "cantidad": 1, "total_centimos": 2800 }
}
```

| Campo | Significado |
|---|---|
| `cantidad`, `total_centimos` | Tickets y total cobrado en la fecha |
| `por_tipo` | Subtotal por tipo de vehículo (siempre aparecen AUTO y MOTO, aunque tengan 0) |
| `perdidos` | Tickets perdidos de la fecha y el total que pagaron (tarifa + cargo del Art. 10) |

### Errores

Todos los endpoints responden los errores con el mismo formato:

```json
{ "codigo": "E01", "mensaje": "La salida es anterior a la entrada" }
```

| Código | HTTP | Cuándo |
|---|---|---|
| E01 | 422 | La salida es anterior a la entrada |
| E02 | 422 | El tipo de vehículo no es AUTO ni MOTO |
| E03 | 422 | Una fecha u hora no tiene un formato aceptado (o no existe, p. ej. 2026-02-30) |
| E04 | 422 | El campo perdido no es SI, NO ni vacío (solo en importación) |
| E05 | 422 | Falta una fecha u hora obligatoria (código agregado; ver DECISIONES.md) |
| `400`, `404`, … | estándar | Errores de la solicitud (JSON mal formado, falta un parámetro, ruta inexistente) |
| `500` | 500 | Error interno no previsto |

En los errores estándar, `codigo` es el estado HTTP (por ejemplo `"400"`).

---

## Estructura del backend

```
com.quilla.parqueo
├── controller            endpoints REST
├── dto/request, response contrato JSON del API
├── domain                objetos de negocio (DatosTicket, ResultadoLiquidacion, LineaDetalle)
├── model                 entidades JPA (Ticket, CobroTicket, DetalleCobro)
├── repository            acceso a datos
├── service               LiquidacionService, ImportacionService, CierreService
│   └── tarifa            reglamento: CalculadoraTarifa, DivisorTramos, Feriados, Tramo
├── util                  ConversorDatos (texto → tipos, errores E02–E05), HoraLima (Art. 1)
├── exception             CodigoError, ReglaNegocioException, GlobalExceptionHandler
└── config                CORS
```

La lógica de cobro está aislada en `service/tarifa`: no depende de la base de datos ni del API, por lo que se prueba con tests unitarios simples.

## Estructura del frontend

```
src/app
├── core
│   ├── interceptors      errorInterceptor: todo error HTTP → { codigo, mensaje }
│   ├── models            ApiError, TipoVehiculo
│   └── layout/navbar     barra de navegación
├── shared
│   ├── components        error-alert (recuadro de error)
│   └── pipes             soles (900 → "S/ 9.00")
└── features              una carpeta por pantalla, cargada bajo demanda
    ├── liquidacion       routes, models, services, pages/liquidacion-page, components/desglose-tabla
    ├── importacion       routes, models, services, pages/importacion-page, components/filas-aceptadas, filas-rechazadas
    └── cierre            routes, models, services, pages/cierre-page, components/subtotales
```

La URL del API está en `src/environments/` (`http://localhost:8080/api`).

## Documentación adicional

- [DECISIONES.md](DECISIONES.md): puntos ambiguos del reglamento y cómo se interpretaron.
- [IA.md](IA.md): herramientas de IA usadas y para qué.
