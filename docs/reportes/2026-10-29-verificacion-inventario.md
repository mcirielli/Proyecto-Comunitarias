# Verificación del cierre de inventario Andy

Fecha: 29/10/2026, America/Buenos_Aires. Solicitud: comprobar si se terminaron todos
los cambios planeados. Resultado: NO está completado el cierre de todo el plan.
Este reporte actualiza el estado posterior a la ventana inicial de preparación;
no sustituye ni reescribe sus aprobaciones históricas.

## Implementado y verificado

- Stock libre/reservado, movimientos, reserva completa por pedido, consumo único,
  recepción total de EMITIDA y ledger durable por pedido/producto/orden.
- Políticas/modelo aprobados por el usuario, y luego autorización expresa
  «Autorizar 004 y validación real». SQL 004 aplicado el 28/10, hash SHA256
  54d2eba3baf035bdfabbb9f33df0f20c154ebc65fa5881ac9b1d4ec6381575dc.
  No reejecutar. Se conservaron los originales y se trasladaron dos unidades de
  cobertura demostrable a las asignaciones nuevas. Sin stock ni movimientos retroactivos.
- Respaldo public con pg_dump: 56151 bytes, catálogo pg_restore --list de 162 líneas,
  en .cache/inventario-before-004.dump/list, fuera de Git. Se comprobó catálogo;
  no se hizo un ensayo de restauración completa en otra base.
- Tablas inventario_recepciones, reservas_pedido e inventario_asignaciones,
  constraints/índices/RLS y permisos JDBC de tabla/secuencia verificados por el runner.
- Maven Java 21: 127 pruebas estándar aprobadas. Siete InventarioSupabaseIT reales
  aprobadas, incluidas recepción/reserva concurrentes y rollback de movimientos,
  saldo y estado. Regresión Supabase: 13 aprobadas con inventario habilitado.
  PedidoSupabaseIT se adaptó con stock propio y autenticación, manteniendo rollback;
  no se apagó inventario para acreditar el circuito.
- Frontend TypeScript/Vite y build Docker backend/frontend aprobados. Confirmación
  accesible de recepción, detalles de cantidades, textos de integración y próxima
  transición corregidos tras observaciones del navegador.
- UML renderizado e inspeccionado. Navegador: saldos/movimientos persistidos, temas
  claro/oscuro y ancho 390px, sin desborde documental (375 <= 390). No confundir
  esta revisión con finalización de todos los recorridos de aceptación.

## Evidencia de demo y comprobación actual

La demo HTTP previa completó pedido #50 ENTREGADO, demanda #17 RECIBIDA y Quinteros
#18 RECIBIDA, producto #2. Generó cuatro movimientos; saldo al terminar ese recorrido:
libre 3 y reservado 0. Comprobó falta de stock, reintentos, roles/CSRF, recepción
manual arbitraria rechazada y Quinteros no elegible antes de emisión.

El recorrido de navegador posterior creó pedido #51 de cuatro unidades. Compras
utilizó las tres libres y generó orden #19 de una unidad. Se comprobó rechazo de
preparación sin abastecimiento; la orden quedó recibida y se inició preparación.
Hubo problemas de automatización con window.confirm, sustituido por un dialog HTML
accesible; un reintento devolvió 409 sin duplicar ingreso. No se acreditó terminar
PREPARADO → DESPACHADO → ENTREGADO de ese segundo pedido.

Consulta JDBC de solo lectura del 29/10, transacción revertida:

| Dato | Resultado actual |
|---|---|
| Pedido #50 | ENTREGADO |
| Pedido #51 | EN_PREPARACION |
| Órdenes #17, #18 y #19 | RECIBIDA |
| Producto #2 | Libre 0, reservado 4 |
| Tablas nuevas | 4 asignaciones, 2 reservas, 3 recepciones |

No se modificaron datos para hacer parecer finalizado el recorrido. Los contenedores
de comunitarias-inventario-check no estaban ejecutándose al consultar Docker el
29/10. La ejecución anterior utilizó exclusivamente loopback 8082/8083.

## Publicación y pendientes

PR #6: https://github.com/mcirielli/Proyecto-Comunitarias/pull/6, andy → main,
abierta, en borrador y sin fusionar. En la comprobación inicial su head era c41aec3;
había seis archivos modificados y inventario.css nuevo sin publicar. También la
descripción de PR y los documentos aún presentaban 004 como pendiente, aunque ya
había sido autorizada y aplicada. Se preparan correcciones de publicación/documentación
sin alterar el SQL aplicado ni volver a ejecutar migraciones.

Falta completar y acreditar el segundo recorrido de navegador, ampliar la matriz
de aceptación donde no haya prueba concreta (incluidos casos históricos/inactivos
de extremo a extremo), integrar/verificar Factura/Pago de Tino con ENTREGADO y cerrar
la revisión final. No se declara inventario/MVP completo ni demo conjunta aprobada.
No hay autorización para merge ni despliegue público.

Logs locales: inventario-aplicacion-004.log, inventario-supabase-it.log,
inventario-regresion-supabase.log, inventario-verify-final.log,
inventario-frontend-final.log e inventario-verificacion-cierre.log. Las cifras
anteriores provienen de ejecuciones observadas, no de inferencias sobre el código.
