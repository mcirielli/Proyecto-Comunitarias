# Pedido + State con persistencia real

Estado: CERRADO (incremento académico; MVP completo aún pendiente).
Fecha: 27/10/2026 America/Buenos_Aires. Objetivo próximo: mostrar patrones el jueves 29/10.

## Alcance autorizado y evidencia inicial

Usuario priorizó patrones y autorizó iniciar Pedido + State. Se revisaron tablas
compartidas, instrucciones, código y decisiones existentes. Inspección JDBC real
de clientes/ofertas/oferta_detalle/pedidos/pedido_detalle confirmó columnas y FK,
cantidades >0, precios >=0, unicidad de producto por pedido y CHECK de estados.
Pedidos inicialmente sin registros. CHECK admite CANCELADO además de los seis
estados acordados; cancelación no se implementó por falta de regla aprobada.
No hubo DDL ni nuevas dependencias. Cambios locales preexistentes preservados.

## Implementación

- Pedido, DetallePedido, Cliente, Oferta y DetalleOferta mapean tablas existentes.
- PedidoState, EstadoSecuencial, seis clases concretas y PedidoStates reconstruyen
  comportamiento desde enum persistido. Cada estado concreto define sucesor.
- PedidoService crea pedidos copiando precio de oferta, valida cantidades,
  productos únicos/activos y oferta publicada vigente; filtra por cliente y
  cambia estado delegando al contexto. Lock pesimista al cambiar para evitar
  validaciones concurrentes sobre el mismo estado previo.
- DTOs/API y errores 400/404/409. Seguridad limita cambios a Operaciones y pedidos
  a Cliente/Operaciones. CSRF se conserva, Gerencia no hereda permisos.
- PedidoDemoService/Controller crean fixtures únicamente por acción de Operaciones
  con modo demo activo. Cuenta Cliente -1 se vincula a cliente demo separado por
  nombre reservado; identidad negativa nunca se usa como FK. Fuera de demo se
  usa clientes.usuario_id. Sin tocar cuentas de usuarios ni datos previos.
- PedidosPage permite alta, consulta, detalle/precios, siguiente permitido y
  selección de destino para demostrar rechazo del backend; resumen actualizado.
- PEDIDO_STATE.md explica reglas/patrón/recorrido; diagramas/pedido-state.puml
  contiene UML fuente. No se ejecutó render PlantUML.

## Verificaciones observadas

1. Maven local `mvn -o -f backend/pom.xml test`: **71 aprobadas**.
   PedidoStateTest ejecuta las 36 combinaciones (5 válidas y 31 inválidas), más
   fechas/delegación del contexto; PedidoServiceTest nueve reglas.
2. Integración explícita `mvn -o -f backend/pom.xml
   -Dtest=PedidoSupabaseIT,SupabaseJdbcIT,MaestrosSupabaseIT test`:
   **6 aprobadas**, 0 errores/fallos/omitidas. Datos ficticios con rollback.
   PedidoSupabaseIT limpia EntityManager entre transiciones, verifica clase
   EntregadoState reconstruida y conserva precio histórico incluso después de
   cambiar precio_venta de la oferta dentro de esa transacción de prueba.
3. Se añadió prueba HTTP Cliente PATCH con CSRF válido ->403 y Gerencia GET
   pedidos ->403; mensajes de JSON inválido se manejan explícitamente como 400.
4. Build completo Compose con clean verify dentro de imagen: **72 aprobadas**,
   BUILD SUCCESS. Frontend TypeScript/Vite aprobado, backend Healthy/frontend
   iniciado. Log .cache/pedido-compose.log. Rebuild final solo frontend actualiza
   resumen y enlace al módulo, sin reiniciar backend.
5. Smoke real Python HTTP contra Nginx -> Spring -> Supabase:
   - Operaciones prepara fixtures, respuesta 204.
   - Cliente crea pedido **#2**, precio servidor ARS 1000 x 2, total ARS 2000.
   - Suplantar cliente_id rechazado 409; Cliente PATCH estado ->403.
   - Operaciones RECIBIDO -> ENTREGADO rechazado 409; GET confirma RECIBIDO.
   - Cinco transiciones válidas ->200, fechas despacho/entrega presentes y
     consulta Cliente confirma ENTREGADO; retroceso ->409; estado desconocido ->400.
   - Cliente crea pedido **#3**, queda RECIBIDO para la presentación.
   - Gerencia GET pedidos ->403. Script .cache/smoke-pedido.py, sin credenciales
     reales guardadas; solo cuentas ficticias ya documentadas.
6. Navegador: login real Operaciones, navegación Pedidos y captura visual con
   pedido #3 recibido y #2 entregado. Intento posterior de selección por locator
   no encontró control tras desconexión de pestaña; no se afirma ejecución de la
   transición por UI. El mismo rechazo fue probado por HTTP real y tests.
7. git diff --check sin errores, avisos LF/CRLF habituales. Sin commit/push/PR.

## Datos y límites

Se dejaron fixtures demo explícitos (cliente y oferta separados), pedido #2
entregado y #3 recibido. No se modificaron pedidos previos ni productos/precios
comerciales existentes. Los fixtures reutilizan hasta tres productos activos,
precio de venta ficticio y vigencia actual; no son importación de datos reales.

State implementado y demostrado no significa integración logística completa:
stock/reservas, abastecimiento/Strategy, facturas/pagos siguen pendientes.
Transiciones todavía no mueven inventario; la pantalla lo informa. Publicación
general de ofertas también está pendiente: este módulo usa ofertas existentes
o fixture explícito, no implementa todo el newsletter.
Revisión del agente principal; revisión cruzada del equipo pendiente.

## Próximo prompt

Arrancar con los tres Compose de DEMO_LOCAL.md, ingresar como Operaciones y abrir
Pedidos. Usar #3 para mostrar salto inválido y avance válido. Cada participante
debe poder explicar contexto, interfaz, estados concretos, reconstrucción y tests.
El siguiente patrón prioritario es Strategy: acordar cálculo de faltantes, stock
y proveedor, luego implementar estrategias demanda/Quinteros con pruebas reales.
No modificar esquema ni publicar cambios sin el alcance correspondiente.
