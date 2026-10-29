# Aceptación de inventario, reservas y recepción

29/10/2026. Matriz del incremento Andy; cierre final documentado en reportes/.
Fuente de reglas: POLITICAS_INVENTARIO_RECEPCION.md. No redefine CMP/FAC/PAG.

| Regla/caso | Evidencia ejecutable |
|---|---|
| STK-01: stock libre/reservado, existencia y overflow | InventarioDomainTest; InventarioSupabaseIT.desbordamientoDeRecepcionRevierteOrdenYRegistro |
| STK-01: fila ausente es cero; GET no crea | InventarioSupabaseIT.consultaDeFilaAusenteEsCeroSinCrearStock |
| STK-02: roles, CSRF, sesión y JSON UTF-8 | AuthFlowTest.inventarioConservaRolesYCsrf, erroresDeSesionYLoginSonJsonUtf8; HTTP real y navegador Cliente/Gerencia |
| STK-03: unicidad y trazabilidad producto/pedido/orden/fecha/actor | InventarioSupabaseIT.recepcionReservaDespachoEntregaYReintentoPersisten; índices 004; HTTP de reintento |
| RSV-01: reserva completa o rollback de todas las líneas | InventarioSupabaseIT.faltanteEnSegundaLineaRevierteLaPrimera |
| RSV-01: dos pedidos no reservan la misma unidad | InventarioSupabaseIT.dosPedidosNoReservanLasMismasUnidades, conexiones distintas |
| RSV-02: reserva propia, preparación, consumo único y entrega sin efecto adicional | InventarioServiciosTest.preparadoSinReservaPropiaNoEsValido, reservaYDespachoNoSeRepiten; InventarioSupabaseIT.recepcionReservaDespachoEntregaYReintentoPersisten |
| RSV-02/04: fallo revierte saldo, reserva y estado juntos | InventarioSupabaseIT.fallaMovimientoRevierteSaldoReservaYEstado, falloAlDespacharRevierteConsumoSaldoYEstado |
| RSV-03/STK-04/CMP-06/07: A y B compiten por stock/órdenes; recepción y reserva no duplican cobertura | InventarioSupabaseIT.conciliaDosGruposSinReutilizarNiDescontarDosVeces; InventarioServiciosTest.gruposCompitenAntesDespuesDeRecibirReservarYDespachar |
| REC-01: total exacta, reintento y recepción concurrente única | InventarioSupabaseIT.dosRecepcionesEnSesionesDistintasIngresanUnaVez; HTTP payload arbitrario 400 y repetición 409 |
| REC-01: PENDIENTE legado rechazado sin conversión | InventarioSupabaseIT.ordenPendienteLegadaNoSeConvierteNiSeRecibe |
| REC-01/CMP-08: Quinteros requiere autorización y emisión | InventarioSupabaseIT.quinterosSoloSeRecibeDespuesDeAutorizarYEmitir; demo HTTP de orden #18 |
| REC-02: fallos de ingreso/varias líneas revierten orden, stock y movimiento | InventarioSupabaseIT.falloDeIngresoRevierteOrdenRecepcionYStockAusente, recepcionDeVariasLineasNoDejaIngresosParciales |
| REC-03: producto inactivo histórico completa recepción/preparación/entrega sin reactivación | InventarioSupabaseIT.productoDesactivadoCompletaRecepcionYDespachoHistoricos |
| STK-05: EN_PREPARACION/PREPARADO/DESPACHADO legados sin reserva no inventan movimientos; ENTREGADO consultable | InventarioSupabaseIT.pedidosAvanzadosLegadosNoInventanReservasNiEgresos |
| Compatibilidad FAC-01: ENTREGADO puede facturarse sin modificar Pedido/inventario | Navegador: pedido #51 → INT-PED-51. HTTP real: reemitir conserva ID #12 y movimientos/saldos; prueba conjunta en copia Andy + Tino |

## Recorrido y balances reales

| Paso | Producto #2 libre | Reservado | Resultado |
|---|---:|---:|---|
| Demo HTTP inicial, demanda #17 recibida | 2 | 0 | Pedido #50 por dos unidades |
| Reserva del pedido #50 | 0 | 2 | EN_PREPARACION |
| Despacho/entrega de #50 | 0 | 0 | Un egreso de dos; ENTREGADO sin segundo descuento |
| Quinteros #18 autorizada, emitida y recibida | 3 | 0 | Ingreso total de tres |
| Pedido #51 por cuatro, cobertura y orden #19 de una | 3 | 0 | Compra solo faltante; preparación insuficiente rechazada antes de recibir |
| Recepción #19 | 4 | 0 | Ingreso único de una unidad |
| Reserva #51 | 0 | 4 | Reserva propia completa |
| PREPARADO → DESPACHADO → ENTREGADO desde navegador | 0 | 0 | Un egreso de cuatro; pedido finalizado |
| Emisión/reintento de INT-PED-51 con Tino | 0 | 0 | Factura #12, ARS 4000; mismo ID al reemitir; inventario inalterado |

Los dos pedidos conservan precios históricos. La factura #12 queda PENDIENTE sin
pagos nuevos: esta prueba acredita la compatibilidad de entrega/facturación y no
registra dinero ni prueba una transferencia bancaria. Factura/Pago permanecen
idénticos a la rama de Tino en la copia de validación.

## Ejecución y límites

- Andy se verifica contra main da976fc. La validación conjunta utiliza una copia
  local separada con origin/tino cdd6880. No incorpora esos commits a Andy ni
  fusiona PRs a main.
- Las pruebas de inventario real mantienen app.inventario.enabled=true; no se
  apaga inventario para acreditar el circuito. Fixtures con UUID propios y limpieza
  por IDs; concurrencia mediante transacciones/conexiones distintas.
- 003 y 004 ya estaban aplicadas. En el cierre no se ejecuta SQL de migración.
- Facturación conserva su suite de Tino. Sus fixtures unitarios/de dominio no
  sustituyen la demo conjunta, que usa el pedido real #51 y sus reservas/egreso.
- Sin cancelaciones, recepción parcial, excedentes, ajustes manuales o reversiones
  de despacho. Esos comportamientos quedaron fuera del incremento aprobado.
- La aceptación técnica no equivale a revisión humana, merge, publicación de
  producción ni cierre de todos los frentes del MVP académico.
