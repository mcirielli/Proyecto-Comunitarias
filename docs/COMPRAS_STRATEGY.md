# Compras y patrón Strategy

## Estado y alcance

Incremento C01 para la rama `andy`. Reglas aprobadas y trazabilidad en
[COMPRAS_REGLAS_NEGOCIO.md](COMPRAS_REGLAS_NEGOCIO.md), CMP-01 a CMP-11.
El código utiliza tablas existentes más la migración 002, **aplicada con autorización**
en la base configurada localmente el 27/10/2026. El circuito de demo fue habilitado
con el override opt-in compose.compras.yaml y verificado con Supabase. Para otra
base no ejecutar el script sin inspección/aprobación: es de ejecución única.
Evidencia de aplicación y pruebas: [cierre de PR #5](reportes/2026-10-27-cierre-compras-pr5.md).

No hay nuevas dependencias. Se conservan Java 21, BigDecimal, transacciones,
sesiones/CSRF, `ddl-auto=none` y `sql.init.mode=never`.

## Participantes y responsabilidades

- `CompraStrategy` define calcular(ContextoCompra). El contexto contiene cantidades,
  pedidos y cobertura; no depende de HTTP, repositorios ni JPA.
- `CompraPorDemandaStrategy` consolida pedidos y calcula
  máximo(0, demanda − stock utilizable − órdenes abiertas utilizables).
- `CompraQuinterosStrategy` valida productos/cantidades independientes de pedidos.
  La compra para stock no descuenta demanda ni disponibilidad.
- `CompraService` selecciona la implementación de un registro por TipoCompra y
  llama a `CompraStrategy.calcular`. No contiene los algoritmos alternativos.
  Coordina locks, validaciones, precio servidor, copia histórica, persistencia y
  autorización. Las escrituras completas ocurren en una transacción.
- `OrdenCompra` controla autorización/emisión. `DetalleOrdenCompra` conserva el
  precio histórico y calcula subtotal. `CoberturaCompra` registra solicitudes
  únicas y la demanda cubierta sin prometer reservas físicas.
- Repositorios Spring Data separan el acceso de datos. El repositorio de
  abastecimiento agrega queries propias sin editar PedidoRepository/State.
- `CompraController` solo recibe DTOs y delega; `ComprasPage` muestra respuestas
  reales, cantidades/precios, autorización gerencial y emisión operativa.

UML fuente: [compra-strategy.puml](diagramas/compra-strategy.puml).
Diagrama renderizado y revisado: [SVG](diagramas/compra-strategy.svg).
MVC, Repository y Service Layer se reutilizan.

## API y permisos

| Método | Ruta | Perfil |
|---|---|---|
| GET | /api/compras/disponibilidad | Operaciones/Gerencia; no consulta BD |
| GET | /api/compras | Operaciones/Gerencia |
| GET | /api/compras/contexto | Operaciones: proveedores/productos/precios y pedidos elegibles |
| POST | /api/compras/demanda | Operaciones |
| POST | /api/compras/quinteros | Operaciones |
| POST | /api/compras/{id}/autorizar | Gerencia |
| POST | /api/compras/{id}/emitir | Operaciones |

Cliente no accede a costos ni al contexto. Gerencia no hereda maestros ni pedidos.
Todas las escrituras requieren sesión y CSRF. GET no escribe. No hay recepción,
cancelación, modificación de precios ni movimiento de stock en estas rutas.

Demanda:

```json
{"claveSolicitud":"d28198bb-eaac-4643-9206-48b05efb3934","proveedorId":1,"pedidos":[1,2],"productos":[]}
```

Quinteros:

```json
{"claveSolicitud":"58a59577-0267-4137-ac83-38e1fb74357a","proveedorId":1,"pedidos":[],"productos":[{"productoId":1,"cantidad":5}]}
```

No se reciben precios. Alta devuelve 201 con `coberturaId`, `orden` y `mensaje`.
Si no hay faltantes, `orden` es null y se persiste cobertura sin orden vacía.
Los IDs del ejemplo son ilustrativos; elegir datos existentes del contexto.
Repetir UUID/pedido devuelve 409, no genera otra compra. React mantiene el UUID
ante una respuesta fallida para que reintentar no duplique. Tras un 409, actualizar
el listado; no cambiar UUID para volver a comprar la misma solicitud Quinteros.
Datos inválidos/overflow: 400; inexistentes: 404; conflictos: 409; permisos: 403.

## Activación local

La migración 002 ya se aplicó en la base usada por esta demo, con autorización
expresa. No volver a ejecutarla; para otras bases se requiere revisión y aprobación
propias. Evidencia: [aplicación 002](reportes/2026-10-27-aplicacion-migracion-compras-002.md).
El valor por defecto sigue apagado para proteger bases sin migrar.

Desde la raíz, con las variables JDBC en .env y Docker Desktop iniciado:

```powershell
docker compose -f compose.yaml -f compose.supabase.yaml -f compose.demo.yaml -f compose.compras.yaml up -d --build
```

Abrir http://localhost:8081. Operaciones crea compras y emite Quinteros; Gerencia
autoriza. Cuentas ficticias de DEMO_LOCAL.md. No publicar este modo de demostración.
No ejecutar simultáneamente otro proyecto Compose con los mismos puertos.
No se necesita modificar .env ni pasar secretos al frontend.

## Verificación

```powershell
# JAVA_HOME debe apuntar a Java 21; Maven disponible en PATH.
mvn -o -f backend/pom.xml verify
npm --prefix frontend run build
docker compose -p comunitarias-compras-check build backend frontend
# Solo con entorno Supabase y migración 002 aplicada con autorización:
mvn -o -f backend/pom.xml -Dtest=CompraSupabaseIT test
```

`CompraStrategyTest` prueba los dos algoritmos sin BD; `CompraServiceTest` cubre
precio servidor/histórico, proveedor, stock/coberturas, repetición, estados y roles.
`AuthFlowTest` usa servidor HTTP real con repositorios/servicio de compras mock:
prueba permisos, CSRF y validación, **no** persistencia. `CompraSupabaseIT` prepara
fixtures separados con rollback para probar reconstrucción de autorización/precio,
demanda, unicidad y cobertura sin orden. No crea tablas ni aplica migraciones.

Recorrido después de integración: Operaciones confirma dos pedidos compatibles;
crea demanda, observa cantidades y precio; repetir pedidos es rechazado. Solicita
Quinteros sin pedidos; Gerencia consulta detalle y autoriza; Operaciones emite;
recargar conserva estados/actores/fechas. Cliente no puede consultar compras.
La recepción y la liberación siguen fuera de este circuito de demo aprobado.
