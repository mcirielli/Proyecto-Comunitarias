# Inventario y recepción: diagnóstico del incremento Andy

27–28/10/2026. Políticas/modelo aprobados; código compilado y pruebas estándar
aprobadas. Aplicación de DDL, activación y pruebas persistentes pendientes.
Objetivo: cerrar recepción, reservas y consumo reales sin duplicar State/Strategy
ni modificar Factura/Pago de Tino. Contrato de políticas en
[POLITICAS_INVENTARIO_RECEPCION.md](POLITICAS_INVENTARIO_RECEPCION.md).

## Base y lectura

Worktree propio .cache/andy-inventario, rama andy, base da976fc. Fetch realizado:
origin/main=da976fc; andy ya contiene main, no requiere merge ni cherry-pick.
origin/andy=91298e5; dos commits de integración/documentación pendientes de push.
GitHub no devolvió PR abiertas al consultar repositorio. PR #5 es histórica fusionada.
Sin cambios preexistentes en ambos checkouts al iniciar.

Leídos AGENTS.md, SUPERPROMPT, ambas entregas, decisiones, último cierre y reportes
de migración/habilitación, backlog, PEDIDO_STATE, COMPRAS_STRATEGY, reglas CMP,
catálogo y propuesta 002. Referencia académica: material de responsabilidades GRASP,
SOLID y Strategy en context/material-uade (34210_02_UADEConnect_02_2025,
34210_03_UADEConnect_01_20252 y 34210_03_UADEConnect_02_20251).
Se preservan las cinco implementaciones reales y la separación entre servicios,
dominio y repositorios; no se agregan patrones para completar una lista.

Contradicciones resueltas: selector de perfiles superado por sesiones/CSRF; prohibición
histórica de Docker superada por aprobación posterior; comentario NO APLICADA de
002 es histórico y contradice la evidencia actual de esquema presente. Rentabilidad
exigida académicamente pero recortada con validación docente pendiente. La decisión
histórica de movimientos de ajuste no autoriza un botón de corrección sin política.
El reparto genérico del backlog se concreta en Andy C02/C03 e integración necesaria
de C01; Tino conserva Factura/Pago. No se repite Sprint 1 ni se declara MVP completo.

## Evidencia de Supabase actual, solo lectura

Inspección JDBC Java 21 sobre la configuración local; transacción readOnly y rollback.
No se imprimieron conexiones, usuarios, contraseñas, clientes ni precios.

| Tabla | Filas | Hallazgo |
|---|---:|---|
| stock | 0 | int4 libre/reservado NOT NULL, CHECK >=0, UNIQUE producto, FK |
| movimientos_stock | 0 | Cantidad >0; tipos INGRESO/EGRESO/RESERVA/LIBERACION/AJUSTE; referencias excluyentes pedido/orden; sin unicidad de operación |
| pedidos / pedido_detalle | 5 / 7 | 3 RECIBIDO, 1 CONFIRMADO, 1 ENTREGADO; detalle único pedido/producto y cantidad >0 |
| ordenes_compra / orden_compra_detalle | 2 / 2 | Una DEMANDA_CLIENTES y una STOCK_QUINTEROS, ambas EMITIDA; ninguna sin UUID; cantidad >0 y detalle único orden/producto |
| compras_coberturas | 2 | UUID y orden únicos, trazabilidad presente |
| compras_cobertura_pedidos | 1 | Pedido único, PK/FK |
| compras_cobertura_detalles | 1 | Asigna stock=0, abierta=2; PK lote/producto y CHECK >=0 |

Las nueve tablas tienen RLS habilitado, FORCE RLS deshabilitado y cero políticas;
la conexión usada dispone de SELECT/INSERT/UPDATE efectivos. Esto verifica esa
conexión JDBC, no certifica acceso anon/authenticated ni una futura cuenta distinta.
La estructura de 002 está presente: seis columnas, estado varchar(30), CHECK de
autorización/estados y tres tablas de cobertura. No hay registro de migraciones que
pruebe ejecución por sí solo; la observación concuerda con el reporte autorizado.

No hay saldos negativos. Un ENTREGADO carece de movimientos; no inventar un egreso.
No existe evidencia de reserva individual durable ni recepción con actor/fecha.
El ledger relaciona un lote con pedidos y una posible orden nueva, pero su cantidad
abierta agregada no dice qué orden histórica abastece qué pedido. No alcanza con
cambiar RECIBIDA ni restar otra vez las asignaciones actuales.

## Clases existentes y riesgos concretos

- PedidoService.cambiarEstado bloquea pedido y delega en Pedido/State; actualmente
  no consulta inventario. Mantener seis estados y precios históricos.
- CompraService.crear bloquea pedidos y productos ordenados, calcula libre/abierto,
  resta todas las asignaciones históricas y persiste cobertura aun sin compra.
- CompraService.emitir bloquea primero orden y después productos. Debe armonizarse
  antes de cualquier recepción que tome productos antes de orden.
- StockAbastecimiento es entidad de lectura: no autoridad de escritura. Reutilizar
  su proyección explícita mientras Stock concentra invariantes; evitar escrituras
  mediante dos modelos y lecturas cacheadas inconsistentes en la misma transacción.
