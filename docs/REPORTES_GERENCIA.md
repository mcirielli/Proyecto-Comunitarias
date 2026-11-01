# Reportes de Gerencia — movimientos por cliente

Panel **Reportes** del menú lateral, visible y accesible solo para el rol GERENCIA.

## Qué muestra

- Indicadores: ventas en pedidos, clientes con movimientos y, si Facturación está habilitada, facturado/cobrado/saldo.
- Resumen por cliente: pedidos, entregados, unidades, total, ticket promedio, facturado, cobrado, saldo y último pedido.
- Movimientos: cada pedido del período con estado, total, factura y cobrado.
- Filtros por período (fechas en hora de Argentina) y cliente. Exportación CSV del resumen.

## Reglas de cálculo

- Total de un pedido = suma de cantidad × precio histórico de sus detalles (todos los estados).
- Facturado/cobrado solo con `app.facturacion.enabled=true`; facturas ANULADA no suman importes.
- Saldo = facturado − cobrado. Sin rentabilidad ni márgenes.
- Solo lectura: no modifica pedidos, facturas ni pagos. No requiere migraciones.

## API

`GET /api/reportes/clientes?desde=AAAA-MM-DD&hasta=AAAA-MM-DD&clienteId=N` (todos opcionales).
Solo ROLE_GERENCIA (otros roles 403; resto de métodos denegados). Período invertido responde 400.

Implementación: `ReporteController` → `ReporteService` (Service Layer) → repositorios existentes.
Pruebas: `ReporteServiceTest`.

**Alcance:** DECISIONES_ARQUITECTURA.md limitaba la reportería a totales de ventas/compras.
Este panel incorpora reportes por cliente a pedido del equipo; registrar la decisión si se adopta.
