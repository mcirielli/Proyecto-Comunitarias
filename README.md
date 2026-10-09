# Comunitarias

MVP académico UADE. Sprint 0 cerrado con observaciones; S1-01 incorpora Docker/Compose para infraestructura local. **S1-01: En progreso, pendiente de validación Docker en Windows.** No hay login funcional ni módulos comerciales. No se publicó en Render ni se hicieron commits/push en esta implementación.

## Ejecutar con Docker después de clonar

Requisitos: acceso al repositorio privado, Git y Docker Desktop para Windows iniciado en modo **Linux containers**, con Docker Compose v2 (se recomienda 2.20 o superior). El equipo debe poder descargar imágenes oficiales, dependencias Maven/npm y paquetes Alpine. Para Docker no hace falta instalar Java, Maven, Node ni Nginx en el host.

Cuando estos cambios estén incorporados al repositorio por una acción autorizada, un compañero puede ejecutar en PowerShell:

~~~powershell
git clone https://github.com/mcirielli/Proyecto-Comunitarias.git
cd Proyecto-Comunitarias
docker version
docker compose version
docker compose config
docker compose build --no-cache
~~~

Si el build falla, conservar el error y resolverlo antes de arrancar; no saltar pruebas. Luego:

~~~powershell
docker compose up -d
docker compose ps
docker compose logs --tail=100 backend frontend
~~~

Esperar a que ambos servicios aparezcan healthy. El frontend se crea después de que el backend supere su healthcheck; no tomar el retorno de up como prueba del circuito React.

- Aplicación: **http://localhost:8081**.
- API directa para diagnóstico: **http://localhost:8080/api/health**.
- API por Nginx: **http://localhost:8081/api/health**.
- Puertos internos: backend 8080 y frontend 80; puertos publicados solo en 127.0.0.1.

El primer arranque con compose.yaml fija **solo dev**, incluso si la terminal tiene SPRING_PROFILES_ACTIVE=supabase. No requiere .env ni Supabase y no incluye PostgreSQL local. dev excluye DataSource/JPA. No se generan tablas ni se ejecuta SQL.

Si el 8080 ya está usado por el backend local, definir antes de up/build/config:

~~~powershell
$env:BACKEND_HOST_PORT = '8082'
~~~

La API directa pasa a http://localhost:8082/api/health; el proxy interno sigue usando backend:8080. FRONTEND_HOST_PORT permite cambiar 8081; cambiar también la URL usada en las comprobaciones. Los orígenes CORS del contenedor se adaptan a ese puerto. El servicio backend es un hostname **solo para Nginx/red Docker**, no para el navegador.

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

Nginx conserva /api/ al enviar al backend, transmite Host con puerto y headers proxy, preserva estados 401/403 y no reemplaza errores API por HTML. El fallback SPA se aplica a rutas frontend. El resolver Docker permite volver a resolver backend tras recreación. Su funcionamiento en contenedores aún requiere prueba real.

## Checklist Windows para aceptar S1-01

Estos comandos/casos son **pendientes**, no resultados de contenedores verificados por el agente:

~~~powershell
docker compose config
docker compose build --no-cache
docker compose up -d
docker compose ps
# Validar sintaxis Nginx después de construir
docker compose exec frontend nginx -t
# GET desde el backend y por proxy; ambos deben devolver 200 y JSON UP
curl.exe -i http://localhost:8080/api/health
curl.exe -i http://localhost:8081/api/health
# Verificar que proxy conserva la protección; ambos deben devolver 401
curl.exe -i http://localhost:8080/api/recurso-futuro
curl.exe -i http://localhost:8081/api/recurso-futuro
# Fallback SPA: 200 y HTML de React, no un error Nginx
curl.exe -i http://localhost:8081/ruta-de-prueba-spa
# Perfil de la primera prueba
docker compose exec backend printenv SPRING_PROFILES_ACTIVE
docker compose exec backend java -version
~~~

