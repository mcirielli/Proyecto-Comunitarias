# Base verificada y login preparado

Estado: CERRADO (ventana, Sprint 1 sigue abierto).
Fecha: 27/10/2026, America/Buenos_Aires. Base Git 11ed8a7.

## Autorización y contexto

Usuario pidió comprobar base y avanzar MVP. Al presentar contrato de login y
columna de hash, respondió: **Solo preparar login; no modificar Supabase**.
Se respetó: no DDL, migraciones aplicadas ni cambios de cuentas. Las únicas
escrituras contra BD fueron datos ficticios dentro de tests con rollback.
Las secuencias identity pueden avanzar aunque los registros se reviertan.
Los numerosos cambios locales preexistentes se conservaron.

## Persistencia comprobada

Variables tomadas en memoria del contenedor backend existente, no impresas ni
incorporadas a imagen. Maven local con Java 21:
`mvn -o -f backend/pom.xml -Dtest=SupabaseJdbcIT,MaestrosSupabaseIT test`.
Resultado: **5 tests aprobados**, sin fallos/errores/omitidos.

Se comprobaron SELECT 1, metadatos, alta/consulta producto, alta/consulta proveedor
y rechazo de CUIT duplicado. EntityManager se limpia antes de consultar para
evitar confundir lectura desde caché con consulta real. Altas con rollback.
Tablas resueltas en public; ids bigint identity; seis columnas por maestro;
PK(id) y UNIQUE(cuit) confirmados en proveedores.
Límites reales: nombre/razón social/email 150; categoría 100; unidad/teléfono 30;
CUIT 11. No se auditaron todas las tablas ni políticas/permisos de otros roles.

Repetición Docker con etapa build del Dockerfile:
`docker build --target build -t comunitarias-backend-verificacion ./backend`;
`docker run --rm -e SUPABASE_JDBC_URL -e SUPABASE_DB_USER -e SUPABASE_DB_PASSWORD
comunitarias-backend-verificacion mvn --batch-mode --no-transfer-progress
-Dtest=SupabaseJdbcIT,MaestrosSupabaseIT test`.
Primer intento: 4 aprobados, 1 error por prepared statement S_1 already exists.
Configuración del driver prepareThreshold=0 en application-supabase.properties
para el pooler transaccional. Rebuild y repetición: **5 aprobados, BUILD SUCCESS**.
No se desactivó CSRF ni alteró esquema para resolver el problema.
Logs locales en .cache/integracion-2026-10-27.log e integracion-docker-2026-10-27.log.

## Incremento preparado, sin desplegar

- Usuario/UsuarioRepository; UsuarioDetailsService valida activo, hash y rol
  permitido; UsuarioPrincipal expone solo identidad mediante DTO.
- AuthController: CSRF, consulta sesión y logout. SecurityConfig: login de
  Spring Security solo en supabase, BCrypt, sesión, JSON genérico 401/403,
  protección Operaciones y CSRF conservados; sin JWT ni usuarios de respaldo.
- Cookie HttpOnly/SameSite=Lax y 30 minutos de inactividad.
- OperacionesPage: formulario de login, sesión/logout, navegación maestros,
  altas/consultas reales con Fetch, credenciales y token CSRF sin localStorage.
  HomePage conserva diagnóstico HTTP con explicación precisa.
- database/propuestas/001_password_hash.sql: propuesta NO aplicada.
- LOGIN_PROPUESTA.md actualizado: código preparado no equivale a login real.

La columna password_hash sigue pendiente. No se probó login con usuarios reales
ni circuito navegador completo; no se puede usar todavía contra la BD actual.
Servicios en ejecución no se reconstruyeron ni reemplazaron. La imagen de
verificación es de desarrollo con Maven, no se usa como runtime de la demo.

## Verificaciones y revisión

Suite `mvn -o -f backend/pom.xml test`: **22 aprobadas**, 0 errores/fallos/omitidos
(infraestructura 6, maestros 7, errores 2, login simulado 7).
El primer test de CSRF mostró 401 al redirigir el error; se corrigieron handlers
JSON explícitos 401/403 y la suite pasó. Los siete tests de login comprueban
sesión/logout, credenciales incorrectas, inactivo, hash ausente, rol desconocido,
restricción Cliente y ausencia de herencia Gerencia→Operaciones.
Se reforzó la prueba de ciclo con rotación de sesión y rechazo del token previo;
repetición `mvn -o -f backend/pom.xml -Dtest=AuthFlowTest test`: siete aprobadas,
BUILD SUCCESS (.cache/login-final.log).

Frontend: `npm run build`, TypeScript y Vite exitosos. Sin prueba visual/browser
ni afirmación de circuito funcional autenticado. git diff --check sin errores,
con avisos de conversión LF/CRLF. Sin nuevas dependencias, commit/push o publicación.
Revisión principal únicamente, no revisión cruzada independiente del equipo.
Se excluyen contraseñas, hashes y tokens de respuestas; sin credenciales nuevas
en código o SQL. No se realizó auditoría exhaustiva del historial Git.

## Estado y siguiente prompt

Conectividad y persistencia de maestros ya acreditadas local y contenedor.
No cerrar S1-02 sin restantes controles/revisión cruzada de SPRINT_1; no cerrar
S1-03/05 porque falta historial de precios y alcance completo. S1-04 preparado,
pendiente migración/provisión y pruebas reales. S1-06/oferta aún pendiente.

Solicitar autorización concreta para aplicar/provisionar password_hash antes de
modificar Supabase. Revisar tabla usuarios/roles y cuenta de demo por canal local,
sin versionar secretos. Después ejecutar circuito login→alta→consulta→logout
desde React y verificar reinicio. Historial de precios requiere extensión separada.
No ampliar pedidos/State o compras/Strategy automáticamente.
