# Demo local de maestros funcional

Estado: CERRADO (ventana de ejecución; Sprint 1 continúa abierto).
Fecha: 27/10/2026 America/Buenos_Aires. Base Git 11ed8a7.

## Autorización

Usuario pidió una cuenta local por rol y resultados reales para mostrar al
profesor. Sustituye para demo la dependencia de usuarios persistidos/password_hash.
No se modificó tabla usuarios ni otras estructuras Supabase. No se pidió otra
aprobación para implementar el incremento autorizado.

## Implementación

- DemoUsersConfig, habilitado solo por app.auth.demo-enabled=true con supabase:
  tres identidades locales, una por rol; BCrypt en memoria con el encoder existente.
  UsuarioDetailsService persistido se deshabilita en este modo. No se consulta
  password_hash ni se provisionan cuentas. No se añaden dependencias.
- compose.demo.yaml: activa modo expresamente junto a los dos Compose existentes.
- Producto/Proveedor permiten cambiar activo; MaestrosService coordina recuperación,
  cambio y guardado transaccional; inexistente devuelve 404.
- CambiarActivoRequest y endpoints PATCH con activo obligatorio; rutas y subrutas
  de maestros restringidas a Operaciones. CSRF y sesión se conservan.
- React agrega botones de activar/desactivar, recargando mediante GET tras éxito.
  Conserva alta, consulta, login y logout. No simula persistencia ni selección de rol.
- DEMO_LOCAL.md documenta credenciales ficticias y recorrido; decisiones/API actualizadas.

## Verificaciones observadas

Java 21, Maven local: `mvn -o -f backend/pom.xml test` → **25 aprobadas**,
0 errores/fallos/omitidos. Infraestructura 6; HTTP login simulado 7; errores 2;
servicio maestros 9; usuarios locales 1 (tres roles y login repetible tras borrar
credenciales del principal). Log: .cache/demo-tests.log.

Frontend `npm run build`: TypeScript/Vite exitosos.

`docker compose -f compose.yaml -f compose.supabase.yaml -f compose.demo.yaml
up -d --build`: build exitoso con suite Maven, backend Healthy y frontend iniciado.
Log: .cache/demo-compose.log. Se actualizaron contenedores locales, sin publicar.

Prueba HTTP real con Python urllib y cookie jar contra http://localhost:8081/api
(proxy Nginx, backend y BD real; sin repositorios simulados):

1. Login Operaciones 200 con CSRF.
2. POST producto 201, ID **7**, Arroz demo facultad.
3. PATCH producto activo=false y GET confirman inactivo; PATCH true y GET confirman
   reactivación. Cambios reales confirmados tras completar cada transacción HTTP.
4. POST proveedor 201, ID **12**, Proveedor demo facultad, email ficticio.
5. Logout 204 y consulta posterior de maestros 401.
6. Login Cliente y Gerencia 200; ambos GET maestros y PATCH producto responden 403.

Se dejaron esos dos registros ficticios para mostrar el módulo; producto queda
activo. No se modificaron datos comerciales preexistentes. No se borraron cuentas.
Las pruebas de integración con rollback de la sesión anterior acreditan la base;
esta ventana añade el circuito HTTP real, no una repetición de toda esa suite.
git diff --check sin errores, con avisos habituales LF/CRLF.

## Límites y revisión

No se ejecutó interacción visual en navegador ni ensayo completo de presentación.
HTTP real y build frontend no prueban todo comportamiento visual. No commit/push.
Revisión del agente principal; revisión cruzada del equipo pendiente.
Credenciales demo públicas y locales: documentadas; sin credenciales Supabase en
código. No aplicar la migración propuesta. Modo persistido sigue condicionado a
hash/provisión; no se declara operativo por esta demo.
Identidades locales negativas no pueden usarse directamente como FK Cliente.
Cliente/Gerencia aún no tienen sus módulos comerciales; solo login y permisos.
State, Strategy, pedidos, oferta e inventario siguen pendientes; no cinco patrones
completos ni MVP final. Activación/desactivación no modifica referencias históricas.

## Próximo prompt

Arrancar con los tres Compose de DEMO_LOCAL.md; ingresar como operaciones@demo.local
con la contraseña ficticia allí documentada. Mostrar alta/consulta y cambios de
estado desde React. Mantener datos comerciales reales en Supabase y cuentas en
memoria. Siguiente incremento recomendado: oferta vigente para Cliente y luego
pedido con precios históricos/State, resolviendo explícitamente vínculo de cuenta
demo con Cliente persistido y reglas de reserva antes de implementarlo.
