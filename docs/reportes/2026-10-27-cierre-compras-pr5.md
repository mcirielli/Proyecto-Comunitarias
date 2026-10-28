# Compras + Strategy: cierre de revisión PR #5

27/10/2026, America/Argentina/Buenos_Aires. Incremento C01 para demo académica.
Usuario solicitó completar revisión, documentación/UML y ajuste móvil, actualizar
PR #5 y fusionar si no aparecen bloqueos. No autoriza nuevas reglas de inventario,
despliegue público ni cambios SQL adicionales.

## Bloqueos anteriores resueltos

1. Migración 002 aplicada con autorización en la base configurada localmente.
   Ver [reporte de aplicación](2026-10-27-aplicacion-migracion-compras-002.md).
   No ejecutar otra vez. Se verificaron esquema/RLS/permisos antes y después.
2. Compras habilitado por override opt-in compose.compras.yaml. El valor global
   false permanece para no romper bases sin migrar. No se versionan credenciales.
3. Persistencia real y recorridos demanda/Quinteros comprobados. Ver
   [habilitación local](2026-10-27-habilitacion-compras-local.md).
4. UML PlantUML actualizado, render SVG generado y PNG inspeccionado visualmente.
   El SVG documental se incluye intencionalmente; PNG/herramienta quedan en caché.
5. Corrección visual acotada: clases field/action/theme-switch existentes y CSS
   propio de Compras. Grilla minmax(0,1fr), hijos con min-width:0 y scroll interno
   de tablas evitan que el módulo ensanche el documento en móvil. Sin cambios Java.

## Revisión del agente principal

Se revisaron CompraService, estrategias, contexto, OrdenCompra/detalles,
CoberturaCompra/asignaciones, repositorios, DTO/controller, matriz de roles/CSRF,
frontend y migración. Revisión asistida, sin atribuir aprobación humana externa.
GitHub no tenía revisiones ni hilos pendientes al comenzar este cierre.

- Strategy mantiene polimorfismo real: dos algoritmos independientes del servicio/JPA.
- Demanda procesa CONFIRMADO seleccionado, proveedor y precio válidos; descuenta
  stock libre y cobertura abierta según CMP-01..11. No resta reservado dos veces.
- Precios salen del servidor y se copian a detalle; transacciones y bloqueos en
  orden estable, UUID/pedido únicos como protección adicional de persistencia.
- Quinteros exige autorización Gerencia antes de emisión Operaciones. Cliente
  no accede a costos. Sesión y CSRF conservados.
- Cobertura sin faltantes persiste sin crear orden vacía. Asignaciones durables
  conservadoras y sin liberación son límite aprobado de demo, no stock operativo.

No se encontraron bloqueos para integrar este incremento delimitado. Revisión
académica cruzada de compañeros sigue siendo recomendable, no se declara realizada.

## Evidencia de verificación

Base revisada b6bd6d4 (andy con main adbb0c1 integrado), más cambios de cierre
exclusivamente en frontend, documentación, diagrama y override local opt-in.

- Docker Maven clean verify con Java 21: **111 pruebas aprobadas**, sin fallos,
  errores u omitidas. No hubo cambios de backend después de esa ejecución.
- Supabase explícito: CompraSupabaseIT, OfertaSupabaseIT, PedidoSupabaseIT,
  SupabaseJdbcIT y MaestrosSupabaseIT: **13 aprobadas**, con rollback de fixtures.
  Precio histórico, cobertura sin compra y reconstrucción de autorización incluidos.
- HTTP real: pedido demo #16 → CONFIRMADO → demanda #3 EMITIDA, ARS 1700;
  Quinteros #4 → autorización Gerencia → emisión Operaciones, ARS 2550.
  Reintentos de UUID/pedido/transición rechazados; CSRF y roles comprobados.
- Dos sesiones enviaron el mismo UUID Quinteros concurrentemente: respuestas
  201 y 409, exactamente una orden creada. Ensayo concreto, no prueba de carga.
- Build final frontend tras ajuste CSS/TSX: TypeScript/Vite aprobado mediante
  `docker compose ... build frontend`. Solo frontend recreado, backend conservado.
- Playwright final: formulario Quinteros, proveedores/productos reales, listado,
  vista Gerencia sin alta, Cliente sin menú/acceso y sesión vencida. Claro/oscuro
  y ancho 390 px. Aserción scrollWidth <= innerWidth aprobada; cero errores JS.
- PlantUML 1.2025.2 descargado de Maven Central con checksum verificado; herramienta
  local sin dependencia del proyecto. Layout Smetana y export SVG/PNG correctos.
- git diff --check y revisión de archivos preparados/secretos antes de publicar.

Artefactos locales ignorados: .cache/compras-habilitado-*.log,
.cache/compras-cierre-*.log, capturas, JSON, herramienta PlantUML y snapshots.
No se incorporan .env, URL JDBC, claves, tokens, target, dist o node_modules.

## Integración y límites

Se actualiza PR #5 de andy hacia main conservando historial, sin force push y con
SHA esperado al fusionar. Retirar borrador después de publicar evidencia y comprobar
estado/revisiones. No activar auto-merge. Mensaje [skip render] para evitar despliegue
automático de Render, cuya configuración externa no se modifica desde esta tarea.
La confirmación efectiva de merge se informa en el chat y estado de GitHub.

Próximo frente funcional: stock/reservas, recepción y movimientos integrados con
State. No implementados aquí. Facturas/pagos/reportes e historial proveedor también
permanecen en backlog. El código de compras puede operar en el circuito limitado
de demo; no acredita abastecimiento continuo ni liberación/cancelación de cobertura.
No se afirma MVP completo ni producción desplegada.