Abrir http://localhost:8081: debe verse «API disponible». En DevTools > Network comprobar petición **http://localhost:8081/api/health**, HTTP 200 y JSON application=Comunitarias/status=UP; no http://backend ni llamada directa al puerto 8080. Registrar captura y resultado.

~~~powershell
# Mantener frontend levantado y detener solo la API
docker compose stop backend
~~~

En la pantalla, pulsar «Volver a comprobar»: debe mostrar «API no disponible»; Nginx puede devolver 502/504 cuando el upstream está caído. Después:

~~~powershell
docker compose start backend
docker compose ps
~~~

Esperar backend healthy y repetir desde la pantalla: vuelve a disponible. Verificar también recreación completa mediante down/up y que el proxy recupera el upstream. El healthcheck frontend indica Nginx/estáticos, no disponibilidad de PostgreSQL ni garantía de API.

Revisar que los contextos e imágenes no contienen .env/credenciales/artefactos del host. En la imagen runtime backend solo está app.jar, no fuentes ni target host; en frontend solo dist/config Nginx, no node_modules. Ejemplos de inspección sin imprimir entorno secreto:

~~~powershell
docker compose exec backend ls -la /app
docker compose exec frontend ls -la /usr/share/nginx/html
docker compose images
~~~

No compartir docker inspect/config del futuro perfil supabase sin sanitizar: puede revelar variables secretas. Completar builds/pruebas nativos indicados a continuación y registrar resultados antes de marcar S1-01 Terminada.

## Ejecución nativa fuera de Docker

Se conserva el comportamiento actual: Vite en localhost:5173 llama directamente a localhost:8080 por defecto. Docker no cambia fuentes React, propiedades Spring ni Spring Security. VITE_API_BASE_URL se fija a cadena vacía únicamente en el build Docker para que Fetch use /api/health relativo.

Requisitos nativos: JDK 21 en JAVA_HOME/PATH, Maven >=3.6.3 (sin wrapper; IntelliJ puede usar el incorporado), Node >=22.12 (Node 24 recomendado), npm y acceso a registros.

Terminal backend desde la raíz (detener contenedores o cambiar puertos si hay conflicto):

~~~powershell
cd backend
mvn clean verify
if ($LASTEXITCODE -ne 0) { throw 'Resolver el fallo de Maven antes de arrancar' }
mvn spring-boot:run
~~~

Perfil dev por defecto. Para forzarlo si la terminal tenía otro perfil: definir $env:SPRING_PROFILES_ACTIVE = 'dev' antes de arrancar. En otra terminal desde raíz:

~~~powershell
cd frontend
# Solo copiar si no existe una .env local; conservar valores propios
if (!(Test-Path -LiteralPath .env)) { Copy-Item .env.example .env }
npm.cmd ci
if ($LASTEXITCODE -ne 0) { throw 'Resolver npm ci antes de compilar' }
npm.cmd run build
if ($LASTEXITCODE -ne 0) { throw 'Resolver el build antes de arrancar' }
npm.cmd run dev
~~~

Abrir http://localhost:5173. Ejemplo frontend/.env: VITE_API_BASE_URL=http://localhost:8080. Fuera de Docker ese valor y el default siguen funcionando; una base vacía requiere un proxy del mismo origen y no debe copiarse a la configuración nativa sin prepararlo.

## Variables y preparación futura Supabase

| Variable | Uso |
|---|---|
| SPRING_PROFILES_ACTIVE | compose.yaml fija dev; override explícito fija solo supabase; nativo dev por defecto |
| SERVER_PORT | Backend interno 8080; default nativo 8080 |
| BACKEND_HOST_PORT | Puerto publicado backend Compose, default 8080 |
| FRONTEND_HOST_PORT | Puerto publicado Nginx Compose, default 8081 |
| CORS_ALLOWED_ORIGINS | Nativo localhost/127.0.0.1:5173; Compose agrega su origen frontend |
| SUPABASE_JDBC_URL | JDBC de Connect/Session Pooler, con SSL |
| SUPABASE_DB_USER / SUPABASE_DB_PASSWORD | Credenciales propias, solo entorno de ejecución backend |
| VITE_API_BASE_URL | Público; default nativo http://localhost:8080; build Docker vacío |

