# Decisiones de arquitectura

## Facturación interna y pagos — aprobación 27/10, cierre 28/10/2026

El usuario autorizó C06 en tino, commit/push y PR a main, sin merge/auto-merge.
Aprobó la política completa FAC/PAG: INT-PED-{pedidoId}, solo ENTREGADO, ARS y
precio histórico, parciales sin sobrepago, PENDIENTE/PAGADA y factura cero PAGADA
sin pago ficticio; medios manuales EFECTIVO/TRANSFERENCIA/TARJETA, referencia
opcional y fecha servidor. Sin edición/anulación/devolución. Operaciones escribe,
Cliente consulta solo propias y Gerencia consulta. Reintentos recuperan la misma
factura/pago; clave de pago con otro contenido devuelve conflicto. Sin snapshot
fiscal de identidad. Reglas y participantes en POLITICAS_FACTURACION_PAGOS.md.
Autorizó específicamente aplicar únicamente 003 (UUID único de pagos): ejecutada
con transacción y verificación post-commit. No se aplicó SQL de Andy ni se alteraron
Pedido/State, stock o compras. Activación opt-in independiente; no despliegue público.


## Cierre de Compras y autorización de integración — 27/10/2026

En este chat, el usuario autorizó aplicar únicamente migración 002, luego habilitar
Compras localmente y finalmente completar la revisión/PR y fusionarla si pasan las
verificaciones. Sustituye para esta etapa la instrucción anterior de mantener la
PR en borrador sin merge. Sin auto-merge ni despliegue público. Las reglas CMP-01
a CMP-11 y los límites de inventario se conservan. Ver reportes de aplicación,
habilitación y cierre PR #5. Otros entornos requieren su propia migración aprobada.


## Compras + Strategy — 27/10/2026

Usuario autorizó implementar C01 de punta a punta y aprobó expresamente el
contrato de stock libre, demanda CONFIRMADO, proveedor explícito, órdenes abiertas
y autorización/emisión Quinteros por roles separados. También aprobó cobertura
persistida para evitar reutilización entre grupos, con límite de demo sin liberar
asignaciones hasta integrar inventario. Reglas con IDs CMP-01 a CMP-11 en
[COMPRAS_REGLAS_NEGOCIO.md](COMPRAS_REGLAS_NEGOCIO.md); participantes/API y activación
en [COMPRAS_STRATEGY.md](COMPRAS_STRATEGY.md).

Autorización de esquema: **solo preparar** propuesta 002; no aplicarla.
El módulo queda apagado por defecto hasta completar esa dependencia. No se
modifican State, ofertas, reservas/movimientos ni precios de proveedores. Se
conservan sesiones/CSRF y separación Gerencia/Operaciones, sin dependencias nuevas.

## Oferta semanal C05 — 27/10/2026

Usuario autorizó gestión/publicación end-to-end, commit/push a tino y PR a main,
sin fusionar. Aprobó explícitamente conservar las publicadas sin edición en este
incremento (cambios mediante otro borrador); productos desactivados posteriormente
se ocultan al Cliente y no pueden pedirse, sin alterar pedidos históricos.
Se conserva el esquema existente y PedidoService/State, sin DDL ni dependencias.
Precio cero sigue permitido; límites numeric(12,2) se verificaron en Supabase.
Reglas, API y recorrido: [OFERTAS_SEMANALES.md](OFERTAS_SEMANALES.md).

## Prioridad académica Pedido + State — 27/10/2026

El usuario priorizó los patrones y autorizó iniciar Pedido + State para avanzar
hacia la demostración del jueves 29/10. Se conserva arquitectura y esquema actual.
Este incremento implementa creación/consulta y ciclo de seis estados, precios de
oferta históricos, asociación explícita del Cliente demo y fixtures separados
creados solo a petición de Operaciones en modo demo.
No se afirma integración completa con stock: reservas/movimientos, Strategy y
administración siguen pendientes. Las transiciones muestran ciclo de vida y no
acreditan por sí mismas recepción/preparación/entrega logística completas.
Reglas, participantes y recorrido: PEDIDO_STATE.md. No se agrega CANCELADO por
el solo hecho de existir en el CHECK de BD. Sin nuevas dependencias ni DDL.

## Demo local autorizada — 27/10/2026

El usuario autorizó mantener una cuenta local por rol para acelerar el MVP de
facultad. Para esta demo se sustituye la provisión de usuarios persistidos por
tres cuentas ficticias en memoria, habilitadas explícitamente por compose.demo.yaml.
Se conserva BCrypt en memoria, sesión y CSRF; no se modifica Supabase ni se aplica
password_hash. Producto/proveedor siguen persistiendo en la BD real. Los IDs de
identidad demo son negativos y no equivalen a clientes persistidos.
Se autoriza el circuito visible de alta/consulta y activación/desactivación de
maestros; no implica que estén implementados pedidos, compras o los cinco patrones.
Credenciales y recorrido: DEMO_LOCAL.md. El modo no se destina a publicación.

