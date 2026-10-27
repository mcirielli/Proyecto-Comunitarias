# Compras y patrón Strategy

## Estado y alcance

Incremento C01 para la rama `andy`. Reglas aprobadas y trazabilidad en
[COMPRAS_REGLAS_NEGOCIO.md](COMPRAS_REGLAS_NEGOCIO.md), CMP-01 a CMP-11.
El código utiliza tablas existentes más la migración propuesta
`database/propuestas/002_compras_strategy.sql`, **no aplicada**. Por eso el módulo
está deshabilitado por defecto y la PR debe permanecer borrador hasta completar
la verificación de persistencia y el recorrido HTTP con BD.

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
No se afirma render del diagrama. MVC, Repository y Service Layer se reutilizan.

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

## Activación pendiente de autorización

1. Revisar la propuesta 002 y autorizar expresamente su aplicación. No la ejecuta
   ni Spring ni Docker. Verificar backup y permisos de la cuenta JDBC con el dueño
   de la BD. Nuevas tablas tienen RLS sin políticas públicas; revisar GRANT según
   esa cuenta. El script exige ejecución única y revierte todo ante error.
2. Aplicar manualmente solo después de esa autorización; conservar evidencia del
   esquema y de sus restricciones. No ejecutar la propuesta 001 de password_hash
   como parte de compras.
3. Habilitar `COMPRAS_ENABLED=true` en el entorno del backend. Para ejecución
   nativa se usa esa variable; para Compose se puede crear un override local:

   ```yaml
   services:
     backend:
       environment:
         COMPRAS_ENABLED: "true"
   ```

   Pasar ese archivo con `-f` después de los Compose habituales de demo. No poner
   credenciales en el override ni habilitar compras antes de completar el esquema.
4. Ejecutar las pruebas explícitas de integración y luego el recorrido real.
   Si falla, deshabilitar el módulo y diagnosticar; no usar `ddl-auto=update`.

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