La .env.example raíz conserva placeholders. Spring no carga .env automáticamente; Vite sí carga su .env local. Compose interpola variables, pero compose.yaml no transmite secretos Supabase en dev. Nunca poner credenciales en VITE_*, Dockerfiles, capas ni argumentos de build.

**S1-02 futura:** compose.supabase.yaml prepara un override sin nuevos servicios. No se activó en S1-01. Antes de usarlo, obtener variables propias del panel Connect y revisar la tarea JDBC; no inferir conexión desde health ni desde SELECT 1 en IntelliJ.

Ejemplo futuro en PowerShell, **no ejecutado ni necesario para la primera prueba**:

~~~powershell
$env:SUPABASE_JDBC_URL = Read-Host 'JDBC de Session Pooler con sslmode=require'
$env:SUPABASE_DB_USER = Read-Host 'Usuario de base de datos'
$clave = Read-Host 'Contraseña de base de datos' -AsSecureString
$env:SUPABASE_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $clave).Password
# Eliminar contenedores dev antes de cambiar de configuración
docker compose down
# Validar sin imprimir secretos; variables vacías hacen fallar el override
docker compose -f compose.yaml -f compose.supabase.yaml config --quiet
docker compose -f compose.yaml -f compose.supabase.yaml up -d
~~~

El override **sustituye** dev por supabase; no combinar perfiles. Mantiene ddl-auto=none y sql.init.mode=never y no crea esquema. El éxito JDBC requiere prueba desde DataSource Spring/SELECT 1 en S1-02. Para volver a dev: down con ambos archivos y up con compose.yaml solo; el primer Compose vuelve a fijar dev. Al finalizar retirar variables de la terminal:

~~~powershell
Remove-Item Env:SUPABASE_DB_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:SUPABASE_DB_USER -ErrorAction SilentlyContinue
Remove-Item Env:SUPABASE_JDBC_URL -ErrorAction SilentlyContinue
~~~

## Estado de verificación y seguridad

En el entorno del agente Docker no está disponible: docker compose config falló ENOENT; build/up/ps/Nginx/proxy/navegador Docker no se ejecutaron. npm ci limpio falló EACCES contra npm; build estándar llegó a Vite y falló por acceso denegado al cargar configuración. Build Vite alternativo con loader runner y base vacía sí pasó usando dependencias existentes del host; no equivale a build Docker o instalación limpia. Maven clean verify fue intentado con Java 21 y falló por recursos/permisos; se mantienen errores y detalles en [SPRINT_1.md](docs/SPRINT_1.md).

Solo GET /api/health es público; rutas futuras protegidas, BCrypt preparado y CSRF activo. No se modificó Spring Security para Docker. Las seis pruebas existentes se repitieron mediante surefire:test con Java 21: 6 ejecutadas, 0 fallos, 0 errores, 0 omitidas, usando clases compiladas existentes; no acreditan clean verify ni imagen Docker.

La conexión Supabase desde IntelliJ/SELECT 1 y el primer push fueron reportados en el cierre S0; la conexión Spring permanece pendiente de S1-02. Git local ya existe. No hacer commits/push/publicación automáticamente. Gitignore mantiene fuera entornos/dependencias/generados y los contextos Docker son mínimos.

Documentación: [Arquitectura](docs/ARQUITECTURA.md), [Decisiones](docs/DECISIONES_ARQUITECTURA.md), [Sprint 1 operativo](docs/SPRINT_1.md), [Planificación vigente](docs/PLANIFICACION_SPRINT_1.md), [Cierre Sprint 0](docs/SPRINT_0.md). Preparar Render está aprobado para S1-09; publicar no. Rentabilidad excluida, pendiente de validación docente.