Autorización del Sprint 0; registro del 9 de octubre de 2026. Las entregas académicas son la referencia funcional principal. Las siguientes decisiones fueron aprobadas expresamente por el usuario y prevalecen sobre propuestas previas. Su registro no implica implementación funcional en Sprint 0.

| Decisión aprobada | Alcance |
|---|---|
| Login real simplificado | Spring Security, usuarios precargados y roles Cliente, Operaciones y Gerencia; futuras autoridades ROLE_CLIENTE, ROLE_OPERACIONES y ROLE_GERENCIA. Sin JWT ni OAuth. |
| Inventario | Reservas automáticas y movimientos de ingreso, egreso y ajuste. |
| Precios | Conservar historial de precios de proveedores. |
| Compra Quinteros | Autorización manual de Gerencia. |
| Administración | Facturación interna simplificada y registro de pagos. |
| Reportes | Solo totales de ventas y compras; sin cálculo de rentabilidad. |

**Recorte académico pendiente de validación docente:** excluir rentabilidad reduce el alcance de la Actividad 2 (Gestión y Reportería, módulos gerenciales) y del superprompt. Limitar reportes a ventas y compras también reduce los reportes allí descritos. No se presenta este recorte como conformidad docente ya obtenida.

La planificación original en PLANIFICACION_MVP.md permanece como propuesta histórica: sus referencias a margen/rentabilidad y reportes ampliados quedan sustituidas por este registro. La decisión de login real sustituye el selector de perfiles sugerido por el superprompt.

## Configuración del Sprint 0

- Monolito por capas; backend Java 21, Spring Boot 3.5.16 y Maven. Esta versión estable permite conservar JUnit 5 gestionado por spring-boot-starter-test.
- React, Vite, TypeScript y Tailwind mediante su plugin oficial de Vite. Las versiones declaradas se concretan en el lockfile cuando la instalación se complete.
- Perfil dev por defecto: HTTP sin DataSource/JPA. Perfil supabase: JDBC real con variables obligatorias, sin crear esquema. Usar solo uno a la vez.
- GET /api/health indica disponibilidad de la API, no estado de PostgreSQL.
- CORS limitado a los orígenes locales configurados. CSRF activo; sin mecanismos de autenticación funcionales ni usuario aleatorio de Spring Boot. BCrypt disponible como bean.

No se aprueban por este registro reglas adicionales de negocio, algoritmos, fórmulas ni el esquema definitivo. Strategy, State y las entidades permanecen fuera de Sprint 0.

## Decisiones aprobadas para Sprint 1 — 9 de octubre de 2026

Fuente: instrucción expresa del usuario de formalizar Sprint 1 del 10 al 16 de octubre. Este registro conserva las decisiones anteriores y distingue preparación, implementación futura y publicación. La presente ejecución es únicamente documental.

| Decisión aprobada | Alcance y efecto |
|---|---|
| Incorporar Docker Compose | Entorno de desarrollo local para backend y frontend. Sustituye la restricción «No utilizar Docker» de context/SUPERPROMPT.md y la prohibición histórica del Sprint 0 para el trabajo futuro; no altera el registro de lo autorizado/ejecutado en Sprint 0. |
| Preparar Render | Frontend como sitio estático y backend como contenedor Docker. Configuración/documentación del despliegue en S1-09. La publicación efectiva, creación de servicios públicos o activación de despliegue automático todavía no están autorizadas; no constituyen P2 ejecutable. |
| Conservar persistencia | PostgreSQL administrado en Supabase, conexión JDBC desde Spring Boot. Sin PostgreSQL local en Compose, ni React conectado directamente a BD. Verificar JDBC real en S1-02, distinto de health/IntelliJ. |
| Conservar arquitectura y stack | Monolito por capas; Java 21, Spring Boot, Maven/JUnit 5, React/Vite/TypeScript/Tailwind y PostgreSQL. Sin cambio de versiones por esta decisión, microservicios ni componentes innecesarios. |
| Compromiso Sprint 1 | Ocho P0: S1-01/02/03/04/05/06/09/10. Esquema inicial sujeto a aprobación previa del modelo. Login con sesiones, BCrypt, CSRF y roles; productos/proveedores/historial y consulta de oferta. |
| Trasladar pedidos/State | S1-07 crear/consultar pedidos y S1-08 confirmar con State pasan a Sprint 2. No opcionales del Sprint 1; exigencia académica de State y Strategy se conserva para el MVP. |

Las decisiones previamente aprobadas de stock/reservas, precios, autorización Quinteros, factura/pagos y reportes sin rentabilidad permanecen. Validación docente del recorte sigue pendiente. Las referencias históricas del apartado Sprint 0 y de otros documentos de cierre no deben leerse como prohibición vigente de Docker para Sprint 1.

### Detalles no aprobados por este registro

