La propuesta es un **monolito por capas con tres procesos completos**: venta semanal, Compra Quinteros y gestión/reportes. Recomiendo conservar los cinco patrones acordados y completar el circuito hasta facturación y cobranza, porque forman parte de la entrega académica.

Leí íntegramente `AGENTS.md`, `context/SUPERPROMPT.md`, las nueve páginas de las dos entregas y los diagramas de la Actividad 2. Consulté las diapositivas de UADE sobre MVC, Strategy y State. **No escribí código ni modifiqué archivos o carpetas.**

Para distinguir el respaldo de cada decisión usaré:

- **E1:** entrega inicial, centrada en el problema operativo.
- **E2:** Actividad 2, con procesos, dominio, arquitectura y patrones.
- **S:** superprompt; documentado internamente, pero no equivale por sí solo a una exigencia académica.
- **P:** propuesta técnica mía, pendiente de aprobación y sin especificación explícita en las fuentes.

Fuentes académicas:

**E1:** :codex-file-citation{path="C:/Users/Noxie-PC/OneDrive/Documentos/Comunitarias/context/entregas/Proyecto _Comunitarias_ - Grupo 2 (2).pdf" purpose="source"}

**E2:** :codex-file-citation{path="C:/Users/Noxie-PC/OneDrive/Documentos/Comunitarias/context/entregas/Actividad 2 - Proyecto Comunitarias - Grupo 2 (1).pdf" purpose="source"}

**1. Requerimientos funcionales y no funcionales**

| ID | Requerimiento funcional documentado | Respaldo |
|---|---|---|
| RF01 | Administrar productos, proveedores, clientes, usuarios y perfiles. | E1, E2 |
| RF02 | Mantener un producto independiente del proveedor; registrar código, precio y condiciones por proveedor. | E2, S |
| RF03 | Conservar precios históricos de compras y permitir su consulta/comparación. | E1; E2 incluye vigencias y precios en detalles |
| RF04 | Definir y publicar la oferta semanal; el cliente consulta productos y precios. | E2, S |
| RF05 | El cliente realiza pedidos desde su usuario y consulta su seguimiento. | E2 |
| RF06 | Consolidar demanda, validar stock y generar compras por faltantes. | E2, S |
| RF07 | Generar Compra Quinteros independiente de los pedidos; evaluar proveedor, precio y condiciones. | E2, S |
| RF08 | Registrar recepción y sus cantidades; actualizar inventario. | E2, S |
| RF09 | Registrar ingresos, egresos, reservas y ajustes de stock. | E2 |
| RF10 | Preparar, despachar y confirmar entrega; la distribución física es tercerizada. | E2, S |
| RF11 | Facturar pedidos entregados y registrar pagos/cobranzas. | E2, S |
| RF12 | Consultar ventas, compras, stock, clientes, proveedores, cobranzas y rentabilidad. | E2 |
| RF13 | Gerencia participa en decisiones de compra; el diagrama indica evaluar/autorizar Quinteros. | E2 |
| RF14 | Trabajar con información originada en distintas fuentes. | E1; no especifica importadores automáticos |

**Recorte propuesto del MVP —P—:** formularios simples, consultas y operaciones completas; newsletter publicado dentro de la aplicación; datos de demostración precargados; facturación interna simplificada; sin envío automático de emails, OCR o importación Excel. Estas exclusiones son propuestas de implementación, no requisitos explícitos de las entregas.

| Requerimiento no funcional documentado | Respaldo |
|---|---|
| Centralización, trazabilidad y conservación del historial. | E1, E2 |
| Modularidad, separación de responsabilidades y evolución incremental. | E2 |
| Java 21, Spring Boot, React/Vite/TypeScript y PostgreSQL en Supabase. | AGENTS, S y tu solicitud |
| DTOs, validación de solicitudes y errores HTTP coherentes. | S |
| Credenciales mediante variables de entorno; dinero con `BigDecimal`; operaciones transaccionales. | S |
| Patrones reales, documentación UML y pruebas unitarias independientes de Supabase. | AGENTS, S |
| Ejecución local, código sencillo y defendible por los cuatro integrantes. | S |
| Demostración de los procesos principales en cinco minutos. | S y tu solicitud |

