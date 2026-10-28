# Compras habilitado y verificado localmente

Fecha: 27/10/2026 America/Argentina/Buenos_Aires. Usuario autorizó «habilitalo»,
después de aplicar expresamente la migración 002. No se autorizó merge ni despliegue.

## Entorno y cambios

Se hizo fetch y se creó worktree aislado .cache/compras-habilitado, rama local
codex/compras-habilitado, desde origin/andy b6bd6d4. Ese commit ya integra main
adbb0c1 y conserva ofertas. No se sobrescribió tino, main ni cambios locales.
No se modificó lógica Java, reglas, State, stock, precios o componentes de Andy.

Se agregó únicamente override local compose.compras.yaml en esa copia, con
COMPRAS_ENABLED=true. El valor global por defecto sigue false. Credenciales
se leen del .env original mediante --env-file, sin copiarlas ni mostrarlas.
ddl-auto=none y sql.init.mode=never permanecen. No se volvió a ejecutar DDL.

Proyecto Docker comunitarias-compras: frontend en http://localhost:8081 y
backend en 127.0.0.1:8080; ambos healthy. Puertos enlazados exclusivamente a loopback.
Las imágenes contienen backend/frontend compilados de la copia integrada.

## Verificaciones observadas

1. `docker build --target build -t comunitarias-compras-habilitado-check
   .cache/compras-habilitado/backend`: clean verify con Java 21, **111 pruebas**,
   cero fallos/errores/omitidas; BUILD SUCCESS. Incluye compras y ofertas integradas.
2. Compose construyó backend/frontend con los cuatro archivos (base, Supabase,
   demo, compras). TypeScript/Vite aprobado. Arranque con up -d --no-build.
3. Integración explícita en imagen Maven/Java 21, entorno JDBC sin secretos en args:
   `mvn -o --batch-mode --no-transfer-progress
   -Dtest=CompraSupabaseIT,OfertaSupabaseIT,PedidoSupabaseIT,SupabaseJdbcIT,MaestrosSupabaseIT test`:
   **13 aprobadas**, cero fallos/errores/omitidas. Incluye tres escenarios reales
   de compras antes pendientes: autorización/emisión/precio histórico, demanda
   con stock/cobertura y cobertura sin orden. Fixtures revertidos por rollback.
4. HTTP real Nginx → Spring → Supabase con cookies y CSRF:
   - Disponibilidad habilitada para Operaciones. Cliente sin acceso a compras ni
     contexto y Gerencia sin contexto operativo: 403.
   - Se creó pedido demo #16, producto #1, cantidad 2, y se confirmó por API existente.
     Usa cliente demo existente y oferta vigente; no cambia pedidos previos.
   - Compra por demanda #3 EMITIDA, precio servidor 850 × 2 = ARS 1700.
   - Repetir UUID y repetir el pedido con otro UUID: 409. Sin CSRF: 403.
   - Dos sesiones Operaciones enviaron simultáneamente la misma solicitud Quinteros:
     una respondió 201 y otra 409; se creó solo la orden #4, cantidad 3.
   - Emisión antes de autorización: 409; autorización por Operaciones/emisión por
     Gerencia: 403. Gerencia autorizó y Operaciones emitió #4: ARS 2550.
     Actores/fechas persistidos y repetición de transiciones rechazada 409.
   - GET final confirmó exactamente dos compras nuevas, ambas EMITIDA.
   No se modificaron vínculos, precios, productos ni inventario existentes.
5. Chromium/Playwright con backend real: formulario operativo por proveedor,
   modo Quinteros, listado de órdenes, vista Gerencia sin alta operativa, Cliente
   sin menú/acceso directo y vuelta a login al quitar sesión. Temas claro/oscuro
   y captura a 390 px revisados; cero errores JavaScript.
   El estilo de Compras sigue siendo el de Andy: controles menos delimitados que
   maestros y contenido con desborde horizontal en móvil. No se retocó su UI;
   la captura móvil no se considera conformidad final de diseño responsive.

Logs, resultados y capturas en .cache/compras-habilitado-*. Fuera de Git.
Los IDs saltan por secuencias usadas por pruebas con rollback; no implican que
se hayan dejado fixtures de esas pruebas en la base.

## Uso y reinicio

Abrir http://localhost:8081 e ingresar con cuentas ficticias de DEMO_LOCAL.md:
operaciones@demo.local o gerencia@demo.local; contraseña pública demo Demo2026!.
Menú Compras. Cliente no ve costos ni tiene acceso a ese módulo.

Para reiniciar desde la raíz, con Docker Desktop iniciado y .env disponible:

```powershell
& .\.cache\iniciar-compras.ps1
```

El script usa el worktree .cache/compras-habilitado y las imágenes ya construidas.
No borrar esa copia mientras se use este entorno. El compose normal de la raíz
no incluye por sí solo ni el código de Andy ni este override; ejecutar ambos
entornos simultáneamente chocaría en los puertos 8080/8081.

## Pendientes reales

Aplicación habilitada localmente y dependencia de esquema resuelta. Sin commit,
push ni actualización/merge de PR #5 en esta etapa: evidencia preparada localmente
para coordinar con Andy. Sus comentarios históricos «no aplicada» deben actualizarse
en la PR, sin volver a ejecutar SQL. Queda revisión del equipo y render UML;
el ensayo concurrente descrito no es una prueba exhaustiva de carga/bloqueos.

Recepción, movimientos/reservas, cancelación/liberación de coberturas y operación
continua de inventario siguen fuera del circuito aprobado de esta demo. Las
órdenes emitidas de prueba no acreditan recepción de mercadería ni cambio de stock.
