# Incremento de maestros sobre Supabase

Actualización 27/10: disponible modo de usuarios locales por rol en compose.demo.yaml
(ver DEMO_LOCAL.md); ya no requiere password_hash para ese modo. Nuevas operaciones
PATCH /api/productos/{id}/activo y /api/proveedores/{id}/activo con Boolean
obligatorio, transacción, 404 si no existe y acceso exclusivamente Operaciones.
Los párrafos de login pendiente de este documento describen el estado anterior
y el modo de usuarios persistidos, no el modo demo autorizado.

Implementado el 26/10/2026. S1-03 y S1-05 parcialmente implementadas, sin cierre.
La estructura entregada por el usuario está preservada en
database/ESTRUCTURA_SUPABASE_COMPARTIDA.md. Sustituye como base de implementación
de maestros a MODELO_MAESTROS_PROPUESTA.md; no prueba DDL real ni todas las FK.

## Operaciones

Perfil `supabase` exclusivamente; variables JDBC configuradas externamente.
Los nombres de tabla se resuelven con el esquema por defecto de la conexión;
confirmar search_path/esquema real antes de integrar. No se ejecutó SQL ni DDL.

| Método | Ruta | Entrada | Respuesta |
|---|---|---|---|
| GET | /api/productos | — | 200, lista de productos |
| POST | /api/productos | nombre, descripcion opcional, categoria opcional, unidadMedida | 201, producto creado |
| GET | /api/proveedores | — | 200, lista de proveedores |
| POST | /api/proveedores | razonSocial, cuit/email/telefono opcionales | 201, proveedor creado |

Alta siempre activa. Nombre/unidad/razón social obligatorios y sin blancos;
servicio normaliza espacios externos y convierte opcionales vacíos a null.
CUIT no informado se persiste null, evitando colisiones por cadena vacía.
No se aplica algoritmo de validación fiscal ni unicidad de nombres.
Email informado debe tener formato válido. Duplicado de CUIT se resuelve mediante
restricción existente en PostgreSQL: 409, sin filtrar SQL. Entrada inválida: 400.

Ejemplos de cuerpos JSON:

```json
{"nombre":"Arroz","unidadMedida":"kg","categoria":"Almacén"}
```

```json
{"razonSocial":"Proveedor de ejemplo","email":"ventas@example.org"}
```

## Seguridad y límites para la demo

Rutas requieren ROLE_OPERACIONES; CSRF permanece activo. Login todavía pendiente:
no existe usuario funcional para invocar estos endpoints desde navegador.
No se habilita un selector de roles ni acceso anónimo para simular autenticación.
`dev` conserva solo infraestructura sin DataSource ni servicios de maestros.
En supabase se usan JPA Repository y transacciones; controller delega al servicio
y devuelve DTOs, nunca entidades. Sin dependencias nuevas.

La tabla usuarios compartida no tiene password/hash: el login BCrypt aprobado
requiere completar su contrato y almacenamiento. producto_proveedor tiene un
precio actual sin fecha ni versiones: todavía no permite conservar historial.
No sobrescribir precios ni declarar implementado historial con este esquema.
Pedidos/State, compras/Strategy, oferta, stock y frontend quedan fuera de este
incremento. No se consideran aprobadas sus reglas por existir tablas.

## Verificación

`mvn -o -f backend/pom.xml test` con Java 21: 13 tests aprobados
(InfrastructureTest: 6, MaestrosServiceTest: 7). Reglas probadas: campos
obligatorios, normalización, alta activa, opcionales null y email válido/inválido.
Sin conexión real Supabase, prueba HTTP autenticada ni revisión cruzada del equipo.
`mvn -Dtest=SupabaseJdbcIT test` sigue siendo una prueba explícita separada.
La ausencia de prueba real impide dar S1-02/03/05 por terminadas.