- OrdenCompra encapsula autorización/emisión; agregar recibir como operación de
  dominio. CoberturaCompra y sus asignaciones no modelan transferencia ni consumo.
- No hay StockService, reservas individuales, recepción API ni UI de inventario.

## Archivos previstos y etapas verificables

| Etapa | Archivos/responsabilidad Andy | Verificación / dependencia |
|---|---|---|
| 1 actual | Estos documentos y reporte; inspección local ignorada | Políticas y modelo para aprobación agrupada |
| 2 | domain/Stock, MovimientoStock; repositorios y StockService; RecepcionService, OrdenCompra, DTO/controller propios; propuesta 004 si se aprueba modelo | Unitarias, HTTP, persistencia y recepción concurrente; aplicar DDL requiere autorización separada |
| 3 | ReservaPedido/líneas y repositorio; PedidoService y colaboración State; ledger efectivo, CompraService y repositorios | Reserva total, rollback, sesiones distintas, ejemplo A/B, consumo único; no sustituir polimorfismo |
| 4 | StockPage/RecepcionPage, services/types; cambios aditivos de SecurityConfig/navegación aislados | Sesión/CSRF, roles, móvil/temas, recarga y build; preservar Tino |
| 5 | API/UML real renderizado/demo/backlog/reporte; integrar main reciente | Maven Java 21, Supabase, Docker, HTTP/navegador y ENTREGADO facturable si Tino está disponible |

No se agregan dependencias del proyecto. Las políticas/modelo fueron aprobados
durante esta ventana y habilitaron el código. No hay endpoints/botones simulados.
UML de clases implementadas: diagramas/inventario-recepcion.puml y SVG renderizado.
Factura/Pago, ofertas, pagos y precios no se editan. ULTIMO conserva enlaces previos.

API implementada bajo opt-in: GET /api/stock, GET /api/stock/{productoId}/movimientos
(página de 100), POST /api/compras/{id}/recepcion sin cuerpo y GET de esa ruta para
actor/fecha persistidos. GET /api/stock/disponibilidad informa habilitación y siempre
está disponible para Operaciones/Gerencia. Se reutiliza PATCH de pedido;
sin reserva manual. Entrada inválida 400, sesión ausente 401, rol/CSRF 403,
inexistente 404 y conflicto de saldo/estado/reintento 409. HTTP con mocks verificado;
HTTP contra inventario real pendiente de migración y habilitación.

Backlog Andy: A-STK diagnóstico/políticas; B-STK saldos/movimientos; C-REC recepción
total y transferencias; D-RSV reserva/State/consumo; E-INT concurrencia/UI/demo.
A-STK aprobado; B/C/D implementados en código con validación persistente pendiente;
E-INT tiene UI/build y pruebas estándar, pero falta esquema, Supabase y demo. Se conserva
reparto de cuatro frentes del backlog vigente, con propiedad actual Andy/Tino
indicada en la solicitud. No se reasignan alumnos sin acuerdo.

## Activación, recuperación y verificación pendiente

No activar compose.inventario.yaml antes de ejecutar y verificar 004 con aprobación
específica. app.inventario.enabled permanece false; la UI informa preparación pendiente
y no ofrece recepción operativa mientras no esté habilitado. En modo histórico,
Pedido/Compras conservan su recorte documentado; no presentarlo como circuito real.
ddl-auto=none y sql.init.mode=never conservados.

Antes del SQL: detener escritores de compras/inventario del entorno que se migra,
respaldo pg_dump restaurable en .cache y comprobación con pg_restore --list,
inspección reciente y precondiciones de 004. Aplicar transacción exacta sin 001/002,
validar tablas, constraints/índices/RLS, permisos de tabla y secuencia para JDBC,
comparar saldos/estados/movimientos y asignación antigua=efectiva. No imprimir secretos.
Rollback antes de commit ante cualquier fallo. Tras commit, conservar datos nuevos;
recuperación requiere restauración aislada y reparación auditada, no borrar ledger.

Comandos (Java 21, Maven disponible; solo ejecutar IT con esquema preparado):

```powershell
mvn -o -f backend/pom.xml verify
npm --prefix frontend ci
npm --prefix frontend run build
docker compose -p comunitarias-inventario-check build backend frontend
mvn -o -f backend/pom.xml -Dtest=InventarioSupabaseIT test
```

InventarioSupabaseIT usa fixtures con UUID propios y transacciones separadas para
concurrencia. Los confirma temporalmente para compartirlos entre conexiones y los
elimina por IDs en AfterEach; prueba rollback de la operación, no de secuencias.
Si su limpieza falla, detenerse y registrar IDs locales antes de reintentar.

Demo pendiente: oferta/pedido CONFIRMADO con faltante → demanda → recepción total
→ EN_PREPARACION/reserva → PREPARADO → DESPACHADO/consumo → ENTREGADO sin efecto
adicional; luego Quinteros con Gerencia/Operaciones y recepción. No se ejecutó
esta demo con inventario real ni se dejaron datos nuevos en Supabase en esta ventana.
Tino: ninguna edición de Factura/Pago ni mensaje a terceros. Main consultado aún no
contiene ese módulo; dependencia de facturación conjunta pendiente de integrar/verificar.
