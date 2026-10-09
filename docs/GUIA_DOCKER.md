# Guía Docker — primera ejecución y uso diario

Guía operativa de la configuración existente, sin cambios de Dockerfiles/Compose. [Kanban](KANBAN_SPRINTS.md), [DoD S1-01](SPRINT_1.md) y [guía desarrollo](GUIA_DESARROLLO.md). Pruebas funcionales Docker y revisión 3/4 aprobadas según usuario; **S1-01 En revisión final** por falta de comprobación de contenido de capas de las imágenes entregadas.

## 1. Requisitos

Docker Desktop iniciado, motor de contenedores Linux accesible, Compose v2 (README previo recomienda >=2.20), Git/acceso al repo privado, Internet para imágenes/dependencias y puertos 8080/8081 libres. No instalar PostgreSQL local. No hacen falta Java/Maven/Node/Nginx en el host para Compose.

## 2. Primera ejecución

En PowerShell:

~~~powershell
git clone https://github.com/mcirielli/Proyecto-Comunitarias.git
cd Proyecto-Comunitarias
docker version
docker compose version
docker compose config --quiet
if ($LASTEXITCODE -ne 0) { throw 'Resolver Compose antes de continuar' }
docker compose up -d --build
if ($LASTEXITCODE -ne 0) { throw 'Resolver build/arranque antes de continuar' }
docker compose ps
~~~

Esperar ambos healthy. El primer build descarga herramientas/dependencias; up puede tardar. Frontend depende de backend service_healthy.

| URL | Uso |
|---|---|
| http://localhost:8081 | React servido por Nginx |
| http://localhost:8080/api/health | Backend directo: HTTP 200/UP |
| http://localhost:8081/api/health | Health mediante proxy Nginx |

Hoy pantalla inicial muestra «API disponible», botón «Volver a comprobar» y errores. No hay login ni pantallas operativas. dev ejecuta HTTP **sin Supabase**, excluye DataSource/JPA y no crea tablas. El healthcheck frontend prueba Nginx/estáticos; no garantiza API/JDBC.

## 3. Qué se construye

| Servicio | Build desde fuentes | Runtime |
|---|---|---|
| backend | Maven 3.9.9/Java 21; clean verify con tests, sin target host | Temurin 21.0.12.1_1 JRE Alpine 3.23, usuario comunitarias, curl health |
| frontend | Node 24.21.0 Alpine 3.23; npm ci con lockfile y npm run build, sin node_modules/dist host | Nginx 1.30.5 Alpine 3.24 y dist generado |

Dos servicios, red bridge comunitarias. Backend interno 8080, frontend interno 80; host loopback 8080/8081. Tags conservados, no digests inmutables. Las exclusiones .dockerignore limitan contexto y bloquean .env/credenciales/Git/generados. No pasar secretos como ARG/ENV de build ni VITE_*.

Navegador → localhost:8081/api/ → Nginx → backend:8080/api/. Hostname backend solo sirve dentro de Docker. VITE_API_BASE_URL vacío solo en build Docker genera ruta relativa; ejecución nativa conserva localhost:8080/default. Nginx usa resolver DNS Docker, conserva prefijo/query/estados HTTP, y aplica fallback index.html únicamente a rutas SPA.

## 4. Comandos diarios

Desde raíz:

~~~powershell
# Crear/iniciar servicios con imágenes actuales
docker compose up -d
# Cambié fuentes/configuración: reconstruir y recrear
docker compose up -d --build
# Consultar estado y salud
docker compose ps
# Ver diagnóstico reciente (revisar localmente; no compartir secretos)
docker compose logs --tail=100 backend frontend
# Seguimiento continuo; Ctrl+C no detiene servicios
docker compose logs -f --tail=100 backend frontend
# Detener y quitar contenedores/red de este proyecto
docker compose down
~~~

Reiniciar no compila código; start inicia contenedores ya creados y restart conserva su imagen/configuración. Build construye imágenes; up recrea contenedores cuando corresponde. Git pull actualiza fuentes, no una imagen ejecutándose: reconstruir después. No hay bind mounts/hot reload en este Compose.

~~~powershell
# Parar y recuperar solo API, por ejemplo para probar UI
docker compose stop backend
docker compose start backend
# Build sin caché para validación limpia, seguido de recreación
docker compose build --no-cache
if ($LASTEXITCODE -ne 0) { throw 'Resolver el build antes de continuar' }
docker compose up -d
~~~

Backend Docker ejecuta Maven verify; frontend npm ci/build. No omitir pruebas para eludir fallos. No ejecutar prune global ni down -v: este proyecto no usa PostgreSQL/volúmenes de BD locales y no requiere borrar datos ajenos.

## 5. Verificación funcional reproducible

Estos son comandos para repetir pruebas; no nuevos resultados observados por el agente:

~~~powershell
docker compose config --quiet
docker compose ps
docker compose exec frontend nginx -t
curl.exe --silent --output NUL --write-out "%{http_code}" http://localhost:8080/api/health
curl.exe --silent --output NUL --write-out "%{http_code}" http://localhost:8081/api/health
curl.exe --silent --output NUL --write-out "%{http_code}" http://localhost:8081/api/recurso-futuro
curl.exe --silent --output NUL --write-out "%{http_code}" http://localhost:8081/ruta-de-prueba-spa
docker compose exec backend printenv SPRING_PROFILES_ACTIVE
~~~

Esperados: config sin error, ambos healthy, nginx successful, health 200/UP, ruta protegida 401, SPA 200 HTML, solo dev. Solicitud 401 no demuestra que exista ese controlador: la seguridad rechaza antes de resolver una futura ruta. En navegador Network: /api/health del origen 8081; disponible. Detener API, volver a comprobar: no disponible y 502 reportado; start, esperar healthy y volver a comprobar: disponible/200 reportado.