**Criterios adicionales propuestos —P—:** impedir stock disponible negativo, proteger operaciones repetidas, conservar referencias y fechas para auditoría, verificar permisos en backend y mostrar errores sin simular éxito. Las fuentes no fijan disponibilidad, concurrencia, tiempos de respuesta, cobertura porcentual ni volumen de datos; no los trataría como compromisos académicos.

**2. Inconsistencias y decisiones pendientes**

| Diferencia encontrada | Implicación | Resolución propuesta |
|---|---|---|
| E2 incluye usuario propio e inicio de sesión; S propone selector de perfiles sin autenticación real. | El selector no cumple literalmente el diagrama entregado. | **P:** login mínimo con cuentas precargadas y roles; sin registro público ni recuperación de contraseña. |
| E2 incluye `Rol`, `Recepcion` y `DetalleRecepcion`; S no los enumera. | Omitirlos reduce el modelo académico y pierde trazabilidad. | Mantenerlos. |
| E2 incluye reservas y ajustes; S destaca principalmente recepción y actualización de stock. | Calcular solo ingresos menos egresos permite comprometer el mismo stock varias veces. | Mantener reservas y ajustes; aprobar sus reglas concretas. |
| E1 exige historial de precios de compra; S enfatiza precios históricos del pedido. | Guardar solo precios de venta no cubre E1. | Conservar precios de compra en detalles y vigencias de `ProductoProveedor`. |
| E2 incluye facturación, cobranza y consultas del cliente; la API ejemplo y aceptación de S no las cubren completamente. | Puede aprobarse un MVP técnicamente funcional pero académicamente incompleto. | Incluir endpoints y pantallas mínimos de factura/pagos. |
| E2 asigna a Gerencia evaluación/autorización de Quinteros; S no define ese paso. | Falta definir quién autoriza y qué se registra. | **P:** autorización explícita, manual, registrada antes de generar la orden. |
| E2 usa `Newsletter`/`ItemNewsletter`; S usa `OfertaSemanal`/`DetalleOferta`. | Diferencia de nombres, no de concepto. | Usar `OfertaSemanal`/`DetalleOferta` y documentar equivalencia. |
| E2 usa `EstrategiaCompra`/`EstadoPedido`; S usa `CompraStrategy`/`PedidoState`. | Diferencia de nomenclatura. | Adoptar nombres de S conservando participantes y responsabilidades. |
| El diagrama State de E2 muestra cuatro estados; su texto menciona seis, coincidentes con S. | El dibujo está resumido. | Implementar los seis estados. |
| E2 dibuja `PedidoRepositoryDB`; S propone Spring Data JPA. | Cambia la implementación concreta del repositorio. | Usar interfaces Spring Data y explicar la implementación provista por el framework. |

La Actividad 2 amplía el alcance de E1; eso es una evolución del diseño, no una contradicción por sí misma. Tampoco encontré una consigna docente que establezca que los cinco deban ser patrones GoF: **son los cinco seleccionados por el equipo**, y pertenecen a categorías distintas.

**3. Arquitectura mínima**

Flujo propuesto:

**React → REST/DTO → controladores Spring → servicios de aplicación → dominio/patrones → repositorios JPA → PostgreSQL en Supabase.**

| Componente | Responsabilidad |
|---|---|
| React, Vite, TypeScript y Tailwind | Formularios, navegación, presentación, errores y consumo mediante Fetch. |
| Controladores REST | Recibir solicitudes, validar entrada y delegar. |
| Servicios | Ejecutar casos de uso, comprobar permisos y coordinar transacciones. |
| Dominio | Pedidos, compras, precios, inventario y reglas de State/Strategy. |
| Repositorios | Consultar y persistir mediante Spring Data JPA. |
| PostgreSQL | Relaciones, restricciones e integridad persistente. |

Java 21 es compatible con la versión documentada actualmente de Spring Boot. Propongo fijar una versión estable compatible al iniciar la implementación y mantenerla durante el MVP. [Requisitos oficiales de Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html).

