# Pedido y patrón State: primer incremento académico

27/10/2026. Objetivo: demostrar State real sobre un pedido persistido, reutilizando
MVC, Repository y Service Layer. Sin cambios de esquema ni dependencias nuevas.

## Participantes y responsabilidades

- Contexto: domain/Pedido, que persiste EstadoPedido y delega en PedidoState.
- Interfaz: state/PedidoState. Define codigo, siguiente y cambiarA.
- Estados concretos: RecibidoState, ConfirmadoState, EnPreparacionState,
  PreparadoState, DespachadoState y EntregadoState.
- EstadoSecuencial comparte rechazo de destinos que no coinciden con el sucesor;
  cada clase concreta define el siguiente objeto. Entregado no tiene sucesor.
- PedidoStates solo reconstruye comportamiento desde el enum persistido; su
  registro no contiene decisiones de transición.
- PedidoService coordina oferta, cliente, validaciones y repositorios; operaciones
  de escritura transaccionales. Repository bloquea pedido al cambiar estado para
  serializar cambios concurrentes sin agregar columna version.
- PedidoController recibe DTOs; React muestra respuesta real y errores de negocio.

El enum guarda datos, no implementa por sí solo State. El comportamiento aparece
en las clases concretas; Pedido no contiene un gran switch de transiciones.
El beneficio demostrable es impedir saltos/retrocesos y permitir reconstruir la
misma regla después de reiniciar la aplicación.

## Reglas del incremento

Flujo: RECIBIDO → CONFIRMADO → EN_PREPARACION → PREPARADO → DESPACHADO → ENTREGADO.
Solo un paso hacia adelante; repetir estado, retroceder o saltar devuelve 409.
Despacho/entrega registran hora del servidor. Fechas de oferta inclusivas en zona
America/Argentina/Buenos_Aires. El usuario selecciona explícitamente una oferta
publicada vigente; puede haber varias, sin selección implícita arbitraria.

Pedido requiere cliente activo y entre 1 y 100 productos distintos de la oferta,
activos y con cantidad positiva entera. No recibe precios del navegador: copia
precio_venta a pedido_detalle.precio_unitario y calcula total con BigDecimal.
Los precios históricos no cambian al modificar la oferta posteriormente.
No hay endpoint para editar detalles después del alta.

Cliente crea/consulta solo para su cliente asociado; cliente_id enviado por el
navegador no puede suplantar esa asociación. Operaciones puede seleccionar cliente,
crear/consultar pedidos y cambiar estados. Gerencia sin acceso a estas rutas.
Las escrituras mantienen CSRF y sesión. Sin SQL directo en servicios.

Las reservas y movimientos de stock están pendientes de integración: el recorrido
de estados actual es una demostración del ciclo, no acredita preparación/entrega
operativa completa con inventario. No se factura, compra ni descuenta stock en
este incremento. CANCELADO existe como opción del CHECK en Supabase, pero está
fuera del flujo de seis estados acordado; no se agregaron cancelaciones.

## API

| Método | Ruta | Uso |
|---|---|---|
| GET | /api/pedidos/contexto | Clientes autorizados y ofertas vigentes con productos/precios de venta |
| GET | /api/pedidos | Operaciones todos; Cliente solo propios |
| POST | /api/pedidos | ofertaId, clienteId para Operaciones y detalles de productoId/cantidad |
| PATCH | /api/pedidos/{id}/estado | Solo Operaciones; estado destino |
| POST | /api/demo/pedidos | Solo Operaciones y modo demo; prepara fixtures explícitamente |

Ejemplo de creación: {"ofertaId":1,"clienteId":1,"detalles":[{"productoId":1,"cantidad":2}]}.
Cliente puede omitir clienteId. Respuesta incluye detalles, precios históricos,
total, estado y siguienteEstado calculado por el objeto State del backend.

## Datos demo y recorrido

En la vista Pedidos, Operaciones pulsa Preparar datos de ejemplo. Se crea un
Cliente separado llamado Kiosco Demo UADE State y una Oferta Demo UADE State
vigente siete días desde hoy, publicada, con hasta tres productos activos y
precio de venta ficticio ARS 1000 cada uno. No cambia productos/precios/ofertas
previos ni crea pedidos automáticamente. Si existen fixtures vigentes se reutilizan.
No se ejecuta carga automática al arrancar ni se modifica tabla usuarios.

La cuenta cliente@demo.local, identidad -1, se vincula a ese Cliente por nombre
reservado solo cuando el modo demo está activo; no persiste el ID negativo como FK.
Ambigüedad o inexistencia de esa asociación devuelve conflicto y no elige un
cliente comercial arbitrariamente. Fuera de demo se usa clientes.usuario_id.

1. Operaciones prepara fixtures y abre Nuevo pedido para ese kiosco y oferta.
2. Alternativamente, Cliente inicia sesión y crea su pedido con cantidades.
3. Operaciones ve recibido y prueba ENTREGADO directamente: el backend rechaza.
4. Avanza por los cinco pasos válidos y muestra fechas al despachar/entregar.
5. Recarga; el pedido y su estado siguen persistidos. Cliente consulta sin poder
   cambiar estado ni ver pedidos de otros clientes.

## Verificación académica

PedidoStateTest cubre todas las 36 combinaciones y fechas del contexto;
PedidoServiceTest cubre precio servidor, cantidad, duplicados, vigencia, activo,
suplantación y filtrado por cliente. PedidoSupabaseIT crea datos ficticios con
rollback, cambia precio de oferta y reconstruye cada estado tras limpiar caché.
La ejecución y resultados finales se registran en reportes/ULTIMO.md.
Diagrama de clases: diagramas/pedido-state.puml.
