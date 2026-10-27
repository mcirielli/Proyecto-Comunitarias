# Ruta del equipo para completar el MVP

Actualización de ejecución 27/10: Pedido + State implementados en incremento
separado; ver PEDIDO_STATE.md y último reporte. El cierre operativo de M03/M04
sigue condicionado a inventario y sus reglas; no marcar MVP completo.

Estado al 27/10/2026. Lista de planificación para revisión del equipo, no nueva
autorización para modificar esquema ni aprobación de reglas pendientes.
Merge de PR #2 comprobado en origin/main, commit 6cd8309. El incremento State
se prepara por separado en codex/pedidos-state.

## Base disponible

- Docker, React/Spring y persistencia real de maestros en Supabase.
- Login local por Cliente/Operaciones/Gerencia, sesión, CSRF y permisos.
- Productos y proveedores: alta, consulta, activación/desactivación.
- Vistas separadas, menú por rol, temas y resumen de maestros.
- 72 pruebas estándar y 6 de integración Supabase aprobadas en la última verificación.
- MVC, Repository y Service Layer demostrables en maestros/pedidos. State ya
  implementado; Strategy pendiente. Tablas existentes no equivalen a casos de uso completos.
- Para repartir el código pendiente actualizado usar PENDIENTES_CODIGO_EQUIPO.md;
  la tabla siguiente conserva el mapa general y sus dependencias.

## Incrementos en orden de dependencia

| ID | Entrega necesaria | Criterio de terminado | Dependencia |
|---|---|---|---|
| M01 | Producto por proveedor e historial | Vincular sin cambiar ID Producto; código/condiciones; registrar nuevo precio sin perder anterior y consultar historia; BigDecimal y tests | Aprobar extensión para historial: tabla actual solo tiene precio vigente |
| M02 | Oferta semanal | Operaciones arma/publica oferta con vigencia y precio de venta; Cliente consulta datos reales sin ver costos de compra | Productos; acordar oferta vigente y si hay más de una publicada |
| M03 | Cliente y pedido | Cuenta demo vinculada a Cliente existente; cantidades válidas; precio tomado del servidor y copiado al detalle; Cliente solo ve sus pedidos | M02 y vínculo demo→Cliente explícito |
| M04 | State del pedido | Clases por estado y contexto; RECIBIDO→CONFIRMADO→EN_PREPARACION→PREPARADO→DESPACHADO→ENTREGADO; rechaza saltos; reconstruye estado persistido | M03; reglas de reserva/preparación con M05 |
| M05 | Stock y movimientos | Consulta disponible/reservado; reservas automáticas y egresos/ajustes trazables; sin negativos ni doble movimiento; transacciones y concurrencia probadas | Productos; acordar semántica de cantidades y momento de reserva |
| M06 | Compras y Strategy | Interfaz común con dos implementaciones: demanda calcula faltantes; Quinteros compra independiente de pedidos; proveedor y precios trazables; autorización manual Gerencia para Quinteros | M01, M03, M05; reglas de proveedor, órdenes abiertas y autorización |
| M07 | Recepción | Orden recibida actualiza stock y registra movimiento atómicamente; repetir recepción no duplica ingreso | M05/M06; acordar total/parcial y excedentes |
| M08 | Factura y cobranza | Solo pedido entregado; factura interna única por pedido, importe histórico; pago y saldo coherentes | M03/M04; acordar pago total/parcial, sobrepago y numeración |
| M09 | Resumen gerencial | Totales reales de ventas y compras para un período; cifras reconciliables con operaciones | M06/M08; acordar base de cada total |
| M10 | Cierre académico | Tests de State/Strategy/stock y flujo completo; UML refleja código; guía reproducible; demo ≤5 min y defensa de los cuatro | Todos los anteriores; pruebas/documentación se hacen durante cada incremento |

