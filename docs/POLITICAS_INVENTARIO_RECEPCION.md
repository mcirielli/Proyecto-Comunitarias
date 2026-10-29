# Políticas de inventario y recepción

Fecha inicial: 27/10/2026, Argentina. Estado consolidado al 29/10/2026.
Estado al 29/10/2026: POLÍTICAS/MODELO Y SQL APROBADOS; 004 aplicada,
persistencia/concurrencia verificadas. Aceptación y compatibilidad con Tino en
[matriz de cierre](INVENTARIO_ACEPTACION.md).
El usuario respondió «Aprobar políticas y modelo propuestos» a la consulta agrupada
de esta sesión. Esa primera aprobación autorizó código y preparación de SQL;
la posterior «Autorizar 004 y validación real» autorizó la aplicación ya ejecutada.

## Fuentes y precedencia

Solicitud «Andy: stock, reservas y recepción», ambas entregas de context/entregas,
DECISIONES_ARQUITECTURA.md, CMP-01..11 y PEDIDO_STATE.md. Las aprobaciones de
Compras conservan vigencia hasta que se apruebe su evolución explícita aquí.
La aprobación histórica de 002 no autoriza 004. No se reejecuta 001 ni 002.
La Actividad 2 exige recepción, movimientos y cinco patrones reales; no define
reserva por pedido, recepción parcial ni cómo conciliar coberturas.

## Paquete mínimo solicitado

Las reglas siguientes fueron aprobadas como paquete. La tabla define su aceptación;
la evidencia real y sus límites se detallan al final. No confundir código compilado
con comportamiento comprobado en la base real.

| ID | Regla e invariante | Entidades/servicios | Transacción/constraint | Aceptación requerida | Legado |
|---|---|---|---|---|---|
| STK-01 | Disponible L significa libre físico y reservado R pertenece a pedidos. Existencia física F=L+R. L,R>=0; cantidades enteras positivas; rechazar overflow de L,R y F | Stock, StockService; StockAbastecimiento conserva lectura coherente | CHECK y aritmética exacta; una autoridad de escritura | Fila ausente consulta como cero sin INSERT; límites y saldos | No inventar saldos iniciales |
| STK-02 | Operaciones ejecuta acciones; Operaciones y Gerencia consultan stock/movimientos; Cliente sin acceso | StockController, SecurityConfig, DTO sin costos | Sesión/CSRF; autorización servidor | 401/403, CSRF y recarga | Mantener permisos de Tino |
| STK-03 | Cada movimiento conserva producto, cantidad, fecha y referencia a pedido u orden; operación única | MovimientoStock | Unicidad por orden/producto/INGRESO y pedido/producto/tipo; transacción común | Reintento/concurrencia produce un solo efecto | Movimientos existentes no se recrean |
| RSV-01 | CONFIRMADO no exige stock. Entrar EN_PREPARACION reserva todo el pedido o falla entero. Reservar q: L'=L-q, R'=R+q | ReservaPedido, líneas, PedidoService; State conserva validación polimórfica | Pedido bloqueado; productos ordenados; UNIQUE pedido/producto; reserva y estado atómicos | Dos pedidos compiten; falta última línea revierte todas | Sin reservas inferidas de totales agregados |
| RSV-02 | PREPARADO exige reserva propia completa. DESPACHADO consume esa reserva: R'=R-q, L'=L. ENTREGADO no mueve stock | Pedido/State, ReservaService | Consumo durable único; movimiento y estado en misma transacción | Saltos/reintentos rechazan sin efecto; fallo al registrar revierte | Un ENTREGADO histórico sigue consultable/facturable por contrato |
| RSV-03 | Si hay cobertura previa, reservar utiliza su compromiso recibido y stock asignado; no toma mercancía comprometida a otro pedido. Sin cobertura previa puede usar únicamente libre no comprometido | ReservaService, asignación efectiva | Locks de productos y asignaciones; F no cambia al reservar | A/B antes y después de recibir/reservar | Asignación ambigua requiere resolución previa |
| RSV-04 | Liberación solo técnica y trazada si un caso de uso autorizado la necesita; fallo transaccional hace rollback, no inventa movimientos compensatorios persistidos. Sin cancelación ni corrección manual | ReservaService | Sin endpoint de ajuste/liberación manual | Fallo no deja reserva parcial | Sin backfill implícito |
| REC-01 | Recepción total exacta una vez de orden EMITIDA, cantidades de sus detalles históricos. RECIBIDA repetida devuelve 409, permite recuperar resultado mediante GET. PENDIENTE legado se rechaza 409 | OrdenCompra.recibir, RecepcionService | Orden bloqueada; transición/ingresos/cobertura atómicos | Reintento y dos sesiones: un éxito y un conflicto; fallo revierte todo | No convertir PENDIENTE automáticamente |
| REC-02 | Recepción agrega q a L, registra INGRESO y transforma compromiso abierto a compromiso físico; orden deja de aportar abierto en la misma transacción | RecepcionService, asignación efectiva | UNIQUE movimiento y vínculo de origen; saldo/cobertura juntos | Una recepción no cuenta como abierto y stock simultáneamente | Compras demo EMITIDA son elegibles tras activar módulo; nunca se reciben al arrancar |
| REC-03 | Producto desactivado después de confirmar/emitir permite completar ese pedido/compra histórica. No reactiva producto ni permite nuevas altas incompatibles con reglas actuales | RecepcionService, ReservaService | No cambia activo/precios | Producto inactivo recibido/preparado con precios históricos | No altera reglas de oferta ni compra nueva |
| STK-04 | Preservar CMP-06: pedido y UUID nunca se procesan otra vez, aun despachado. Evolucionar CMP-07 con asignaciones efectivas durables por pedido/producto/origen | CompraService, ledger efectivo | Originales inmutables; auditoría de transferencias; unicidad de origen/evento | Cero faltante, grupos múltiples, recepción de orden de otro lote y no recompra | Habilitar solo tras reconciliación verificada |
| STK-05 | Pedidos avanzados históricos sin reserva no generan egreso retroactivo. Sin cambios de estado operativos sobre ellos hasta resolución específica; ENTREGADO permite lectura y facturación | PedidoService | Corte de activación documentado; validación explícita | Caso legado sin saldos/movimientos inventados | Un caso ENTREGADO observado; sin EN_PREPARACION/PREPARADO/DESPACHADO observados |

