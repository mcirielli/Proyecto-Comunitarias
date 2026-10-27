# Primera etapa de negocio: maestros e historial

26 de octubre de 2026. Propuesta para aprobación; no constituye esquema aprobado.

## Situación y alcance

El último reporte (17/10) configura agentes, sin avance funcional. SPRINT_1.md
mantiene S1-01 en revisión final y S1-02/03/04/05/06/09/10 pendientes.
El calendario ubicaría hoy al equipo en Sprint 3, pero no hay evidencia de cierre
del Sprint 1. No se declara avance por el paso del tiempo.

Primer incremento propuesto: registrar y consultar productos/proveedores,
vincularlos y registrar/consultar precios históricos (S1-03 parcial y S1-05).
Conservar monolito, capas y dependencias actuales. No incluir pedidos ni compras.

## Modelo mínimo propuesto v1

Destino propuesto: esquema PostgreSQL `comunitarias`; IDs BIGINT identity.
Todos los campos son obligatorios salvo los indicados como opcionales.

| Entidad | Campos y restricciones |
|---|---|
| Producto | id PK; nombre VARCHAR(150) no vacío; descripción VARCHAR(500) opcional |
| Proveedor | id PK; nombre VARCHAR(150) no vacío |
| ProductoProveedor | id PK; producto_id FK Producto; proveedor_id FK Proveedor; código VARCHAR(100) no vacío; condiciones VARCHAR(500) opcional; UNIQUE(producto_id, proveedor_id); UNIQUE(proveedor_id, código) |
| PrecioProveedor | id PK; producto_proveedor_id FK; importe NUMERIC(19,2) > 0; registrado_en TIMESTAMPTZ generado por backend |

Cardinalidades: Producto 1:N ProductoProveedor; Proveedor 1:N ProductoProveedor;
ProductoProveedor 1:N PrecioProveedor. FK sin borrado en cascada. Índice sobre
proveedor_id e índice compuesto de historial (producto_proveedor_id, id DESC).
Los índices de PK y unicidad cubren las otras búsquedas iniciales.

El ID Producto no cambia al incorporar otro proveedor. Nombres no únicos:
el nombre solo no identifica inequívocamente un producto o proveedor.
PrecioProveedor extiende la relación académica ProductoProveedor para conservar
historial sin duplicar el vínculo. El precio actual se deriva de la versión con
mayor id por vínculo; no se duplica en ProductoProveedor.

Reglas propuestas: moneda única ARS para esta etapa; BigDecimal, máximo dos
decimales sin redondeo silencioso; no editar ni borrar precios: una corrección
agrega una versión. Sin vigencias futuras ni retroactivas. Serializar altas de
precio por vínculo mediante bloqueo transaccional para ordenar las versiones.
La fecha representa registro, no vigencia comercial. No calcular precio de venta.

## Contrato e implementación a autorizar

GET/POST /api/productos; GET/POST /api/proveedores;
POST /api/productos/{id}/proveedores;
GET/POST /api/productos/{id}/proveedores/{proveedorId}/precios.
GET de precios devuelve historial ordenado y precio actual (nulo sin registros).
POST devuelve 201; datos inválidos 400; referencias inexistentes 404; vínculo o
código duplicado 409. DTOs sin entidades JPA expuestas.

Archivos: domain/{Producto,Proveedor,ProductoProveedor,PrecioProveedor}.java;
repository/*Repository.java; service/{ProductoService,ProveedorService,
PrecioProveedorService}.java; controller de maestros, DTOs, mappers y manejador
de errores; pruebas unitarias de validación/historial e integración separada;
database/schema.sql y seed.sql solo para este incremento tras aprobación.
SQL versionado antes de aplicación; destino real y permisos se comprueban antes
de aplicarlo. Mantener ddl-auto=none y sql.init.mode=never.

El acceso funcional requiere integrar S1-04: Operaciones gestiona maestros y
costos; no habilitar endpoints anónimos ni exponer costos a Cliente. Usuario,
roles, Cliente y oferta requieren su propio contrato/modelo antes de completar
el esquema de Sprint 1. Esta propuesta no los da por aprobados.

## División sugerida para cuatro integrantes

1. Cerrar S1-01 y comprobar JDBC local/contenedor; preparar Render.
2. Modelo maestros/historial, repositorios y servicios; tests de reglas.
3. Contrato de sesiones/CSRF/permisos y revisión de integridad/historial.
4. Contrato y pantalla de maestros/oferta; revisión e integración con API.

Revisiones cruzadas según SPRINT_1.md. No cerrar tareas con integración omitida.

## Discrepancias y bloqueos

Login real aprobado sustituye selector del superprompt. Docker está aprobado.
Rentabilidad/reportes ampliados pedidos por la entrega 2 siguen recortados por
el equipo sin validación docente acreditada. No modificar ese alcance aquí.
Copias preexistentes *-Andres-PC.java repetían clases y nombres públicos:
se preservaron nueve archivos en .cache/conflictos-onedrive/backend/src/,
fuera de las fuentes compiladas. La suite de seis pruebas vuelve a pasar.

## Verificación JDBC preparada

SupabaseJdbcIT usa DataSource de Spring, perfil supabase y SELECT 1 sin DDL.
Ejecutar `mvn -Dtest=SupabaseJdbcIT test` desde backend con las tres variables
SUPABASE_JDBC_URL, SUPABASE_DB_USER y SUPABASE_DB_PASSWORD en el entorno.
El sufijo IT lo separa de la suite unitaria por defecto; no omite el fallo por
falta de credenciales. No activar dev simultáneamente. Verificar SSL en la URL.
No compartir salida sin revisar posibles datos sensibles de conexión.