Orden de primer resultado visible: M02 → M03 → M04. M01/M05 se pueden preparar
en paralelo con responsables distintos; son necesarios antes de cerrar compras.
No presentar la primera venta parcial como MVP completo de los tres procesos.

## Decisiones de equipo antes del código dependiente

1. Cliente demo: resuelto en el incremento State mediante cliente separado y
   asociación controlada; no usar identidad negativa como FK ni aceptar suplantación.
2. Oferta: fechas inclusivas en Argentina y selección explícita ya implementadas
   para pedidos. Queda gestión/publicación y origen del precio de venta real.
   Propuesta simple: ingreso por Operaciones, sin fórmula automática de margen.
3. Historial: relación estable con registros de precio inmutables; vigencia y
   correcciones. Requiere revisar/aprobar cambio de esquema.
4. Stock: significado de disponible/reservado, cuándo reservar/liberar y cómo
   proceder ante faltantes; sincronizar esas reglas con State.
5. Compras: selección manual/automática de proveedor, alcance de consolidación,
   tratamiento de órdenes abiertas, autorización Quinteros y recepción parcial.
6. Administración: pago total/parcial, validaciones de saldo y bases de ventas/
   compras. Una cobranza no se convierte automáticamente en venta.
7. Confirmar con profesor el recorte ya decidido de rentabilidad/reportes ampliados:
   la entrega académica los incluye y no hay validación docente acreditada.

## Reparto propuesto entre cuatro integrantes

| Integrante | Responsabilidad principal | Archivos/frentes previstos | Revisión |
|---|---|---|---|
| 1 | Oferta, cliente/pedido y State | domain/state, PedidoService, repositorios/DTO/API y tests propios | 2 revisa reglas y estados |
| 2 | Relación proveedor/precios, stock y recepción | servicios/repositorios de precios e inventario; SQL solo aprobado; pruebas de concurrencia | 3 revisa movimientos y consistencia |
| 3 | Compras/Strategy y administración | strategy, CompraService, factura/pagos/reportes y sus tests; comenzar contratos mientras espera dependencias | 1 revisa cálculos y autorización |
| 4 | React e integración por rol | oferta/pedido, compras/recepción, estados y dashboard; componentes y servicios HTTP | Revisión cruzada con autor de cada API |

Todos: revisar PR de otro integrante, actualizar documentación de su módulo,
aportar tests de reglas y ensayar su explicación. El integrante 3 concentra
varios módulos: dividir factura/reportes con 1 al cerrar State si hay capacidad.
Nombres son asignaciones sugeridas; el equipo debe reemplazarlos por responsables.
Definir contrato DTO/API antes de trabajar backend/frontend en paralelo.

## Guion mínimo de aceptación

1. Operaciones vincula producto/proveedor y demuestra cambio de precio histórico.
2. Publica oferta; Cliente crea pedido con precio histórico.
3. Operaciones confirma; State bloquea una transición inválida y permite las válidas.
4. Compra por demanda cubre faltante; recepción ingresa stock sin duplicarse.
5. Se prepara, despacha y entrega; factura y pago completan la venta.
6. Gerencia autoriza Quinteros; compra sin pedido, recepción aumenta stock.
7. Dashboard muestra totales verificables; cada integrante explica un patrón/capa.

Preparar datos ficticios pequeños antes de grabar, sin alterar reglas para la demo.
Comprobar persistencia tras reinicio y aislamiento de roles.

## Fuera del camino crítico

Sin registro público, recuperación de contraseña, JWT/OAuth, carga Excel/OCR,
envío automático de newsletters, pagos online, logística propia, dominio o
despliegue público como requisito de demo local. No quitar funcionalidades ya
comprometidas para cerrar por calendario. Render preparado sigue siendo tarea
documental del Sprint 1; no equivale a publicación autorizada.

Fuentes: entregas en context/entregas, SUPERPROMPT, DECISIONES_ARQUITECTURA,
MVP_PENDIENTES (matriz histórica), DEMO_LOCAL y reportes de ejecución.
