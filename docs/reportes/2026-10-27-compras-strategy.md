# Compras + Strategy — incremento preparado, integración pendiente

Fecha: 27/10/2026, America/Buenos_Aires.
Estado: **EN REVISIÓN / PR BORRADOR**. No se declara módulo terminado.

## Alcance y aprobaciones

Solicitud: completar compras en `andy`, integrar base Pedido/State sin duplicar
commits, probar/documentar y publicar PR a main, sin fusionar ni desplegar.
`origin/main` 2ae7e11 ya incorpora c80373b mediante PR #3. Se creó `andy` desde
esa base; no había rama andy local/remota en la inspección inicial.

Se revisaron instrucciones, superprompt, ambas entregas académicas, decisiones,
reporte anterior, pendientes, Pedido/State y catálogo Supabase. Se inspeccionó
JDBC de solo lectura: stock vacío, tres vínculos de precio, órdenes vacías;
estados originales PENDIENTE/RECIBIDA/CANCELADA con varchar(20).

Usuario aprobó dos contratos en el chat: reglas mínimas de compras y cobertura
persistida para evitar reutilización. Pidió documentar todas las reglas para
conservar tracking: [COMPRAS_REGLAS_NEGOCIO.md](../COMPRAS_REGLAS_NEGOCIO.md), IDs
CMP-01 a CMP-11, registra decisiones, participantes/pruebas y límites de demo.
Autorización de migración: **solo prepararla; no aplicarla**.

## Implementación

- Strategy real: interfaz, contexto y dos algoritmos independientes de JPA/HTTP;
  selección polimórfica en CompraService.
- OrdenCompra y detalle; lectura de vínculo/precio existente y stock libre;
  DTO/API sin entidades expuestas y con precio histórico BigDecimal.
- Transacciones, locks de pedidos/productos/órdenes, UUID común único y cobertura
  durable aun sin faltante. Cantidades/proveedor/productos validados antes de alta.
- Quinteros independiente de pedidos: solicitud → autorización Gerencia → emisión
  Operaciones. Actor autenticado/fecha servidor, sin FK a IDs demo negativos.
- Permisos concretos/CSRF; Cliente no ve costos y Gerencia no hereda Operaciones.
- React Compras: contexto, formularios, consulta/detalles, acciones por rol y errores
  reales. Conserva UUID ante fallo de respuesta. Navegación con cambios pequeños.
- Propuesta SQL 002 sin aplicar. `COMPRAS_ENABLED=false` por defecto para conservar
  el arranque con esquema actual. Aviso de disponibilidad sin consultar tablas nuevas.
- Guía [COMPRAS_STRATEGY.md](../COMPRAS_STRATEGY.md), UML PlantUML fuente,
  actualización de decisiones y pendientes. Sin nuevas dependencias.

## Verificaciones observadas

1. Java 21.0.12.1 local: `mvn -o -f backend/pom.xml verify`, **98 tests aprobados**,
   cero fallos/errores/omitidos. Incluye 8 de estrategias, 14 de CompraService y
   12 de AuthFlow (4 nuevos de permisos/CSRF/validación). Se conservan State,
   PedidoService y maestros. Log local ignorado `.cache/compras-verify-final.log`.
2. Frontend: `npm --prefix frontend run build`, TypeScript/Vite aprobado, 42
   módulos. Log `.cache/compras-frontend.log`. No se agregó dependencia npm.
3. Docker: build separado `docker compose -p comunitarias-compras-check build
   backend frontend`; backend final recompilado tras ajuste de UUID común.
   `clean verify` dentro de Java 21: **98 tests aprobados**, imagen construida.
   Logs `.cache/compras-docker-build.log` y `.cache/compras-docker-final.log`.
4. Regresión explícita Supabase (credenciales locales no publicadas):
   `mvn -o -f backend/pom.xml -Dtest=PedidoSupabaseIT,SupabaseJdbcIT,MaestrosSupabaseIT
   test`, **6 aprobadas**, cero fallos/errores/omitidas, fixtures con rollback.
   No son pruebas del nuevo módulo de compras. Log `.cache/compras-regresion-supabase.log`.
5. Arranque del proyecto Docker separado con perfil Supabase/demo, puertos locales
   18080/18081: backend y frontend healthy. Smoke HTTP Nginx→Spring: los tres roles
   inician sesión 200; disponibilidad de compras 200/deshabilitado para
   Operaciones y Gerencia; Cliente 403. Sin escrituras comerciales ni DDL.
6. Intento de inspección por navegador no conservó la pestaña en la sesión de
   automatización. No se afirma validación visual ni ejecución de compras por UI.
   El circuito con compras habilitadas tampoco se simula.
7. UML fuente preparado; render pendiente. Advertencias existentes de MockBean
   deprecado/agent Mockito y LF/CRLF no bloquearon la compilación.
8. `git diff --cached --check` sin errores. Revisión del índice (200 archivos) con
   patrones de secretos y comparación contra secreto local: **sin hallazgos**.
   Los 40 archivos preparados son exclusivamente del incremento. Se retiraron
   los contenedores temporales de verificación; los habituales se conservaron.

## Pendientes concretos y límites

- Revisar y autorizar aplicación de `database/propuestas/002_compras_strategy.sql`.
  No se ejecutó. Revisar permisos JDBC/RLS y constraints con responsable de BD.
- Después, habilitar compras y ejecutar `CompraSupabaseIT` (3 escenarios preparados
  y compilados, **no ejecutados**) más recorrido HTTP/UI con BD de ambos tipos,
  repetición y autorización/emisión. Concurrencia real de locks/constraints sigue
  pendiente de esa integración; mocks no la prueban.
- Coberturas se conservan, sin liberación automática. Integrar recepción, consumo
  y cancelación con inventario antes de operar continuamente. No se implementan
  reservas ni movimientos. Las transiciones State existentes siguen sin mover stock.
- No se administra historial de proveedores: solo se lee precio actual y se copia
  al detalle de orden. No se edita Oferta/DetalleOferta/OfertaRepository ni State.
- Sin despliegue público, aplicación SQL, merge ni auto-merge. PR borrador mientras
  no se cierre la dependencia indispensable de esquema/persistencia.

Cambios locales ajenos conservados y excluidos del commit: compose.yaml,
docs/AGENTES.md y docs/GUIA_USO_AGENTES.md. Cachés/logs/env/IDE/generados/copies
OneDrive no forman parte de la entrega.

## Próximo prompt

Revisar reglas CMP-01..11 y propuesta 002. Si el usuario autoriza explícitamente
aplicarla, inspeccionar nuevamente esquema/credenciales sin revelarlas, aplicar
solo esa migración con evidencia, habilitar módulo y ejecutar CompraSupabaseIT
y circuito HTTP/UI real. No cambiar reglas de cobertura/inventario ni permisos
sin registrar el acuerdo. Mantener PR borrador hasta pasar estas verificaciones.