Fuente y fecha de cada regla: solicitud y aprobación agrupada del 27/10/2026.
Implementación persistida y activación local verificadas. Evidencia por caso en
INVENTARIO_ACEPTACION.md y el reporte de cierre; el flag general sigue apagado
para evitar habilitar otras bases sin las migraciones correspondientes.
STK-01 preserva CMP-01; REC-01 mantiene CMP-08; STK-04 cambia el mecanismo de
CMP-07 y el límite histórico sin liberación; esa evolución fue aprobada expresamente.

## Extensión de persistencia aprobada conceptualmente

Reutilizar stock y movimientos_stock. Agregar reserva persistida por pedido/producto
con cantidad y estado RESERVADA/CONSUMIDA, más trazabilidad de actor/fecha.
Agregar asignaciones efectivas por pedido/producto con origen STOCK u orden concreta,
cantidad y fase ABIERTA/FISICA/RESERVADA/CONSUMIDA. Una línea puede tener varios
orígenes. Guardar transferencias auditables; no borrar el ledger histórico de 002.
Cada transferencia conserva cantidad y origen; no reusa cantidades consumidas.

Conservar compras_cobertura_pedidos y UUID únicos. Leer saldos efectivos en Compras
solo al activar la nueva versión reconciliada: nunca sumar ledger viejo y nuevo.
Asignar primero stock no comprometido y luego órdenes por ID ascendente; repartir
a pedidos por ID ascendente y producto, hasta cubrir cantidades exactas. El faltante
se vincula a la nueva orden del lote. Recepción cambia solamente las asignaciones
de la orden recibida a FISICA. Reserva convierte compromisos propios a RESERVADA;
sus cantidades dejan de descontarse de L porque ya están en R. Despacho convierte
a CONSUMIDA, conservando protección contra recompra y auditoría.

Para cobertura antigua: preservar originales y preparar reconciliación comprobable
contra detalles y órdenes abiertas. No adivinar fuentes múltiples. Si no existe
reconstrucción unívoca, impedir nuevas escrituras del producto afectado hasta una
resolución específica aprobada, manteniendo consultas. La asignación observada de
dos unidades abiertas debe verificarse contra su pedido y orden antes de convertir.
No aprobar un backfill genérico ni inventar reservas de pedidos entregados.

004_stock_reservas_recepcion.sql incorporó CHECK/FK/UNIQUE, índices y RLS. Se
aplicó una vez con autorización específica, respaldo y permisos JDBC/secuencias
verificados. 003 pertenece a Tino: se verificó su presencia sin aplicarla de nuevo.
La aprobación para esta base no autoriza ejecutar scripts en otras bases.

## Contrato de cantidades y ejemplos de aceptación

