# Reglas de negocio de compras

Fecha: 27/10/2026. Fuente: solicitud de módulo Strategy y dos aprobaciones
expresas del usuario en este chat. Estas reglas concretan C01; no modifican State.
Estado actual: migración 002 aplicada con autorización; circuito local verificado.
Reglas CMP-01 a CMP-11 conservadas. Ver reportes/2026-10-27-cierre-compras-pr5.md.

| ID | Regla aprobada / contrato | Implementación y verificación |
|---|---|---|
| CMP-01 | Disponible significa stock libre. No restar cantidad_reservada nuevamente. Una fila ausente aporta cero. | StockAbastecimiento; CompraPorDemandaStrategy; CompraStrategyTest |
| CMP-02 | Demanda consolida únicamente pedidos CONFIRMADO seleccionados explícitamente por Operaciones. | CompraService; CompraServiceTest |
| CMP-03 | Operaciones elige un proveedor activo. Cada producto a comprar debe estar activo y tener vínculo/precio válido en producto_proveedor para ese proveedor. | CompraService; CompraServiceTest |
| CMP-04 | Precio histórico del detalle se copia del servidor. Cantidades enteras positivas; productos/pedidos duplicados y mezclas de modos se rechazan. Total BigDecimal. Sin escrituras parciales. | Estrategias, CompraService, OrdenCompra; tests correspondientes |
| CMP-05 | Demanda descuenta stock libre y órdenes abiertas de ambos tipos. Solo EMITIDA y PENDIENTE legado aportan abastecimiento; una solicitud o autorización Quinteros todavía no emitida no aporta. | CompraService y CompraPorDemandaStrategy |
| CMP-06 | Cada pedido se procesa una sola vez. Un lote conserva pedidos y cobertura aun cuando el faltante sea cero; no se crea orden vacía. Ambos tipos registran su UUID en compras_coberturas (Quinteros sin asignaciones de demanda), garantizando unicidad global: repetir se rechaza 409. | CoberturaCompra, restricciones y CompraService |
| CMP-07 | Para no reutilizar stock ni órdenes entre distintos grupos, se guardan cantidades asignadas por producto/lote. Stock utilizable = máximo(0, disponible - asignaciones previas de stock). Abierto utilizable = máximo(0, órdenes abiertas - asignaciones previas de abierto). La asignación de abierto del lote incluye la compra nueva generada por ese lote. | CoberturaCompra y CompraService |
| CMP-08 | Quinteros recibe productos/cantidades sin pedidos y no descuenta stock: PENDIENTE_AUTORIZACION → AUTORIZADA por Gerencia → EMITIDA por Operaciones. Cada transición se permite una sola vez. | OrdenCompra, CompraService y permisos HTTP |
| CMP-09 | Conservar actor autenticado y fecha del servidor en solicitud/autorización/emisión. Las cuentas demo no se insertan como FK negativa a usuarios. | OrdenCompra; migración propuesta |
| CMP-10 | Lecturas/costos solo para Operaciones/Gerencia; contexto operativo y altas/emisión solo Operaciones; autorización solo Gerencia. Sesión/CSRF vigentes. | SecurityConfig y pruebas HTTP |
| CMP-11 | Serializar compras por productos en orden de ID y bloquear pedidos/órdenes al modificar. La unicidad de pedido y UUID es defensa adicional en BD. No se reserva ni se mueve stock. | CompraAbastecimientoRepository; repositorios y transacciones |

## Límite aprobado para la demo y dependencia de inventario

El usuario aprobó explícitamente conservar las asignaciones sin liberación
automática. No son reservas físicas: solo evitan que este módulo prometa dos veces
la misma cobertura. Antes de operar continuamente deben integrarse recepción,
consumo/cancelación y liberación con inventario. No borrar coberturas para reintentar
ni afirmar que State descuenta stock. El ledger es conservador si un proceso externo
recibe/cancela una orden o cambia el stock: puede comprar de más al conservar
asignaciones antiguas. No usar ese circuito externo durante esta demo aislada.

Cada selección de pedidos se cubre completa en un lote. Si el proveedor no abastece
todos los productos con faltante, se rechaza el lote completo; elegir pedidos
compatibles. No se divide un pedido entre proveedores en esta versión.

Las órdenes pendientes de autorización no son compromisos de abastecimiento:
pueden coexistir solicitudes Quinteros similares, pues son oportunidades manuales.
Emitirlas es una decisión explícita de Operaciones después de Gerencia.

## Esquema y decisiones históricas

Inspección JDBC de solo lectura: stock vacío, tres vínculos producto_proveedor,
órdenes vacías. CHECK original de estado: PENDIENTE/RECIBIDA/CANCELADA, varchar(20).
No hay columnas de autorización ni vínculo pedido–compra. Ese diagnóstico motivó la migración 002, aplicada posteriormente con autorización
según el reporte de aplicación. Este párrafo conserva el estado previo a migrar.

Actividad 2 exige Strategy real, órdenes de ambos tipos y precios históricos;
coincide con este incremento. El selector de perfiles del superprompt fue
sustituido por sesiones/CSRF y roles según DECISIONES_ARQUITECTURA.md. La autorización
manual Quinteros aprobada concreta la participación gerencial académica. Los
reportes/rentabilidad del MVP siguen fuera de este incremento y conservan su
discrepancia histórica documentada; no se los declara terminados.
