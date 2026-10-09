# Comunitarias

Proyecto académico de Proceso de Desarrollo de Software de UADE: gestión mayorista para kioscos con venta semanal/pedidos, Compra Quinteros y gestión/reportes. Equipo de cuatro; presentación **31 de octubre de 2026**. Monolito por capas, Java/POO y cinco patrones seleccionados: MVC, Repository, Service Layer, Strategy y State.

## Empezar aquí

| Necesidad | Documento |
|---|---|
| Elegir tarea y ver estados por sprint | [Kanban por sprints](docs/KANBAN_SPRINTS.md) |
| Saber qué falta y su respaldo académico | [MVP pendientes y trazabilidad](docs/MVP_PENDIENTES.md) |
| Aprender flujo POO/UML/Spring/React/Git | [Guía de desarrollo](docs/GUIA_DESARROLLO.md) |
| Ejecutar, probar y diagnosticar contenedores | [Guía Docker](docs/GUIA_DOCKER.md) |
| Alcance único aprobado del Sprint 1 | [Planificación formal](docs/PLANIFICACION_SPRINT_1.md) |
| Criterios, evidencias y DoD del Sprint 1 | [Documento operativo](docs/SPRINT_1.md) |
| Arquitectura y decisiones | [Arquitectura S0](docs/ARQUITECTURA.md), [decisiones](docs/DECISIONES_ARQUITECTURA.md) |
| Cierre de infraestructura inicial | [Sprint 0](docs/SPRINT_0.md), [reporte](docs/REPORTE_SPRINT_0.md) |

Leer también [instrucciones](AGENTS.md) y [contexto](context/SUPERPROMPT.md). [Planificación MVP original](docs/PLANIFICACION_MVP.md) es histórica: decisiones posteriores sustituyen login simulado, reportes amplios/calendario anterior. No restaurar recortes ni requisitos obsoletos a partir de ella.

## Stack y requisitos

Backend **Java 21, Spring Boot 3.5.16, Maven/JUnit 5**, Web, Data JPA, Security, Bean Validation y PostgreSQL Driver. Frontend **React, Vite, TypeScript, Tailwind CSS y Fetch**; versiones se conservan en package.json/lockfile. PostgreSQL administrado en **Supabase**, conexión desde Java por JDBC. Sin microservicios, PostgreSQL local ni nuevas dependencias.

Para Docker: Git, acceso al repositorio privado, Docker Desktop con motor Linux/Compose v2, red para descargar dependencias/imágenes y puertos 8080/8081 libres. No exige Java/Node del host. Para IDE/nativo: JDK 21, Maven >=3.6.3 y Node >=22.12/npm; sin Maven Wrapper. Comandos y configuración IntelliJ en guía de desarrollo.

## Quick Start Docker

Desde PowerShell:

~~~powershell
git clone https://github.com/mcirielli/Proyecto-Comunitarias.git
cd Proyecto-Comunitarias
docker compose config --quiet
if ($LASTEXITCODE -ne 0) { throw 'Resolver la configuración antes de continuar' }
docker compose up -d --build
if ($LASTEXITCODE -ne 0) { throw 'Resolver build/arranque antes de continuar' }
docker compose ps
~~~

Esperar ambos healthy; abrir **http://localhost:8081**. Backend directo **http://localhost:8080/api/health**; health por Nginx **http://localhost:8081/api/health**. Pantalla técnica «API disponible», sin pantallas comerciales todavía.

Compose fija **solo dev**, excluye DataSource/JPA y funciona sin credenciales/Supabase ni .env; no ejecuta SQL. El navegador usa /api/health relativo, Nginx resuelve backend:8080 internamente. Host backend no es una URL de navegador. Para detener: docker compose down.

## Estado real del MVP

- Sprint 0 cerrado **APROBADO CON OBSERVACIONES**. Errores y límites conservados en el reporte, sin ocultarlos.
- **S1-01 En revisión final**: builds/Docker/healthy/proxy/SPA/UI/error-recuperación y revisión integrantes 3/4 aprobados según usuario. Controles iniciales de seguridad sin indicadores reportados; falta inspeccionar contenido de capas de las imágenes entregadas. Auditar todas las imágenes históricas no es requisito.
- Implementado: infraestructura Spring/React y GET /api/health; filtros iniciales/CORS/BCrypt. Sin login funcional, entidades, SQL, maestros, oferta comercial, pedidos, compras, inventario, factura/pagos o reportes.
- Supabase: conexión IntelliJ/SELECT 1 reportada; JDBC desde DataSource Spring **pendiente S1-02**. Health no consulta BD.
- Render: **despliegue realizado según el usuario, con verificación funcional y configuración pendientes**. No se aportaron URL pública, servicio, rama ni variables configuradas. No se ejecutó despliegue en esta tarea documental.
- Reportes aprobados solo totales ventas/compras; exclusión de rentabilidad y reportes ampliados **pendiente de validación docente**.

Resultados reportados no se presentan como pruebas ejecutadas por el agente. La lectura actual de código confirma el alcance descrito; esta actualización no repite builds/JDBC ni implementa funcionalidades.