Para Supabase, propongo conexión JDBC desde Spring Boot mediante el **Session Pooler y SSL**. La documentación oficial recomienda esa modalidad para Spring Boot; la conexión directa depende de la conectividad disponible. [Guía oficial de Supabase para Spring Boot](https://supabase.com/docs/guides/getting-started/quickstarts/spring-boot).

**Decisiones P:** backend y frontend locales durante la demostración; Supabase remoto; mapeo manual de DTOs; entidades JPA que también representan el dominio, evitando duplicar modelos en esta primera versión. Para el login propuesto, agregar Spring Security se justifica por sesiones, contraseñas protegidas y permisos. Esta dependencia adicional requiere aprobar primero la decisión de autenticación.

La estructura futura seguiría las carpetas existentes:

| Ruta | Contenido previsto |
|---|---|
| `backend/` | Maven; paquetes `controller`, `service`, `domain`, `repository`, `dto`, `mapper`, `strategy`, `state`, `config`; pruebas. |
| `frontend/` | Componentes, páginas, servicios HTTP, hooks y tipos. |
| `database/` | Esquema y datos iniciales versionados. |
| `docs/` | Arquitectura, API, patrones y diagramas PlantUML. |
| `context/` | Fuentes del proyecto. |

Es una estructura propuesta; no se creó.

**4. Modelo de dominio y relaciones**

| Entidad o grupo | Relaciones y datos centrales |
|---|---|
| `Rol`, `Usuario`, `Cliente` | Un rol tiene muchos usuarios; un cliente accede mediante un usuario. Identidad, contacto, perfil y estado. |
| `Producto` | Identificador estable, nombre, marca, categoría, presentación y estado. |
| `Proveedor` | Identidad comercial, contacto y estado. |
| `ProductoProveedor` | Relaciona producto y proveedor; código, precio de compra, cantidad mínima, condiciones y vigencia. |
| `OfertaSemanal`, `DetalleOferta` | Una oferta contiene muchos detalles; cada detalle refiere a un producto y conserva precio de venta y cantidad ofertada. |
| `Pedido`, `DetallePedido` | Un cliente tiene muchos pedidos; cada pedido pertenece a una oferta y contiene cantidades y precios históricos. |
| `OrdenCompra`, `DetalleOrdenCompra` | Un proveedor tiene muchas órdenes; cada orden distingue `DEMANDA_CLIENTES` o `STOCK_QUINTEROS`, con detalles y precios de compra históricos. |
| `Recepcion`, `DetalleRecepcion` | Una orden admite recepciones; cada detalle indica lo recibido contra un detalle de compra. |
| `MovimientoStock` | Un producto tiene muchos movimientos, con tipo, cantidad, fecha, motivo y referencia al pedido o recepción cuando corresponda. |
| `Factura`, `Pago` | Un pedido tiene cero o una factura; una factura tiene muchos pagos. |

Estas entidades se apoyan en E2 y S. **Las cardinalidades y restricciones operativas siguientes son P**, salvo donde se señala respaldo:

- Mantener versiones de `ProductoProveedor` por vigencia, evitando sobrescribir el historial.
- Cada orden tiene un único proveedor, como muestra E2. Una consolidación con varios proveedores genera varias órdenes.
- Recibir cantidades parciales y bloquear excedentes; calcular faltantes desde lo comprado y recibido.
- Conservar los precios históricos aunque cambien los precios actuales.
- Registrar autor y fecha de transiciones, autorizaciones y movimientos.
- Una factura interna por pedido entregado; pagos parciales permitidos, sin superar el saldo.

**Reglas de inventario propuestas —P—:**

- Stock físico = ingresos − egresos + ajustes firmados.
- Disponible = físico − reservas activas.
- Al iniciar preparación se reserva la cantidad completa, si hay disponibilidad.
- Al despachar se libera la reserva y se registra el egreso, dentro de la misma transacción.
- Confirmar entrega no vuelve a descontar stock.
- Repetir recepción o despacho no duplica movimientos.

Para demanda: **faltante = demanda pendiente no cubierta − disponibilidad utilizable − compras pendientes ya asignadas**, con mínimo cero. Propongo registrar la asignación de abastecimiento por pedido/producto para evitar comprar dos veces la misma demanda. Esa relación auxiliar no aparece explícitamente en las fuentes.

La selección de proveedor no tiene un algoritmo documentado: propongo selección manual por Operación, validando que el proveedor ofrezca los productos. Tampoco hay fórmula documentada de rentabilidad: propongo **margen bruto estimado = venta facturada − costo histórico asignado**, mostrando por separado compras y cobranzas. Restar todas las compras a todas las ventas mezclaría inventario aún no vendido con costos de ventas.

**5. Patrones y clases Java**

| Patrón | Clases/participantes propuestos | Justificación y evidencia de implementación |
|---|---|---|
| MVC | Vistas React; `PedidoController`, `CompraController`, `StockController`, `ReporteController`; dominio y servicios. | Separar interacción, recepción de acciones y reglas. Los controladores delegan y usan DTOs. |
| Repository | `ProductoRepository`, `ProveedorRepository`, `PedidoRepository`, `OrdenCompraRepository`, `FacturaRepository`, entre otros. | Aislar persistencia. Las pruebas de servicios reemplazan repositorios sin conectarse a Supabase. |
| Service Layer | `OfertaService`, `PedidoService`, `CompraService`, `RecepcionService`, `StockService`, `FacturacionService`, `ReporteService`. | Coordinar casos de uso y transacciones sin concentrarlos en controladores. |
| Strategy | `CompraStrategy`; `CompraPorDemandaStrategy`, `CompraQuinterosStrategy`; contexto `CompraService`. | Dos algoritmos distintos de abastecimiento bajo una interfaz común. Demanda calcula faltantes; Quinteros valida una oportunidad autorizada independiente de pedidos. |
| State | Contexto `Pedido`; interfaz `PedidoState`; `RecibidoState`, `ConfirmadoState`, `EnPreparacionState`, `PreparadoState`, `DespachadoState`, `EntregadoState`. | El pedido delega comportamiento al estado actual. Cada clase habilita operaciones y rechaza transiciones inválidas. |

El ciclo previsto es:

**RECIBIDO → CONFIRMADO → EN_PREPARACION → PREPARADO → DESPACHADO → ENTREGADO.**

Los seis estados están respaldados por el texto de E2 y S; **restringir el MVP a esta secuencia sin retrocesos es P**. La prohibición de facturar antes de entregar sí aparece en E2.

Persistir un enum y reconstruir el objeto State es válido; el enum solo no implementa el patrón. La selección de Strategy debe ser por solicitud: propongo evitar guardar una estrategia mutable compartida en `CompraService`.

UADE valida MVC con DTOs y Strategy como algoritmos intercambiables, en las diapositivas 17–30 de:

:codex-file-citation{path="C:/Users/Noxie-PC/OneDrive/Documentos/Comunitarias/context/material-uade/34210_03_UADEConnect_02_20251.pptx" purpose="source"}

State se describe mediante contexto, interfaz y estados concretos, en las diapositivas 5–9 y 17 de:

:codex-file-citation{path="C:/Users/Noxie-PC/OneDrive/Documentos/Comunitarias/context/material-uade/34210_05_UADEConnect_01_20251_ADICIONAL (1).pptx" purpose="source"}

Repository y Service Layer se justifican directamente en E2. No propongo agregar Observer, Adapter o Singleton al alcance.

**6. Endpoints REST mínimos**

Los endpoints de productos, proveedores, oferta actual, pedidos, compras, recepción, stock y resumen se basan en S. **Las ampliaciones y contratos concretos son P** para cubrir E1/E2.

| Grupo | Endpoints mínimos | Uso |
|---|---|---|
| Sesión —P— | `POST /api/sesion`, `GET /api/sesion`, `DELETE /api/sesion` | Iniciar, consultar y cerrar sesión. |
| Maestros | `GET/POST /api/productos`, `PATCH /api/productos/{id}`; equivalentes para proveedores y clientes | Alta, consulta y modificación básica; baja mediante estado. |
| Precios | `GET/POST /api/productos/{id}/proveedores` | Consultar historial y registrar precio/condiciones con vigencia. |
| Oferta | `GET /api/ofertas/actual`, `POST /api/ofertas`, `POST /api/ofertas/{id}/publicacion` | Preparar y publicar oferta. |
| Pedidos | `GET/POST /api/pedidos`, `GET /api/pedidos/{id}`, `PATCH /api/pedidos/{id}/estado` | Crear, seguir y operar pedido. |
| Compras | `GET /api/compras`, `GET /api/compras/{id}`, `POST /api/compras/demanda`, `POST /api/compras/quinteros` | Ejecutar ambas estrategias y consultar detalles históricos. |
| Recepción | `POST /api/compras/{id}/recepcion` | Registrar cantidades recibidas y movimientos. |
| Inventario | `GET /api/stock`, `GET /api/stock/movimientos`, `POST /api/stock/ajustes` | Consultar físico/reservado/disponible y registrar ajustes. |
| Administración | `POST /api/pedidos/{id}/factura`, `GET /api/facturas/{id}`, `POST /api/facturas/{id}/pagos` | Facturar, consultar factura y cobrar. |
| Reportes | `GET /api/reportes/resumen?desde=&hasta=` | Totales, saldos, stock, clientes/proveedores e indicadores. |

**P:** Quinteros incluye autorización verificable de Gerencia; el cliente solo consulta sus pedidos y facturas. Los usuarios/perfiles quedan precargados, sin CRUD de usuarios en esta primera versión: es una simplificación del módulo Administración de E2 que necesita aprobación.

Respuestas propuestas: `201` para creación, `400` para entrada inválida, `401/403` para sesión/permisos, `404` para inexistentes y `409` para conflictos de estado, stock o duplicación.

**7. Backlog para cuatro integrantes**

Asignación propuesta por responsabilidades, pendiente de distribuir entre los nombres del equipo. Los archivos indicados son futuros; cada tarea incluye pruebas de sus reglas.

| ID / responsable | Objetivo y archivos afectados | Dependencias | Criterios de aceptación |
|---|---|---|---|
| B0 / los cuatro | Aprobar alcance, reglas y contratos. Futuro `docs/arquitectura.md`, `docs/api.md`. | Ninguna | Resolver login, stock, precios, Quinteros y métricas; acordar entradas/salidas. |
| B1 / integrante 1 | Base Spring, esquema, Supabase y datos iniciales. `backend/pom.xml`, configuración, `database/`. | B0 | Compila; consulta y modifica PostgreSQL; credenciales externas. |
| B2 / integrante 1 | Maestros, precios históricos y oferta. Dominio, repositorios, servicios y controladores correspondientes. | B1 | ID estable; cambio de precio conserva historia; oferta publicada consultable. |
| B3 / integrante 2 | Pedido y State. `Pedido*`, `DetallePedido*`, `state/`. | B1, contrato B2 | Pedido real con precio histórico; seis estados; salto inválido rechazado y persistencia conservada. |
| B4 / integrante 3 | Strategy y compras. `CompraService`, `strategy/`, `OrdenCompra*`. | B2, B3; contrato de stock | Dos implementaciones reales; faltantes correctos; varias órdenes si hay varios proveedores; demanda no duplicada. |
| B5 / integrante 3 | Recepción e inventario. `Recepcion*`, `StockService`, movimientos/repositorios. | B1, B4; coordinación B3 | Recepción parcial; físico/reservado/disponible correctos; repetición sin doble ingreso/egreso. |
| B6 / integrante 2 | Facturación y pagos. `Factura*`, `Pago*`, `FacturacionService`. | B3 | Solo entregados; una factura por pedido; pagos actualizan saldo y no lo exceden. |
| B7 / integrante 1 | Login y roles mínimos. Configuración de seguridad, usuarios/sesión. | B0, B1 | Login real; cliente aislado; Operación y Gerencia con permisos acordados. |
| B8 / integrante 4 | Base React y circuito Cliente. `frontend/src/pages`, componentes, servicios/tipos. | Contratos B0; integración B2, B3, B7 | Login, oferta, pedido y seguimiento contra backend real; factura visible. |
| B9 / integrante 4 | Circuitos Operación y Compras. Páginas/servicios correspondientes. | B3–B6 | Ejecutar demanda, Quinteros, recepción, estados, factura y pago sin datos simulados. |
| B10 / integrante 1 | Reportes y costo estimado. `ReporteService`, controlador/DTOs. | B4–B6 | Totales reconciliables con detalles; pagos no se confunden con ventas; fórmula de margen explícita. |
| B11 / integrante 4 | Gerencia e integración de pantallas. Dashboard/autorización. | B7, B9, B10 | Autorizar Quinteros y consultar indicadores reales. |
| B12 / los cuatro | Pruebas integradas, UML, instrucciones y walkthrough. Pruebas, `docs/`, futuro README. | Todas | Procesos completos; reglas críticas probadas; ejecución reproducible y demostración ≤5 minutos. |

Para evitar bloqueos, B0 define contratos antes de implementar. El integrante 4 concentra React, pero los otros tres deben ayudar con integración y validación de sus pantallas. Cada tarea tiene un autor y un revisor distinto.

**8. Sprints hasta el 31 de octubre de 2026**

Desde hoy, **8 de octubre**, quedan tres bloques semanales y tres días finales. La capacidad horaria del equipo no está documentada: este calendario es **P**, no una estimación garantizada.

| Período | Prioridad | Resultado verificable |
|---|---|---|
| **8–14 octubre** | B0–B3, base B7/B8. Aprobar decisiones y lograr integración temprana. | Login, oferta consultada desde React, pedido persistido y prueba de State. |
| **15–21 octubre** | B4–B5; integrar reservas y pantallas operativas. | Demanda y Quinteros generan órdenes; recepción actualiza stock; pedido llega a entrega; errores críticos probados. |
| **22–28 octubre** | B6, B9–B12. Completar ciclo administrativo, reportes y documentación. | Tres procesos completos, factura/pagos, dashboard y primera grabación de cinco minutos. |
| **29–31 octubre** | Correcciones, ensayo y entrega. | Sin funcionalidades nuevas; ejecución verificada, grabación final y defensa de los cuatro. |

Ruta crítica: **contratos/modelo → pedido → compras/stock → entrega → factura/pago → reportes → ensayo**. La conexión a Supabase debe verificarse en el primer sprint.

Si falta tiempo, propongo recortar estética, filtros avanzados, exportaciones y automatizaciones. Cualquier recorte de login, reservas, recepción, factura o cobranza debe revisarse expresamente porque afecta compromisos de E2.

**Walkthrough propuesto —cinco minutos—**

| Tiempo | Demostración |
|---|---|
| 0:00–0:40 | Cliente inicia sesión y consulta la oferta. |
| 0:40–1:20 | Crea pedido; se muestran cantidades y precio histórico. |
| 1:20–2:10 | Operación consolida demanda y genera compra por faltantes: Strategy. |
| 2:10–2:50 | Recepción actualiza stock; reserva, preparación, despacho y entrega: State. |
| 2:50–3:20 | Se factura y registra pago; saldo actualizado. |
| 3:20–4:10 | Gerencia autoriza Quinteros; se genera y recibe compra independiente. |
| 4:10–4:40 | Dashboard e historial muestran resultados de ambos procesos. |
| 4:40–5:00 | Transición inválida rechazada y mapa breve de clases/patrones. |

Los datos se preparan previamente, pero las operaciones demostradas deben ejecutarse realmente.

Antes de implementar, necesitamos aprobar **login mínimo, reservas/egresos, historial de compra, autorización Quinteros, facturación interna y fórmula de margen**. Son las decisiones que concretan o resuelven las diferencias entre fuentes. La aprobación de esta propuesta dejaría lista únicamente la **ETAPA 0** para su siguiente autorización; todavía no se inició ninguna etapa.