Inspección de seguridad inicial favorable reportada: metadatos, historial, logs, bundle y archivos visibles. Falta inspección de **contenido de todas las capas de las dos imágenes entregadas**, incluidos archivos ocultos/eliminados; ver SPRINT_1.md §12. No es requisito revisar todas las imágenes históricas del equipo. No publicar salidas crudas de inspect/config/history/logs con secretos. Los tests/versiones/evidencias reales se conservan en documentos de sprint.

## 6. Perfil supabase: preparación existente para S1-02

Se requiere **solo supabase**, nunca dev,supabase. Las tres variables reales, obtenidas localmente de la configuración Supabase y con SSL según guía del proyecto, son SUPABASE_JDBC_URL, SUPABASE_DB_USER y SUPABASE_DB_PASSWORD. No se incluyen cadenas JDBC completas ni valores reales aquí. dev no transmite esas variables.

Compose puede leer .env para interpolar, pero no lo inyecta automáticamente todo al contenedor: el override lo hace explícitamente mediante environment. Spring tampoco carga archivos .env por sí solo. Vite carga frontend/.env y sus VITE_* son públicos. [Documentación oficial de variables Compose](https://docs.docker.com/compose/how-tos/environment-variables/set-environment-variables/).

Ejemplo futuro para preparar la verificación S1-02, no necesario para primera ejecución dev, sin modificar tablas:

~~~powershell
$env:SUPABASE_JDBC_URL = Read-Host 'URL JDBC privada obtenida del panel Connect con SSL'
$env:SUPABASE_DB_USER = Read-Host 'Usuario de base de datos'
$clave = Read-Host 'Contraseña de base de datos' -AsSecureString
$env:SUPABASE_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $clave).Password
docker compose down
docker compose -f compose.yaml -f compose.supabase.yaml config --quiet
if ($LASTEXITCODE -ne 0) { throw 'Revisar variables sin imprimir sus valores' }
docker compose -f compose.yaml -f compose.supabase.yaml up -d --build
docker compose -f compose.yaml -f compose.supabase.yaml ps
~~~

El override sustituye dev por supabase; no agrega PostgreSQL local. Credenciales quedan en entorno del proceso/contenedor, no en imagen. No compartir docker inspect/config/logs sin revisión. Mantiene ddl-auto=none/sql.init.mode=never: no genera ni aplica schema/seed. Arrancar o recibir UP no acredita JDBC; S1-02 necesita Connection/SELECT 1 del DataSource Spring local y contenedor. No existe hoy una prueba JDBC ni endpoint BD que ejecutar para declararla aprobada.

Volver a dev y retirar valores de terminal:

~~~powershell
docker compose -f compose.yaml -f compose.supabase.yaml down
Remove-Item Env:SUPABASE_DB_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:SUPABASE_DB_USER -ErrorAction SilentlyContinue
Remove-Item Env:SUPABASE_JDBC_URL -ErrorAction SilentlyContinue
$clave = $null
docker compose up -d
~~~

## 7. Solución de problemas

| Síntoma | Procedimiento seguro |
|---|---|
| Docker no encontrado/daemon no disponible | Abrir Docker Desktop, esperar motor Linux, comprobar docker version/compose version; reiniciar terminal si PATH cambió. No instalar componentes de negocio para resolverlo |
| Puerto ocupado | Detener ejecución duplicada o definir puertos host libres en terminal; recrear con up. Proxy sigue backend:8080 |
| Backend unhealthy | ps y logs recientes localmente; revisar build/Java/perfil. En dev no debería requerir DB. En supabase revisar variables/conectividad; no desactivar seguridad/tests |
| HTTP 502 | Nginx funciona, upstream no disponible; start backend, esperar healthy, reintentar. Revisar ambos logs y red si persiste |
| Frontend disponible/API no disponible | Comprobar directo y proxy; Network origen/URL; no usar hostname backend en navegador; distinguir 401, 502 y formato JSON |
| Cambios no aparecen | Confirmar rama/fuentes guardadas; up -d --build; recargar navegador. restart no reconstruye |
| Git/repo privado inaccesible | Verificar cuenta/autorización/remotos; solicitar acceso al propietario; no poner token en URL ni forzar historial |
| Permisos de archivos/build | Conservar error exacto, verificar acceso/antivirus/OneDrive/paths y herramientas; no saltar Maven tests ni modificar Spring Security |
| Supabase falla | Solo perfil supabase; variables explícitas; destino Connect/SSL/usuario/red/pooler correctos; prueba SELECT 1 desde Spring, sin imprimir cadena/clave |
| Health UP pero BD no probada | Es endpoint técnico sin consulta BD; completar S1-02, no afirmar conexión exitosa |

Cambiar puertos host antes de up/config:

~~~powershell
$env:BACKEND_HOST_PORT = '8082'
$env:FRONTEND_HOST_PORT = '8083'
docker compose up -d
~~~

Frontend pasa a localhost:8083 y diagnóstico backend localhost:8082/api/health; Compose adapta CORS y proxy interno sin cambiar código. Elegir puertos realmente libres. Retirar estas variables si se desea volver a defaults y recrear.

## 8. Local, Supabase y Render

Compose ejecuta solo frontend/backend local; Supabase es PostgreSQL remoto para el perfil adecuado. Render es otro ambiente: arquitectura aprobada frontend Static Site y backend Web Service Docker independientes. El despliegue concreto fue reportado, con URLs, servicios, repo/rama, variables por nombre y validación pendientes. No afirmar que Render usa Compose ni que el proxy local existe en un Static Site. La URL pública de API, CORS y sesiones/cookies deben comprobarse allí en S1-09. No ejecutar deploy ni crear servicios desde esta guía.