- Modelo físico inicial, campos/cardinalidades, representación Rol/Usuario/Cliente, restricciones, precisión monetaria, esquema destino/cuenta de aplicación y procedimiento/datos SQL. Aprobar antes de generar entidades o escribir/aplicar esquema.
- Historial de precios: versiones de ProductoProveedor versus relación estable con entidad de historial; vigencias, precio actual, correcciones y solapamientos.
- Oferta: modelo, vigencia/selección actual, origen del precio de venta y datos iniciales. Consulta aprobada no equivale a aprobación automática de esas reglas.
- Sesiones/CSRF/permisos: contrato y matriz concreta, duración/cookies locales; producción con subdominios propios, URLs separadas o proxy. No asumir que CORS resuelve cookies; no introducir JWT/OAuth ni desactivar CSRF.
- Detalles Docker inicialmente pendientes en la formalización: resueltos para S1-01 por la autorización específica registrada a continuación; la topología de producción sigue pendiente.
- Plan/costos/dominio público y publicación Render: no autorizados. Preparación permitida; despliegue real requiere instrucción expresa adicional.

Las reglas de reservas y transiciones de pedidos se concretarán al planificar Sprint 2. Ver [planificación vigente](PLANIFICACION_SPRINT_1.md) y [documento operativo](SPRINT_1.md) para responsables, dependencias, criterios y trazabilidad. No se requiere otra actualización general del alcance para comenzar las subtareas independientes en la siguiente etapa autorizada.

## Implementación autorizada S1-01 — 9 de octubre de 2026

El usuario autorizó expresamente implementar solo S1-01 y aprobó: Dockerfiles multi-stage backend/frontend; Java 21; Node para compilar React/Vite y Nginx para servir; proxy inverso /api/ desde Nginx hacia Spring Boot; Compose con únicamente frontend/backend; primer arranque exclusivamente dev, sin PostgreSQL.

La aprobación concreta sustituye las alternativas pendientes de construcción multietapa y Nginx frente a Vite para este entorno local. Las sesiones/cookies de producción, modelo/historial y demás decisiones de negocio continúan pendientes. Este proxy local no aprueba por sí solo una topología de producción Render.

| Detalle implementado | Justificación / límite |
|---|---|
| Contextos backend/frontend y .dockerignore de lista permitida | Construir desde fuentes, excluyendo target/node_modules/dist, .env, Git y archivos ajenos; no copiar secretos |
| Maven 3.9.9 / Java 21; mvn clean verify dentro del build | Tests obligatorios antes de empaquetar; sin skipTests; no cambia POM ni Spring Boot |
| Runtime Temurin 21.0.12.1_1 Alpine 3.23 y usuario comunitarias | Java 21 como el JDK localizado; usuario sin privilegios; curl solo para healthcheck HTTP |
| Node 24.21.0 Alpine 3.23 y npm ci; Nginx 1.30.5 Alpine 3.24 | Node compatible con engine existente; lockfile y versiones React/Vite/TypeScript/Tailwind conservados; Nginx estable como servidor aprobado |
| VITE_API_BASE_URL vacío solo en build Docker | Fetch produce /api/health del origen frontend; nativo conserva URL/default localhost:8080. No se modifica código React |
| Proxy /api/ y fallback SPA | Conservar prefijo/query, estados de seguridad y Host con puerto; backend solo se resuelve dentro de Docker; resolver DNS para recreación |
| Red bridge; frontend 127.0.0.1:8081→80 y backend 127.0.0.1:8080→8080 | Acceso local, diagnóstico directo y mismo origen por proxy; puertos host configurables sin cambiar upstream interno |
| Healthchecks y depends_on service_healthy | Backend listo antes de crear frontend; frontend comprueba Nginx/estáticos. Ningún healthcheck certifica JDBC |
| compose.yaml fija dev | Evita que variables del host cambien el primer arranque o activen supabase sin intención |
| compose.supabase.yaml preparatorio | Override explícito exige variables existentes y sustituye dev por supabase, sin servicio adicional, SQL ni credenciales reales. No ejecutado; verificación JDBC sigue en S1-02 |

Tags de imágenes explícitos, sin latest, contrastados con publicaciones oficiales; su pull/build y digests no pudieron comprobarse sin Docker. Las etiquetas no son digests inmutables. No se añaden dependencias Maven/npm; paquetes de imagen son herramientas de ejecución/healthcheck.

Estado: archivos implementados, S1-01 En progreso por pruebas Docker pendientes. No modificar Spring Security/CORS nativo para eludir pruebas; Compose suministra orígenes locales apropiados. No se implementó login, dominio/SQL ni se publicó Render. Alcance del sprint no cambió: PLANIFICACION_SPRINT_1.md se conserva; avance y resultados en SPRINT_1.md.

Referencias de ejecución: [multi-stage](https://docs.docker.com/build/building/multi-stage/), [depends_on y salud](https://docs.docker.com/compose/how-tos/startup-order/), [proxy Nginx](https://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_pass). Los comandos/resultados reales y las verificaciones pendientes están documentados; no se presenta un build Docker como exitoso.
