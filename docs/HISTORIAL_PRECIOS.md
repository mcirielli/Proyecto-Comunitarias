# Historial de precios de productos (Operaciones)

En **Productos**, cada fila tiene **Ver precios**, que abre el historial del producto arriba del listado.

## Qué muestra

- Precio de venta vigente (oferta publicada cuya vigencia incluye hoy, hora Argentina).
- Último precio de compra (orden de compra más reciente no CANCELADA).
- Proveedores vinculados y su precio de compra actual (`producto_proveedor.precio_compra`).
- Precios de venta por oferta semanal publicada y precios de compra por orden, con variación % respecto del registro anterior.

## Decisión

El historial se arma con precios ya persistidos en `oferta_detalle` y `orden_compra_detalle`:
**sin tabla nueva ni migraciones** en el Supabase compartido. Solo lectura.
Una tabla con vigencias por proveedor (RF-05 completo) sigue pendiente de aprobación del equipo.

## API

`GET /api/productos/{id}/precios` — solo ROLE_OPERACIONES (regla existente de `/api/productos/**`).
Producto inexistente → 404.

Implementación: `HistorialPreciosController` → `HistorialPreciosService` → `HistorialPreciosRepository` (consultas JPQL).
Pruebas: `HistorialPreciosServiceTest`.
