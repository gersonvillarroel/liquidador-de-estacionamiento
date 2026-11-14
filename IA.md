# Uso de IA

## Herramientas

| Herramienta | Uso |
|---|---|
| **Claude Code** (Anthropic), extensión de VS Code | Asistente de programación durante todo el proyecto |

## Para qué la usé

### Configuración del proyecto
- Generar el frontend con Angular 21 (standalone) y configurar los environments de desarrollo y producción.
- Configurar `application.yaml`, CORS, los Dockerfiles del backend y del frontend, `docker-compose.yml` (PostgreSQL, Adminer, backend y frontend), `.gitignore` y `.gitattributes`.
- Resolver el conflicto de puerto con una instalación local de PostgreSQL (se publicó en el 5433).

### Backend
- **Manejo de errores:** el enum `CodigoError`, `ReglaNegocioException` y el `GlobalExceptionHandler`, con el formato `{codigo, mensaje}` en todos los endpoints.
- **DTOs:** los response de importación (`ImportacionResponse`, `FilaAceptadaResponse`, `FilaRechazadaResponse`).
- **Services:**
  - `ImportacionService`: lectura del CSV legado, regla de "vale la última fila", validación de cada fila y liquidación de las aceptadas.
- **Tests:**
  - `CalculadoraTarifaTest` (un test por artículo), `DivisorTramosTest`, `FeriadosTest` y `EjemplosPublicosTest` (E1 a E5).
  - `ConversorDatosTest` (formatos de fecha y errores E02 a E05).
  - `ImportacionServiceTest` (CSV de ejemplo del enunciado).
- **Archivos de ejemplo:** los CSV de la carpeta `ejemplos/` para probar la importación y el cierre.

### Frontend
La IA escribió todo el código del frontend, siguiendo la estructura de carpetas que le indiqué (`core`, `shared` y una carpeta por feature, cada una con `routes`, `models`, `services`, `pages` y `components`):
- **core:** el interceptor de errores (`errorInterceptor`), los modelos `ApiError` y `TipoVehiculo` y la barra de navegación.
- **shared:** el componente `error-alert` y el pipe `soles`.
- **features:** las pantallas de liquidación (formulario y tabla de desglose), importación (subida del CSV y tablas de filas aceptadas y rechazadas) y cierre de caja (selector de fecha y subtotales), con sus servicios, modelos y rutas cargadas bajo demanda.
- Los estilos globales y el test de `App`.

### Documentación
La IA redactó la documentación del proyecto: el [README.md](README.md), [DECISIONES.md](DECISIONES.md) y este archivo. Yo la revisé y le indiqué qué agregar, quitar o corregir.

### Consultas y explicaciones
- Ventajas y desventajas de distintas opciones: proxy vs. environments, UTC vs. hora de Lima, `cascade` y `orphanRemoval` frente a la eliminación lógica, ProblemDetail vs. el formato del contrato, y la ubicación de las clases por paquete.
- Cómo se representan en Spring Boot / JPA las relaciones de mi modelo de datos (`ticket` 1:1 `cobro_ticket` 1:N `detalle_cobro`): `@OneToOne`, `@OneToMany`, `@ManyToOne`, `mappedBy`, `@JoinColumn`, `cascade` y `orphanRemoval`.
- Explicaciones de conceptos que no dominaba: records con métodos, streams, `@Component` e inyección de dependencias, y la aplicación de SOLID.
- Configuración de Git con dos cuentas de GitHub.

## Decisiones que tomé yo

- La estructura de paquetes del backend y el modelo de tres tablas.
- La estructura de carpetas del frontend (`core`, `shared`, `features`) y usar environments en lugar de un proxy.
- Guardar las fechas en UTC, usar eliminación lógica y guardar cada entidad por separado (sin `cascade`).
- Crear el código **E05** para T-1008, en lugar de usar E03.
- No guardar historial de importaciones.
- Revisar cada interpretación ambigua del reglamento antes de aceptarla (ver [DECISIONES.md](DECISIONES.md)).

## Cómo verifiqué el resultado

- Los ejemplos públicos E1 a E5 y el CSV de ejemplo del enunciado están cubiertos por tests automáticos, y todos pasan.
- Probé las tres pantallas del frontend en el navegador con los ejemplos de liquidación y los CSV de `ejemplos/`.
- Repasé a mano el cálculo de los ejemplos (en especial E5) para poder explicar en la defensa por qué cada caso da el monto que da.