## Reconstruir, detener y revisar

Desde la raíz:

~~~powershell
# Reconstrucción normal y recreación al cambiar fuentes/configuración
docker compose build
docker compose up -d
# Logs continuos; Ctrl+C sale de logs sin detener servicios
docker compose logs -f --tail=100 backend frontend
# Detener sin eliminar contenedores
docker compose stop
# Volver a iniciar los contenedores existentes
docker compose start
# Detener y quitar contenedores/red del proyecto; no elimina imágenes
docker compose down
~~~

Para una verificación desde cero usar build --no-cache. Cada Dockerfile construye desde fuentes: backend ejecuta mvn clean verify con tests; frontend npm ci y npm run build. No se usan target/node_modules/dist del host, ni se copian .env locales. No se añadieron dependencias Maven/npm ni se cambió el lockfile.

Imágenes: maven:3.9.9-eclipse-temurin-21, eclipse-temurin:21.0.12.1_1-jre-alpine-3.23, node:24.21.0-alpine3.23 y nginx:1.30.5-alpine3.24. Los tags explícitos evitan latest pero no son digests inmutables; registrar los digests reales al verificar Docker. Backend ejecuta como usuario comunitarias sin privilegios; curl se incorpora solo para su healthcheck. Nginx usa wget disponible en Alpine para comprobar los archivos estáticos.

Nginx conserva /api/ al enviar al backend, transmite Host con puerto y headers proxy, preserva estados 401/403 y no reemplaza errores API por HTML. El fallback SPA se aplica a rutas frontend. El resolver Docker permite volver a resolver backend tras recreación. Su funcionamiento principal en contenedores fue validado según el usuario; las limitaciones de seguridad se registran en docs/SPRINT_1.md.


## Entornos y variables

| Entorno | Frontend/backend | Base de datos y límites |
|---|---|---|
| IDE/nativo dev | Vite 5173 → Spring 8080 | HTTP sin DataSource; default API directa |
| Compose dev | Nginx 8081 → backend:8080 | Sin BD; primer arranque existente |
| Perfil supabase local | IDE o override compose.supabase.yaml | PostgreSQL remoto JDBC, requiere variables reales; verificación Spring pendiente |
| Render | Diseño aprobado Static Site + Web Service Docker | Despliegue reportado; configuración concreta y conectividad por verificar; no asumir Compose |

| Variable | Uso existente |
|---|---|
| SPRING_PROFILES_ACTIVE | dev por defecto/fijo en Compose; override reemplaza por solo supabase |
| SERVER_PORT | Puerto Spring, default 8080; compatibilidad PORT Render por verificar |
| BACKEND_HOST_PORT / FRONTEND_HOST_PORT | Defaults 8080/8081, publicación loopback; permite resolver conflictos |
| CORS_ALLOWED_ORIGINS | Lista explícita; local 5173 y Compose agrega puerto Nginx |
| SUPABASE_JDBC_URL / SUPABASE_DB_USER / SUPABASE_DB_PASSWORD | Solo entorno backend perfil supabase; sin valores reales aquí |
| VITE_API_BASE_URL | Público, nativo localhost:8080/default; build Docker vacío |

Spring no carga .env automáticamente. Compose interpola y transmite solo lo declarado; Vite expone VITE_* en build. Nunca poner secretos en VITE_*, Git, build args, capas o logs compartidos. ddl-auto=none/sql.init.mode=never: esquema solo tras aprobación. Procedimiento seguro de override, ejecución nativa y resolución de problemas en las guías, sin cadenas JDBC completas.

## Colaboración y sincronización

[Repo Cirielli](https://github.com/mcirielli/Proyecto-Comunitarias) es el fork; [repo Andrés](https://github.com/acedenosanchez/Proyecto-Comunitarias) es upstream en la configuración local comprobada. Cada compañero revisa git remote -v y permisos propios. Usuario confirmó push, [PR #1](https://github.com/acedenosanchez/Proyecto-Comunitarias/pull/1) y merge; log local **65cd3c1** registra ese merge. Estado remoto actual no comprobado en esta ejecución.

Trabajar en feature/... o docs/..., realizar commit de rutas revisadas, push al fork y PR con base/head correctos; solicitar revisión cruzada, tests y DoD antes de merge. No push directo a Andrés ni force push. [Flujo completo Git/PR/sincronización](docs/GUIA_DESARROLLO.md). Para documentación y Render usar [skip render] según guía oficial si corresponde; verificar rama/auto-deploy antes de publicar. No activar despliegues desde estas instrucciones.

## Datos Render por completar (S1-09)

URL frontend: pendiente. URL backend: pendiente. Nombre/tipo real de servicios: pendiente. Repo/rama/SHA desplegado: pendiente. Contexto Docker/build/dist/PORT/perfil/CORS: pendiente de verificar. Nombres de variables configuradas: pendiente, sin valores sensibles. Auto-deploy, cookies/CSRF/JDBC y smoke tests: pendientes. Checklist y alcance en Kanban; no se afirma que la configuración diseñada sea la realmente desplegada.
