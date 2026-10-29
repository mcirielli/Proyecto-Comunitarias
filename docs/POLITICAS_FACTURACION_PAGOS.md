# Políticas de facturación interna y pagos

Fecha 27/10/2026, America/Argentina/Buenos_Aires. Usuario aprobó expresamente en
este chat la propuesta completa y luego autorizó únicamente migración 003.
Las reglas siguientes están implementadas; el reporte final distingue verificaciones
observadas de escenarios pendientes. No son reglas fiscales ni procesan dinero.

| ID | Política aprobada | Participantes / garantía | Prueba |
|---|---|---|---|
| FAC-01 | Solo pedido persistido ENTREGADO, con detalles; no cambia Pedido/State | Factura, FacturacionService; lock del pedido | FacturaTest, FacturacionServiceTest |
| FAC-02 | Una factura por pedido. Número INT-PED-{pedidoId}, interno y estable; reemitir devuelve la existente | FacturacionPedidoRepository, UNIQUE pedido_id/numero existentes | FacturacionSupabaseIT emisión concurrente |
| FAC-03 | Total servidor desde cantidad × precio histórico de Pedido; ARS, sin impuestos, descuentos o intereses nuevos | Factura BigDecimal, numeric(12,2) | FacturaTest, FacturacionSupabaseIT historia |
| FAC-04 | Total cero válido: PAGADA sin pago ficticio; total fuera de numeric(12,2) rechazado | Factura y CHECK existente >=0 | FacturaTest, FacturacionSupabaseIT cero |
| FAC-05 | PENDIENTE con saldo; PAGADA al saldar; no estado PARCIAL nuevo | Factura.registrarAcumulado | PagoServiceTest, integración parcial/total |
| FAC-06 | Sin editar/anular/devolver en este incremento; ANULADA legado se consulta, no recibe pagos ni se reemplaza por otra factura | Sin endpoints de mutación; rechazo en PagoService | PagoServiceTest anulada |
| FAC-07 | Operaciones emite; Cliente consulta solo su asociación persistida/demo; Gerencia solo consulta | SecurityConfig, FacturacionAcceso, filtro por cliente | FacturacionServiceTest, FacturacionHttpTest |
| FAC-08 | Datos de comercio y nombres de productos consultados actuales; precios históricos. No snapshot fiscal de identidad/dirección | FacturaResponse; Pedido inmutable por contrato | Documentado; sin validez fiscal |
| PAG-01 | Pagos manuales positivos, máximo saldo; parciales permitidos y sin sobrepago | Pago, PagoService, BigDecimal y lock de factura | PagoServiceTest, FacturacionSupabaseIT concurrente |
| PAG-02 | EFECTIVO/TRANSFERENCIA/TARJETA son etiquetas manuales, sin conexión bancaria; referencia opcional hasta 100, trim y blanco→null | MedioPago, DTO y CHECK de BD | DTO/servicio; HTTP validación |
| PAG-03 | Fecha del servidor con Clock/Instant; UI muestra zona Buenos Aires | PagoService, PagoResponse, React | Clock fijo en pruebas |
| PAG-04 | UUID de solicitud obligatorio en API nueva; mismo contenido normalizado devuelve el mismo pago; distinto contenido devuelve 409 | Pago.coincide, lock factura, UNIQUE pagos.clave_solicitud | PagoServiceTest, IT clave concurrente |
| PAG-05 | Dos pagos legítimos iguales con UUID distintos son distintos; cada uno se valida contra saldo | PagoService, no deduplicación por importe/referencia | IT emisión/pago parcial/total |
| PAG-06 | Pago e impacto sobre estado atómicos; error revierte ambos. Concurrencia no supera saldo | @Transactional, lock pesimista; UNIQUE como defensa cruzada | FacturacionSupabaseIT rollback/concurrencia |
| PAG-07 | Solo Operaciones registra; Cliente/Gerencia consultan pagos vía factura autorizada. No pago de proveedor | PagoController, FacturacionAcceso | FacturacionHttpTest, prueba aislamiento servicio |

## Persistencia, legado y concurrencia

003 agrega solo clave_solicitud UUID y UNIQUE en pagos. La columna admite NULL
para registros previos: el DTO/constructor de API nueva lo prohíben. No se inventan
claves de reintento ni se deduplican pagos legados por referencia. Inspección inicial:
0 facturas/0 pagos. ANULADA y medios existentes se preservan; no se redefine CHECK.
La autorización de 002 no se reutilizó: 003 se autorizó específicamente y se aplicó
con transacción, instantáneas de metadatos y comprobación post-commit.

Emisión bloquea Pedido exclusivamente mientras busca/crea Factura. Pago bloquea
Factura y después comprueba UUID y saldo mediante SUM de pagos dentro de la misma
transacción READ COMMITTED. Mismo UUID en distintas facturas identifica contenido
diferente: UNIQUE de BD puede rechazar la carrera con 409 y rollback completo.
No se adquieren locks de productos/stock ni se llama a lógica de inventario.

Cliente se resuelve desde clientes.usuario_id; cuenta demo -1 se asocia solamente
por el nombre reservado existente y debe haber exactamente una coincidencia.
No se acepta clienteId libre del navegador. Consultar ajena devuelve 404.
No se refactoriza PedidoService para extraer su resolución privada porque es área
de Andy; este módulo conserva el mismo contrato mediante su resolver propio.

## Límites

La referencia comercial no es una clave de idempotencia. Ante respuesta incierta,
reintentar con la misma clave y contenido, o consultar historial antes de registrar
un hecho distinto. El formulario conserva la clave al fallar la llamada; no promete
recuperar claves tras cerrar navegador. No hay anulaciones ni correcciones de pagos;
requieren política y alcance posteriores, sin borrar historia en esta versión.
