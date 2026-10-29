# Demo local de Comunitarias

## Circuito integrado de inventario — cierre del 29/10/2026

Stock, recepción y reservas ya están implementados y verificados. Ver
[matriz y balances](INVENTARIO_ACEPTACION.md) y
[cierre técnico](reportes/2026-10-29-cierre-inventario-andy.md).
Usar solo la base inspeccionada con 002/004 aplicadas y el override opt-in
compose.inventario.yaml; no ejecutar migraciones al arrancar ni cargar stock ficticio.

1. Cliente/Operaciones crea un pedido desde una oferta vigente y lo confirma.
2. Con faltante, Operaciones cubre demanda en Compras: se compra solo lo que no
   cubren stock libre no comprometido y órdenes abiertas no asignadas.
3. Inventario y recepción muestra órdenes EMITIDA y sus cantidades. Confirmar
   recepción total; recargar confirma saldo/movimiento y retira la orden elegible.
4. Iniciar preparación reserva todo o rechaza sin efectos parciales. PREPARADO
   requiere reserva propia; DESPACHADO consume una vez; ENTREGADO no vuelve a descontar.
5. Quinteros: solicitar, autorizar con Gerencia, emitir con Operaciones y recibir.
6. En la copia combinada con Tino, emitir factura del pedido ENTREGADO conserva
   el inventario y los precios históricos. Cliente/Gerencia consultan sin mutaciones.

Evidencia persistida: pedido #51 ENTREGADO, orden #19 RECIBIDA, producto #2 libre
0/reservado 0 y factura #12 INT-PED-51 por ARS 4000. Para repetir, crear otro
pedido: no retroceder ni borrar estos registros. Las órdenes antiguas #3/#4
permanecen intactas. La copia conjunta está en localhost:8085; Factura/Pago sigue
perteneciendo a Tino y su merge formal se decide por el equipo.

## Circuito inicial y cuentas demo

Oferta semanal ya permite crear borrador, publicar y continuar a Pedidos con
la oferta elegida. Ver [recorrido C05](OFERTAS_SEMANALES.md). El incremento usa
la misma sesión demo y base Supabase; no requiere migración.

Actualización: ya está disponible el incremento de Pedidos + State; ver
[recorrido de pedido y patrón](PEDIDO_STATE.md). La vista Pedidos admite alta,
consulta y transiciones. La integración actual de reservas/stock y Strategy se
detalla en el circuito integrado anterior; los límites del primer incremento se
conservan como historia en PEDIDO_STATE.md.

27/10/2026. Autorización del usuario: una cuenta local por rol para el MVP,
sin modificar la tabla usuarios ni exigir provisión de hashes en Supabase.
Esta decisión sustituye el requisito de cuentas persistidas para la demo local.
El login sigue usando sesión, BCrypt en memoria y CSRF; no requiere migración.

## Arranque

Con las variables JDBC reales en .env de la raíz:

```powershell
docker compose -f compose.yaml -f compose.supabase.yaml -f compose.demo.yaml up -d --build
```

Abrir http://localhost:8081. Las cuentas son ficticias y públicas para esta demo:

| Email | Contraseña demo | Rol |
|---|---|---|
| operaciones@demo.local | Demo2026! | OPERACIONES |
| cliente@demo.local | Demo2026! | CLIENTE |
| gerencia@demo.local | Demo2026! | GERENCIA |

No son credenciales reales ni cuentas de Supabase. Identificadores negativos
distinguen identidades locales; no usarlos como FK de clientes/pedidos.
El modo se habilita explícitamente con APP_AUTH_DEMO_ENABLED=true; sin el override
demo se conserva el login de usuarios persistidos, que requiere password_hash.
No publicar este modo ni reutilizar estas cuentas para un entorno público.

## Recorrido para el profesor

1. Ingresar como Operaciones.
2. Registrar producto con nombre, unidad y opcionalmente categoría/descripción.
3. Ver el registro recuperado desde Supabase y su ID.
4. Desactivarlo: pasa a inactivo sin borrar identidad ni relaciones; reactivarlo.
5. Registrar proveedor; verificar datos y estado. CUIT es opcional y único si se
   informa. No se implementa validación fiscal; no cargar datos personales reales.
6. Actualizar página, cerrar sesión e ingresar nuevamente: los datos comerciales
   continúan en la base. El login persiste mediante cookie de sesión.
7. Ingresar como Cliente o Gerencia: no tienen permisos para modificar maestros.

API nueva: PATCH /api/productos/{id}/activo y
PATCH /api/proveedores/{id}/activo, JSON {"activo":false} o true.
Body con activo obligatorio, Operaciones y CSRF válido; entidad ausente 404.
Repetir el mismo estado es idempotente. No hay borrado físico.

La pantalla solo muestra éxito tras respuesta real del backend y recarga por GET.
Ante error de recarga, volver a consultar antes de repetir un alta.
El recorrido de maestros demuestra MVC, Repository y Service Layer. Los módulos
de Ofertas y Pedidos amplían la venta semanal y demuestran State. Strategy,
inventario, facturación y reportes gerenciales siguen pendientes de sus frentes.
