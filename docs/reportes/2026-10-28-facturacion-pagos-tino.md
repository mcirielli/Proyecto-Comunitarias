# Facturación interna y pagos — Tino

Ventana: 27–28/10/2026, America/Argentina/Buenos_Aires. C06 implementado y
verificado, pendiente revisión de PR. Sin merge/auto-merge ni despliegue público.

## Base, alcance y aprobaciones

Se revisaron AGENTS.md, superprompt, entregas académicas, referencias UADE y
planificación histórica; decisiones, backlog, Pedido/State, ofertas y Compras.
Git inicial: tino ecffa52 y documentos locales sin seguimiento preservados.
Fetch encontró main da976fc, posterior al merge e3dfc52 de PR #5 (cambios de
legibilidad/guías). Se creó worktree .cache/facturacion-tino desde origin/tino y
se avanzó fast-forward a main, sin reset ni duplicación de commits. Rama local
codex/facturacion-tino para no cambiar la rama/directorio original; publicación
en la rama remota tino solicitada. No había PR abierta al inspeccionar.

Se mantiene monolito por capas, stack y precios históricos. Selector de perfiles
histórico no sustituye sesiones/CSRF aprobados. Facturación fiscal, automatización
de cobro y reportes completos no forman parte de C06. No se reescribe Sprint 1.
Usuario aprobó por respuesta explícita la política completa FAC/PAG y después
únicamente la migración 003. Referencia trazable: POLITICAS_FACTURACION_PAGOS.md.

## Esquema y migración autorizada

Inspección JDBC READ ONLY: facturas/pagos existentes con numeric(12,2), UNIQUE de
pedido y número, CHECKs de estados/medios/importes. Cero facturas y pagos al inicio;
un pedido ya entregado. RLS y permisos vigentes. No se imprimieron datos de conexión
ni se consultaron datos personales innecesarios.

003 ejecutada una sola vez: agrega pagos.clave_solicitud UUID nullable y UNIQUE.
La API nueva exige clave, los pagos legados pueden conservar NULL. Sin cambios
a estados, importes, clientes, tablas de Andy ni ejecución de 001/002/004.
Se guardaron instantáneas de metadatos antes/después (no dump completo), se limitó
espera de lock y se ejecutó en transacción. COMMIT y verificación post-commit
correctos. Comparación posterior: constraints anteriores, conteos y RLS conservados.
Logs/snapshots ignorados en .cache/facturacion-*; no se publica la cuenta JDBC.

## Implementación y revisión

Factura/Pago y enums mapean tablas existentes, sin entidades duplicadas de Pedido.
FacturacionService emite total histórico/número estable desde ENTREGADO; lock de
pedido serializa emisión y la unicidad existente protege persistencia. PagoService
bloquea factura, consulta UUID y SUM de pagos, valida saldo y persiste pago/estado
atómicamente. Idempotencia por contenido, no por importe/referencia comercial.
Factura cero PAGADA sin pago sintético; ANULADA solo legado consultable.

FacturacionAcceso mantiene asociación de Cliente de la base y demo reservado, sin
aceptar identidad ajena del navegador. Gerencia consulta; Operaciones escribe.
No se expone precio de compra ni proveedor. Controllers/DTOs y permisos/CSRF.
El resolver replica el contrato privado actual de PedidoService sin modificarlo,
pues ese servicio pertenece a Andy durante este frente.

FacturacionPage y servicio HTTP propio: emisión, detalle histórico y saldo,
historial y formulario de pagos parciales. UUID de formulario se conserva ante
fallo de respuesta sin ocultar/desmontar ese formulario por el error; tras éxito
se renueva. Recarga de navegador no recupera claves: consultar historial ante duda.
Temas/layout existentes, carga/vacío/errores/reintento/sesión vencida. Sin cobro real.
Opt-in FACTURACION_ENABLED/compose.facturacion.yaml; default false protege otras
bases. ddl-auto=none y sql.init.mode=never. Sin dependencias nuevas.