Por producto: Ustock=max(0,L-compromisos FISICA); Uabierto=max(0,total abierto-
compromisos ABIERTA). Reservas y consumidos no se restan de L. No tolerar saldos
inconsistentes silenciosamente: conciliar antes de activar. Compras usa estos U
y Strategy existente para calcular faltante. Cantidades operadas y F se validan
con aritmética exacta; agregaciones intermedias deben detectar overflow.

Ejemplo de aceptación A/B cubierto por prueba persistente: L=4,R=0, orden O1 EMITIDA=6.
A pide 7: compromete 4 FISICA y 3 ABIERTA de O1, compra nueva=0.
B pide 5: quedan 0 de stock y 3 abiertos; compra O2=2. Compromisos abiertos=8,
distribuidos A/O1=3, B/O1=3, B/O2=2. Recibir O1: L=10,R=0, abierto=2;
compromisos FISICA A=7,B=3 y ABIERTA B=2. Reservar A: L=3,R=7;
compromiso físico restante B=3, stock utilizable para otro grupo=0.
Despachar A: L=3,R=0, F=3; A queda consumido. Recibir O2: L=5,R=0;
B queda comprometido por 5 y puede reservar: L=0,R=5. Entregar A no modifica
ningún saldo. C pide 1 y debe comprar 1 en todos los puntos donde no hay recurso
sin comprometer. Repetir abastecimiento de A/B se rechaza por CMP-06.

## Orden global de locks implementado

Pedidos existentes por ID ascendente (cuando el caso los modifica), luego productos
por ID ascendente, luego órdenes existentes por ID ascendente, luego asignaciones/
reservas afectadas. Una operación puede omitir niveles, nunca volver a uno anterior.
Recepción no bloquea pedidos: protege transferencias bajo los locks de productos.
Detalles de compras/pedidos son inmutables; se leen IDs antes, se bloquean productos,
y se revalida orden/estado después. Emisión actual bloquea orden antes que producto:
se adaptó al contrato al habilitar inventario. Compras no bloqueará órdenes
existentes después de tomar un orden inverso. Las filas nuevas de stock se crean
bajo lock de Producto existente y UNIQUE producto_id; bloquear una fila ausente
de Stock por sí solo no sirve. GET nunca inserta.

## Estado real de implementación y pruebas

- STK-01/STK-03, RSV-01/02/03/04, REC-01/02/03: Stock, ReservaPedido,
  MovimientoStock, RecepcionCompra, AsignacionInventario y servicios implementados
  detrás de app.inventario.enabled=false por defecto. Transacciones de aplicación
  y bloqueos reales en repositorios; recepción y reserva concurrentes verificadas
  con conexiones/transacciones distintas y sin saldos negativos.
- STK-02: permisos HTTP comprobados con servidor real y servicios mock. Cliente
  rechazado, Gerencia consulta sin recibir, Operaciones con CSRF; payload de
  cantidades manuales rechazado 400. Esto no prueba persistencia.
- STK-04: CoberturaInventarioService conserva originales, distribuye fuentes
  deterministas y consume fases sin doble descuento. Caso A/B verificado con
  repositorios reales y reconstrucción entre transacciones en Supabase.
- STK-05: se exige reserva propia para pasos avanzados; ENTREGADO previo permanece
  consultable. No se generaron reservas ni movimientos históricos.
- InventarioDomainTest: siete reglas de dominio. InventarioServiciosTest: siete
  casos (incluye A/B). AuthFlowTest: dos casos nuevos de inventario. Maven: 127
  pruebas estándar aprobadas en la primera ventana. La matriz posterior amplía
  InventarioSupabaseIT a quince escenarios y agrega verificación UTF-8 HTTP.
  Las cifras finales y comandos se registran en el reporte de cierre.
- 004 agregó tres tablas y dos índices de unicidad de movimiento. Backfill aprobado
  acotado a lotes de un pedido, cantidad abierta exacta y propia orden EMITIDA;
  aborta ante otras formas. Se verificaron dos unidades trasladadas, sin
  movimientos ni reservas históricas inventadas.
- Registro de recepción durable en inventario_recepciones (actor/fecha); sin
  agregar columnas a OrdenCompra, para conservar consultas de versiones sin 004.

Actualización posterior: usuario autorizó «004 y validación real». Se aplicó el
script exacto con respaldo/verificación y pasaron las siete InventarioSupabaseIT
más trece pruebas de regresión con inventario habilitado. La demo HTTP completó
recepción/reserva/consumo. El pedido #51 terminó ENTREGADO desde el navegador,
con saldo libre/reservado cero; Factura INT-PED-51 se emitió usando el código de
Tino sin modificar inventario. Ver INVENTARIO_ACEPTACION.md y el reporte de cierre.
No se declara MVP completo ni se autoriza merge o despliegue público.
