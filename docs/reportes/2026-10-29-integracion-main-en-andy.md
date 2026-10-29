# Integración de main en Andy: conflictos resueltos

Fecha: 29/10/2026, America/Buenos_Aires. Estado: CERRADO.
Solicitud: solucionar los conflictos que impedían integrar PR #6.
Alcance: incorporar main en andy y publicar la resolución. Sin merge de PR #6
hacia main, force push, nuevas reglas de negocio ni cambios de base de datos.

## Estado inicial y causa

Andy/origin/andy estaban sincronizados en df8fb27 y ambos worktrees limpios.
GitHub informaba PR #6 abierta/en borrador, mergeable=false.
Fetch mostró origin/main=a65cb17: main ya incorporó Factura/Pago de Tino mediante
PR #7, con commits 74c2b64 y cdd6880. El conflicto era la edición independiente
de los mismos archivos compartidos por Inventario y Facturación.

Se ejecutó git merge --no-commit origin/main dentro de .cache/andy-inventario.
Git identificó siete bloques de conflicto en seis archivos, sin conflictos en
las entidades, servicios, repositorios o tests propios de Factura/Pago.

## Resolución aplicada

| Archivo | Resultado |
|---|---|
| backend/.../config/SecurityConfig.java | Se preservan rutas/roles de /api/stock y /api/compras más /api/facturas y /api/pagos; se conserva UTF-8 y CSRF |
| backend/src/main/resources/application.properties | Se conservan los flags independientes de compras, inventario y facturación, todos opt-in |
| frontend/src/pages/OperacionesPage.tsx | Menú, títulos y vistas incluyen Inventario y Facturación/Pagos; Cliente sigue sin stock/compras; permisos de ambos frentes conservados |
| docs/DECISIONES_ARQUITECTURA.md | Aprobaciones de inventario y de facturación conservadas; nueva nota de integración, sin reescribir historia |
| docs/PENDIENTES_CODIGO_EQUIPO.md | Evidencia y reparto de ambos frentes conservados, con estado actual de PR #7 |
| docs/reportes/ULTIMO.md | Se conservan enlaces de Inventario, Facturación/Pagos y Compras; nuevo reporte de integración como entrada actual |

Se actualizaron además las referencias vigentes de demo/aceptación para indicar
que Factura/Pago ya está en main y ahora entra en Andy mediante esta integración.
Los reportes anteriores de validación aislada se conservan como evidencia histórica.
No se utilizaron opciones globales ours/theirs ni se descartaron cambios de otro frente.

## Verificación ejecutada

- git diff --quiet 4d310cd -- backend frontend compose*.yaml: código funcional,
  tests y configuración idénticos al estado combinado previamente validado.
- Sin marcadores de conflicto en backend/src, frontend/src o docs. Sin archivos
  sin resolver después de registrar las seis resoluciones en el índice.
- Maven Java 21 offline verify sobre el Andy resultante: 153 pruebas aprobadas,
  cero fallos/errores/omitidas; BUILD SUCCESS. Incluye State/Strategy, inventario,
  Factura/Pago y HTTP de roles/CSRF/UTF-8.
- npm --prefix frontend run build: TypeScript/Vite aprobado, 48 módulos.
- git diff --check y revisión de secretos antes de commit/push.

Logs locales ignorados: .cache/andy-merge-main-verify.log y
.cache/andy-merge-main-frontend.log. La copia previa 4d310cd ya había pasado 35
pruebas Supabase y demo real; no se presenta esa ejecución anterior como nueva.
No se repitieron escrituras ni migraciones en Supabase, dado que el código
funcional coincide exactamente y esta tarea solo resuelve la integración Git.

## Entrega y límites

Se conserva el historial de ambos padres: Andy df8fb27 y main a65cb17. Se publica
únicamente andy y se actualiza la misma PR #6. No se fusiona a main ni se activa
auto-merge. PR puede seguir en borrador hasta que el usuario complete su revisión.
No se inicia despliegue ni se reinician servicios de otras sesiones.

Si main cambia después de esta verificación, será necesario volver a comprobar
su relación de ancestro y el estado mergeable de la PR antes del merge final.