Se revisaron reglas de dominio, alcance de transacciones y locks, aislamiento de
cliente, casos de reintento y superficies de API. Revisión del agente principal;
no se atribuye revisión humana ni de subagentes. Cambios compartidos acotados a
SecurityConfig, propiedad de configuración y navegación OperacionesPage.
PedidoService, Pedido, state/*, strategy/*, CompraService, stock y recepción intactos.

## Verificaciones observadas

1. Docker Java 21 / Maven clean verify: **136 pruebas estándar aprobadas**, cero
   fallos/errores/omitidas. Primer incremento de unitarias: 132 aprobadas; la
   ejecución final incluye cuatro HTTP nuevas. No se omiten tests para construir.
   TypeScript/Vite frontend aprobado en el mismo build Compose.
2. Fallo intermedio de compilación en la prueba de rollback: inferencia ambigua de
   assertTrue(TransactionTemplate.execute(...)); corregida normalizando Boolean.
   Build completo posterior aprobado. No fue un fallo de la regla de aplicación.
3. Integración explícita con entorno JDBC local y Java 21:
   `mvn -o --batch-mode --no-transfer-progress
   -Dtest=FacturacionSupabaseIT,CompraSupabaseIT,OfertaSupabaseIT,PedidoSupabaseIT,SupabaseJdbcIT,MaestrosSupabaseIT test`:
   **20 pruebas aprobadas**, cero fallos/errores/omitidas, incluyendo siete nuevas
   de facturación y regresión completa previa de Supabase.
4. IT nuevas: parcial/total/idempotencia e historia tras cambio externo de precio de
   oferta, cero sin pago, clave con otro contenido, emisión simultánea, dos pagos
   que excederían saldo, misma clave simultánea y rollback después de flush de
   factura/pago/estado. Concurrencia usa transacciones e hilos distintos.
   Para estas carreras hay fixtures temporariamente confirmados y limpieza finally
   por IDs propios; resto de IT con rollback. No se modifican datos comerciales.
5. Entorno separado comunitarias-facturacion, backend 127.0.0.1:28080 y frontend
   http://localhost:28081, ambos healthy al arrancar. Compras en 8080/8081 se conserva.
   Health no se usa como prueba JDBC: esa evidencia está en IT y smoke.
6. HTTP real Nginx → Spring → Supabase: nuevo pedido demo #29 recorrió todos los
   estados permitidos; antes de ENTREGADO la emisión fue rechazada 409. Dos sesiones
   emitieron simultáneamente una sola factura #9 INT-PED-29, ARS 34,50.
   Dos solicitudes concurrentes con mismo UUID devolvieron el mismo pago #8;
   segundo pago #9 saldó la factura. Cambiar contenido/rebasar saldo ->409,
   precisión inválida ->400; roles/CSRF ->403. Cliente/Gerencia consultaron factura
   saldada y Cliente obtuvo solo datos de venta.
7. Chromium/Playwright con backend real: fixture nuevo pedido #30 creado/entregado
   mediante API existente (sin alterar previos). Desde la pantalla se emitió
   factura #10 INT-PED-30 por ARS 2000 y se registraron dos pagos de ARS 1000,
   quedando PAGADA/saldo cero. Se revisaron Operaciones, Cliente y Gerencia sin
   escrituras, sesión vencida, oscuro/claro y 390 px con aserción sin desborde
   horizontal del documento. Cero errores JavaScript. Capturas inspeccionadas.
8. UML fuente y SVG generados con herramienta local PlantUML ya disponible;
   PNG intermedio inspeccionado, fuera del commit. No nueva dependencia del proyecto.
9. git diff --check y comparación contra valores sensibles del .env en el contenido
   exacto preparado antes de commit/push. Fuera de Git: secretos, logs, snapshots,
   capturas, IDE, cachés, target/dist/node_modules y copia de trabajo.

Comandos Compose usan base + supabase + demo + compras + facturacion. Build:
`docker compose --env-file .env -p comunitarias-facturacion ... build backend frontend`.
Pruebas explícitas en imagen target build `comunitarias-facturacion-check`.
Logs: .cache/facturacion-compose-build.log, facturacion-it.log y facturacion-ui.log.
Arranque local reproducible en esta sesión: & .\.cache\iniciar-facturacion.ps1.
Activación portable desde repo: docs/FACTURACION_PAGOS.md y compose.facturacion.yaml.

## Datos dejados y límites

Demo HTTP: pedido #29, factura #9 y pagos #8/#9. Demo UI: pedido #30, factura #10 y
sus dos pagos (referencias DEMO UI parcial/saldo). Dos facturas PAGADA con saldo cero;
no hubo dinero real. Son fixtures demo separados; no se reutilizó ni facturó el
pedido entregado previo #2 ni se alteraron sus datos. Los saltos de secuencia se
deben también a pruebas revertidas/limpiadas y no significan datos comerciales extra.

No hay edición/anulación/devolución ni comprobantes fiscales, pasarela, impuestos
nuevos, emails, PDF o reportería completa. Comercio/producto se muestran actuales,
no snapshot de identidad. Inventario de Andy no estaba integrado en la base main
revisada; este trabajo valida el contrato ENTREGADO sin cambiarlo. El recorrido
con reservas/recepción nuevas deberá revalidarse cuando ese frente llegue a main.

Publicación autorizada: commits por etapas y PR tino → main, lista para revisión
si no aparecen bloqueos de integración. Sin merge ni auto-merge por este pedido.
No se publica aplicación en internet; usar [skip render] en commits por precaución.
No se declara MVP completo ni reporte gerencial aprobado por cerrar C06.
