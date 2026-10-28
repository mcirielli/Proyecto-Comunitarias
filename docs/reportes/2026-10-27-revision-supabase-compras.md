# Revisión de Supabase previa a migración de compras

Fecha: 27/10/2026. Alcance autorizado: usar credenciales locales para inspeccionar
esquema y permisos, sin modificar Supabase. No autoriza aplicar migración ni merge.

## Método

Conexión JDBC con variables leídas del .env local, SSL obligatorio y consultas
limitadas a metadatos y conteos agregados. Transacción confirmada con
transaction_read_only=on y finalizada mediante rollback. Sin DDL, DML ni pruebas
que creen fixtures. No se imprimieron credenciales, URL JDBC o identidad de cuenta.
Inspección auxiliar con JDK local 23 y driver PostgreSQL existente; no es una
ejecución de pruebas de la aplicación Java 21. Docker no estaba disponible.
La primera conexión falló por restricción de red; ejecución autorizada fuera de
esa restricción completó la revisión. Scripts y log ignorados en .cache/.

## Comparación con propuesta 002 de la rama andy

Revisado database/propuestas/002_compras_strategy.sql, blob
fa05b7621ab17f6fc110003f16a79468356288d7.

- ordenes_compra mantiene cinco columnas originales; estado es varchar(20) con
  CHECK que solo admite PENDIENTE, RECIBIDA y CANCELADA. Faltan los campos de
  solicitud/autorización/emisión previstos por el código de Andy.
- El CHECK de tipo admite DEMANDA_CLIENTES y STOCK_QUINTEROS. PK/FK de órdenes y
  detalle compatibles con la propuesta. Detalle exige cantidades positivas,
  precio numeric(12,2) >= 0 y producto único por orden.
- No existen las tres tablas compras_coberturas, compras_cobertura_pedidos y
  compras_cobertura_detalles, ni los dos índices propuestos. No se detectaron
  colisiones con esos nombres ni aplicación parcial de los objetos inspeccionados.
- Solo se encontró un CHECK de estado en ordenes_compra, que corresponde al que
  reemplaza la propuesta. No se encontró otro CHECK combinado que ese bloque
  pudiera retirar inadvertidamente en el esquema observado.
- Órdenes: 0; detalles de compra: 0. No hay datos existentes de compras que adaptar.
  Stock: 0 filas. Producto-proveedor: 3 vínculos activos con precio válido.
  Pedidos: 4. No se consultaron datos personales ni contenidos de pedidos.
- Cuenta JDBC: permisos de uso/creación en public, privilegio de propietario
  sobre tablas relevantes, SELECT/INSERT/UPDATE y uso de secuencias de compras.
  RLS activo en tablas existentes, no forzado; la cuenta tiene BYPASSRLS.
  No se observa bloqueo de permisos para la cuenta actual; los permisos y acceso
  a las tablas nuevas deben comprobarse después de crearlas. No se revisó ni
  alteró acceso frontend directo, que no forma parte del contrato de la aplicación.

## Conclusión y siguiente etapa

La estructura observada coincide con la base prevista por la migración. No se
detectó incompatibilidad estructural que impida proponer su aplicación. Esto no
certifica ejecución correcta del script ni persistencia/concurrencia de compras.

La migración no fue aplicada. Requiere autorización expresa según la propuesta,
el reporte de Andy y el alcance de esta revisión. Debe aplicarse únicamente 002
con compras deshabilitado, tras volver a comprobar la versión/esquema y preparar
respaldo. Después: verificar columnas, constraints, RLS y permisos resultantes;
habilitar compras localmente y ejecutar CompraSupabaseIT, regresión integrada con
ofertas, concurrencia/reintentos y recorridos HTTP/UI demanda y Quinteros.
Mantener PR #5 en borrador hasta cerrar esa evidencia; sin merge ni auto-merge.

Este archivo registra solo la inspección. No se actualiza ULTIMO.md, porque el
último incremento funcional cerrado continúa siendo oferta semanal.
