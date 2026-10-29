# Facturación interna y registro de pagos

Incremento C06, rama remota tino, sobre main actualizado da976fc. Java 21,
capas MVC/Repository/Service Layer, sin nuevos patrones ni dependencias.
Reglas aprobadas y trazabilidad: [políticas FAC/PAG](POLITICAS_FACTURACION_PAGOS.md).

## Responsabilidades

Factura/Pago mapean tablas existentes. FacturacionService emite desde pedido
entregado sin mutarlo y calcula importes desde su historia; PagoService coordina
saldo, idempotencia y estado con transacciones/locks. DTOs evitan exponer JPA.
FacturacionAcceso conserva asociación de Cliente y separación de roles.
FacturacionPage presenta emisión, detalle, historial y registro manual por rol.
El dinero no se procesa ni se envía a terceros. Comprobante interno sin validez fiscal.
Ningún controlador acepta total de factura/saldo/propietario calculado por navegador.

## API

| Método | Ruta | Perfil y comportamiento |
|---|---|---|
| GET | /api/facturas/disponibilidad | Tres roles; flag de entorno, no consulta de BD |
| GET | /api/facturas/contexto | Operaciones; pedidos ENTREGADO sin factura |
| GET | /api/facturas | Operaciones/Gerencia todas; Cliente solo propias |
| GET | /api/facturas/{id} | Detalle, saldo e historial, con mismo filtro |
| POST | /api/facturas | Operaciones; {pedidoId}; 200 con factura nueva o existente |
| GET | /api/pagos?facturaId={id} | Tres roles, previa autorización de factura |
| POST | /api/pagos | Operaciones; registra o recupera reintento; 200 |

Pago: {"facturaId":1,"claveSolicitud":"UUID estable de la operación",
"importe":"10.25","medioPago":"TRANSFERENCIA","referencia":"opcional"}.
Usar IDs reales del entorno, no los de ejemplo. 400 entrada inválida, 404 recurso
inexistente/no visible, 409 conflicto de negocio/integridad, 401 sesión, 403 rol/CSRF.
Saldo y estado pueden cambiar por otros pagos: refrescar la factura tras un POST.
La respuesta idempotente identifica el mismo pago, no promete el saldo de ese instante.

## Esquema y habilitación

Se inspeccionó esquema real antes de mapear: facturas con UNIQUE pedido/numero,
CHECK importe>=0 y estados PENDIENTE/PAGADA/ANULADA; pagos con importe>0 y medios
EFECTIVO/TRANSFERENCIA/TARJETA. Ambos numeric(12,2), RLS activo y permisos JDBC.
003_facturacion_pagos.sql agrega UUID nullable + UNIQUE en pagos, para conservar
legado sin duplicar cobros de API nueva. Se aplicó con autorización específica en
la base local configurada. No reejecutarla ni aplicar 001/002 para este módulo.
Otra base requiere su propia inspección y aprobación. ddl-auto=none y sql.init.mode=never.

Activación opt-in FACTURACION_ENABLED=true; por defecto apagado. Compose:

```powershell
docker compose -f compose.yaml -f compose.supabase.yaml -f compose.demo.yaml -f compose.compras.yaml -f compose.facturacion.yaml up -d --build
```

Requiere credenciales locales de esa base y migraciones aprobadas. El flag no es
una comprobación automática del esquema; no habilitar en bases sin 003.
No publicar modo demo. Para convivir con Andy, configurar puertos y nombre de
proyecto distintos; no ejecutar dos proyectos sobre los mismos puertos.

## Demo

1. Operaciones abre Facturación y pagos y elige un pedido ENTREGADO propio de la
   demostración. Si hay inventario integrado, completar su recorrido real; no
   saltar estados ni desactivar reglas para facturar.
2. Emite INT-PED-{id}; total de detalles históricos del servidor.
3. Registra parcial: queda PENDIENTE y saldo disminuye. Registra resto: PAGADA.
4. Actualiza: factura/pagos siguen persistidos. Mismo UUID reintentado no duplica.
5. Cliente asociado ve ese comprobante y sus pagos sin altas ni acceso a ajenos.
   Gerencia consulta sin emitir ni registrar pagos.

Fixtures separados y reversibles para IT; no alterar pedidos comerciales para
conseguir una factura. Los pedidos entregados legados no se retroceden ni se les
inventa stock. La cuenta demo usa la asociación documentada en PEDIDO_STATE.md.

## Verificación y coordinación

FacturaTest, FacturacionServiceTest, PagoServiceTest y FacturacionHttpTest cubren
reglas, identidad, roles y CSRF sin BD. FacturacionSupabaseIT verifica persistencia,
precio histórico, cero, rollback y concurrencia con transacciones separadas;
limpia únicamente los fixtures propios que debe confirmar para probar carreras.
Regresión de compras/ofertas/maestros/State preservada. No se modifican Pedido,
PedidoService, state/*, strategy/*, stock o recepción, propiedad de Andy.

Reporte: reportes/2026-10-28-facturacion-pagos-tino.md. No es cierre del MVP:
inventario integrado y los demás frentes siguen según backlog de main actual.

UML: [fuente PlantUML](diagramas/facturacion-pagos.puml) y [SVG](diagramas/facturacion-pagos.svg).
