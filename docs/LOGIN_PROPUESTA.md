# Siguiente incremento: contrato propuesto, sin implementar

## Actualización 27/10/2026

Usuario autorizó **solo preparar login, sin modificar Supabase**.
Backend y pantalla React preparados en esta ventana; SQL propuesto en
database/propuestas/001_password_hash.sql, NO aplicado. No se provisionaron cuentas.
El contrato siguiente se conserva como objetivo. No se da por operativo el login
contra BD hasta aprobar/aplicar la columna, revisar roles y provisionar hash BCrypt.
Los tests HTTP usan repositorios simulados y no sustituyen ese circuito real.
No se reconstruyeron ni reemplazaron los servicios en ejecución con este incremento.

Implementación: Usuario/UsuarioRepository, UsuarioDetailsService/UsuarioPrincipal,
AuthController y SecurityConfig; OperacionesPage en React integra sesión,
login/logout, altas y consultas con Fetch y CSRF en memoria. Sin localStorage.
Logout invalida sesión; consultas de maestros solo para Operaciones.
Las sesiones ya emitidas conservan autoridades hasta logout/expiración; no se
implementa revocación instantánea al cambiar rol/activo en BD en esta etapa.
Producción HTTPS/cookies requiere validación específica antes de publicar.

Ver reporte 2026-10-27-base-login-preparado.md para pruebas y pendientes.

Requiere revisión del equipo antes de implementar. Conserva sesiones, BCrypt,
CSRF y los tres roles aprobados; sin JWT, registro público ni selector de roles.

| Método/ruta | Contrato propuesto |
|---|---|
| GET /api/auth/csrf | Público, 200 `{token, headerName:"X-CSRF-TOKEN"}`; token asociado a sesión, Cache-Control: no-store |
| POST /api/auth/login | Público con CSRF válido; application/x-www-form-urlencoded con email y password; 200 `{id,nombre,email,rol}`; 401 genérico ante credenciales inválidas, cuenta inactiva o sin hash |
| GET /api/auth/session | 200 con el mismo DTO, 401 sin sesión |
| POST /api/auth/logout | Sesión y CSRF requeridos, 204; invalida sesión y elimina cookie |

Usar autenticación administrada por Spring Security, cambiar identificador de sesión
al autenticar y persistir SecurityContext. Renovar CSRF después de login y logout
mediante GET csrf. No registrar cuerpo del login, contraseña, hash ni tokens.
Una escritura sin CSRF devuelve 403 antes de comprobar credenciales.
Error JSON genérico para 401/403; nunca devolver excepciones internas.

Propuesta local: cookie JSESSIONID HttpOnly, SameSite=Lax, path=/,
expiración por inactividad de 30 minutos; Secure en HTTPS (HTTP local sin Secure).
Preferir mismo origen mediante proxy /api existente. Fetch credentials: include,
token en memoria y header en toda escritura, incluido login/logout. No localStorage.
Producción y sus dominios/cookies requieren decisión posterior.

Rol persistido propuesto: CLIENTE, OPERACIONES o GERENCIA, traducido mediante lista
permitida a ROLE_CLIENTE, ROLE_OPERACIONES o ROLE_GERENCIA. Verificar primero
valores reales; no convertir silenciosamente roles desconocidos. Usuarios inactivos
o sin hash no autentican. Gerencia no hereda Operaciones.
Health y csrf públicos; sesión/logout para cualquier usuario autenticado;
GET/POST productos/proveedores exclusivamente OPERACIONES. Otros recursos siguen
protegidos y sus permisos se definen cuando se implemente cada módulo.

## Migración mínima para revisión, NO aplicada

Confirmar esquema, tipos, restricciones y permisos reales de usuarios antes de usar
este SQL. `public` es una propuesta explícita pendiente de contraste.

```sql
ALTER TABLE public.usuarios ADD COLUMN password_hash varchar(60) NULL;
```

Columna inicialmente nullable para no inventar contraseñas de registros existentes.
No agregar default. Proveer hashes BCrypt de cuentas de demo mediante procedimiento
local revisado, sin versionar contraseñas ni hashes de cuentas reales. La longitud 60
corresponde al BCrypt directo del bean existente, sin prefijo de DelegatingPasswordEncoder.
Solo después de revisar/provisionar todas las cuentas y decidir tratamiento de
usuarios históricos evaluar NOT NULL en otra migración. No incluir esa restricción
en el cambio mínimo ni aplicar SQL automáticamente. Sin Flyway ni dependencia nueva.

## Archivos previstos para React, sin crear todavía

- `src/services/http.ts`: Fetch, credenciales, CSRF y errores 401/403/409.
- `src/services/auth.ts` y `src/types/auth.ts`: contrato de sesión/login/logout.
- `src/hooks/useSession.ts`: identidad, carga, expiración y renovación de CSRF.
- `src/pages/LoginPage.tsx`: formulario y errores genéricos.
- `src/services/maestros.ts` y `src/types/maestros.ts`: DTOs de API_MAESTROS.
- `src/pages/ProductosPage.tsx` y `ProveedoresPage.tsx`: alta y recarga desde GET
  tras 201, estados de carga/vacío/error; no simular persistencia.
- `src/main.tsx`: integración de sesión y navegación mínima sin dependencia nueva.

Pruebas del siguiente incremento: BCrypt, login válido/inválido/inactivo/sin hash,
fijación e invalidación de sesión, 401, matriz de roles, CSRF ausente/incorrecto/válido,
renovación de token y circuito de navegador con datos efectivamente persistidos.
Este documento no acredita ese circuito ni cierra S1-04/05/06.
