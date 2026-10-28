# Código pendiente y reparto del equipo

C06 implementado en tino y verificado, pendiente revisión PR: emisión interna,
pagos parciales/saldo, idempotencia y roles. 003 aplicada con autorización;
136 pruebas estándar y 20 Supabase aprobadas. No modifica integración de inventario
que desarrolla Andy. Evidencia: reportes/2026-10-28-facturacion-pagos-tino.md.


Actualizado: 27/10/2026, después de Pedido + State.
C01 implementado y verificado con Supabase: migración 002 aplicada con autorización,
111 pruebas estándar y 13 de integración aprobadas. Demanda y Quinteros con
aprobación/emisión reales; revisión de cierre en reportes/2026-10-27-cierre-compras-pr5.md.
Recepción, reservas y liberación de coberturas siguen pendientes de inventario.
Prioridad académica: cinco patrones reales, con pruebas y explicación.
La prioridad indica orden de trabajo; no elimina compromisos del MVP.

## Ya implementado: reutilizar, no duplicar

- MVC, Repository y Service Layer en maestros y pedidos.
- Login local por tres roles, sesiones/CSRF y permisos.
- Productos/proveedores: alta, consulta y activo/inactivo persistidos.
- Pedido/DetallePedido, creación con precios de oferta históricos, consulta
  aislada por cliente y cambios de estado solo para Operaciones.
- State real: interfaz, seis clases, contexto Pedido y reconstrucción persistida.
- Fixtures explícitos y separados de cliente/oferta para la demo.
- React: login, resumen, maestros y pedidos; navegación por rol y ambos temas.
- Última verificación: 111 pruebas estándar y 13 de integración Supabase aprobadas (C01 + C05).

El catálogo de tablas en Supabase no sustituye los servicios/reglas aún ausentes.
Oferta semanal C05: alta de borrador, publicación y catálogo por rol implementados;
selección explícita hacia Pedidos. Sin edición de publicadas, según aprobación.
Reglas y demo: [OFERTAS_SEMANALES.md](OFERTAS_SEMANALES.md).
Los cambios de estado todavía no reservan, descuentan ni comprueban inventario.

## Backlog de implementación

