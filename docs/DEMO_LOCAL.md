# Demo local del primer circuito funcional

Actualización: ya está disponible el incremento de Pedidos + State; ver
[recorrido de pedido y patrón](PEDIDO_STATE.md). La vista Pedidos admite alta,
consulta y transiciones. Reservas/stock y Strategy permanecen pendientes.

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
Este circuito demuestra MVC, Repository y Service Layer; no demuestra todavía
venta semanal, State, Strategy, inventario o facturación. Las pantallas propias
de Cliente y Gerencia quedan para los siguientes incrementos.