| ID / prioridad | Entrega | Archivos o áreas por crear/extender | Aceptación verificable | Depende de |
|---|---|---|---|---|
| C01 / P0 | Strategy y órdenes de compra implementados para demo | strategy/CompraStrategy, CompraPorDemandaStrategy, CompraQuinterosStrategy; domain/OrdenCompra y detalles; repository/OrdenCompraRepository; service/CompraService; DTO/controller de compras; ComprasPage | Mismo contrato con dos algoritmos reales; demanda consolida pedidos y calcula faltantes; Quinteros funciona sin pedidos; proveedor y precio históricos; Gerencia autoriza Quinteros; tests sin BD y persistencia | Contrato de stock C02, precios C04 y reglas de proveedor/órdenes abiertas |
| C02 / P0 | Stock, reservas y vínculo con State | domain/Stock, MovimientoStock; repositorios; service/StockService; integrar PedidoService; DTO/controller y StockPage | Disponible/reservado coherentes; reserva automática en momento acordado; egreso una sola vez; impide preparar sin stock; bloqueos/transacciones y pruebas concurrentes | Acordar significado de cantidades y momento de reserva/liberación |
| C03 / P0 | Recepción de compras | RecepcionService o caso de uso acotado de compras/inventario; repositorios de C01/C02; API y vista de recepción | Recibir orden ingresa stock y movimiento atómicamente; repetir no duplica; parciales/excedentes según regla revisada | C01/C02; dueño de integración definido abajo |
| C04 / P1 | Producto-proveedor e historial de precios | domain/ProductoProveedor e historial; repositorios; PrecioProveedorService; API/DTO y vista de vínculo/precios; migración solo aprobada | Producto mantiene ID con varios proveedores; código/condiciones por vínculo; nuevo precio conserva anterior; consulta actual/histórica | Revisar extensión del esquema: precio_compra actual no guarda historia |
| C05 / P1 | Gestión y consulta de oferta semanal integrada en main (PR #4) | Extender Oferta/OfertaRepository; OfertaService, DTO/controller; OfertaPage para Cliente y gestión Operaciones | Armar/publicar con fechas/precio de venta; Cliente solo ve ofertas publicadas vigentes sin costos; se reutilizan entidades existentes; fuera de vigencia no permite pedido | Productos existentes; política aprobada documentada en OFERTAS_SEMANALES.md |
| C06 / P1 | Factura interna y pagos implementados; revisión PR pendiente | domain/Factura, Pago; repositorios; FacturacionService/PagoService; DTO/controller y vistas | Solo factura pedido entregado; una factura por pedido; importe calculado desde detalles históricos; pagos/saldo válidos; transacciones y roles | Pedido/State existentes; política de pagos/numeración |
| C07 / P1 | Resumen gerencial de ventas/compras | ReporteService, consultas de repositorio y DTO/controller; ReportesPage | Totales reales por período, reconciliados con factura/orden; no confundir pago con venta | C01/C06; acordar base de cálculo |
| C08 / P0 transversal | Pruebas, integración y defensa | Tests por módulo; docs/diagramas; contratos API; guía demo | State y Strategy explicables, tests de reglas y permisos, flujo completo con BD, UML coincide con clases, ejecución limpia y demo de 5 min | Durante cada tarea; cierre después de integración |

P0: primero para demostrar patrones y su integración. P1: necesarios para el
MVP comprometido, se pueden empezar con otro integrante sin frenar State/Strategy.

## Reparto propuesto: cuatro frentes con propiedad clara

| Integrante | Propiedad principal | React propio | Entrega y coordinación |
|---|---|---|---|
| 1 | C01: strategy/*, OrdenCompra/detalles, repositorios y CompraService/API | ComprasPage y servicio HTTP específico | Acuerda StockService con 2 y consulta de precios con 3; no modifica PedidoService |
| 2 | C02/C03: Stock/MovimientoStock, inventario, recepción e integración en PedidoService | StockPage y RecepcionPage | Único editor de PedidoService durante esta fase; coordina actualización de orden con 1 |
| 3 | C04/C05: ProductoProveedor/historial y gestión Oferta/DetalleOferta/API | OfertaPage y PreciosPage | Reutiliza entidades Oferta existentes; acuerda DTOs con 1 y cambios de esquema antes de SQL |
| 4 | C06/C07: Factura/Pago/reportes; integración de navegación y contratos compartidos | FacturacionPage, ReportesPage y menú en OperacionesPage | Único editor del shell, services/http.ts y SecurityConfig para integrar permisos acordados; valida demo final |

Cada integrante escribe tests de su frente y documenta su patrón/capa. No delegar
todas las pruebas al integrante 4. Revisiones cruzadas: 1↔2 y 3↔4; Strategy revisado
por otro integrante que pueda explicar las dos variantes. Las entidades/repositorios
compartidos se modifican con su propietario, no simultáneamente en ramas distintas.
Integrante 4 puede preparar contratos y facturación desde ahora; reportes de compras
se completan cuando C01 esté integrado. Si se satura el frente 2, repartir recepción
con 1 después de estabilizar Strategy, anunciando primero el cambio de propiedad.

## Contratos que deben acordar antes del código dependiente

1. Stock: si disponible ya excluye reservado, instante de reserva y tratamiento
   de falta parcial. Evitar descontar dos veces disponible y reservado.
2. Demanda: pedidos/estados incluidos, órdenes de compra abiertas y cómo evitar
   volver a comprar el mismo faltante. Acordar cálculo y proveedor, sin inventarlo.
3. Quinteros: solicitud/aprobación de Gerencia y cuándo se materializa la orden.
4. Recepción: total o parcial, repetición y excedentes.
5. Precios: historial inmutable/vigencias/corrección; necesita cambio de BD revisado.
6. Oferta: edición/publicación y selección explícita de oferta. Ya existen fechas
   inclusivas en zona Argentina y precios históricos en pedido: conservar garantía.
7. Pagos: total/parcial, sobrepago, numeración y base de totales de ventas/compras.
8. Recorte académico: validar con profesor exclusión de rentabilidad/reportes
   ampliados; no existe evidencia de conformidad docente.

Estos acuerdos son decisiones pendientes, no reglas que ya estén implementadas.
No hace falta rehacer login ni ampliar gestión general de usuarios para la demo.

## Orden recomendado para llegar al jueves 29/10

1. Integrantes 1 y 2 fijan contrato stock/compras y construyen Strategy + stock;
   3 prepara vínculo/precios y oferta; 4 factura interna y navegación necesaria.
2. Integrar una compra por demanda con faltante y otra Quinteros sin pedido.
3. Conectar recepción, reserva y State sin duplicar movimientos.
4. Ensayar una demo corta: crear pedido → salto inválido → compra/recepción →
   transiciones válidas → factura/pago; luego Quinteros y resumen Gerencia.

Es orden de ejecución, no estimación garantizada de tiempos. No declarar completado
un módulo por tener solo entidades o una pantalla. Mostrar qué persiste y qué regla
rechaza el backend. Sin importación Excel/OCR, pagos online, emails automáticos o
despliegue público como trabajo adicional para demostrar los patrones.
