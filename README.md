# Comunitarias

MVP académico UADE. Sprint 0: estructura, configuración y conexión técnica frontend/backend. No hay módulos comerciales ni login funcional.

## Requisitos

- JDK 21 (JAVA_HOME apuntando al JDK y java/javac en PATH).
- Maven 3.6.3 o superior; no hay Maven Wrapper. IntelliJ permite seleccionar su Maven incorporado.
- Node.js 22.12 o superior (Node 24 recomendado) y npm.
- Acceso a Maven Central y al registro npm para descargar dependencias.
- Credenciales propias de Supabase únicamente para el perfil supabase.

## Ejecución local

En una terminal PowerShell, desde la raíz:

```powershell
cd backend
mvn verify
mvn spring-boot:run
```

El perfil por defecto es dev; no intenta conectar PostgreSQL. En otra terminal:

```powershell
cd frontend
Copy-Item .env.example .env
npm.cmd install
npm.cmd run build
npm.cmd run dev
```

Abrir http://localhost:5173. La pantalla consulta http://localhost:8080/api/health y muestra disponible solo si recibe HTTP 200 y el JSON esperado. Tiene tiempo límite y reintento manual. Si se detiene la API, el reintento debe mostrar no disponible.

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Respuesta: {"application":"Comunitarias","status":"UP"}. Es salud HTTP de la API, no prueba de conexión a Supabase.

## Variables de entorno

| Variable | Uso |
|---|---|
| SPRING_PROFILES_ACTIVE | dev (por defecto) o supabase, nunca ambos |
| SERVER_PORT | Puerto backend, 8080 por defecto |
| CORS_ALLOWED_ORIGINS | Lista separada por comas; localhost:5173 y 127.0.0.1:5173 por defecto |
| SUPABASE_JDBC_URL | JDBC copiada del panel Connect, con SSL |
| SUPABASE_DB_USER | Usuario del panel Connect |
| SUPABASE_DB_PASSWORD | Contraseña real, solo local |
| VITE_API_BASE_URL | URL pública backend; http://localhost:8080 por defecto |

La .env.example raíz documenta valores ficticios. **Spring Boot no carga .env automáticamente:** definir variables en la terminal o en la configuración de ejecución de IntelliJ. Vite carga frontend/.env. Nunca poner contraseñas o claves en VITE_* porque se publican al navegador.

## Supabase

En Connect elegir Session Pooler (puerto 5432) y copiar host, usuario y contraseña propios; agregar sslmode=require a la URL JDBC. No usar los placeholders como credenciales. En PowerShell, antes de ejecutar el backend:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'supabase'
$env:SUPABASE_JDBC_URL = Read-Host 'URL JDBC del panel Connect (con sslmode=require)'
$env:SUPABASE_DB_USER = Read-Host 'Usuario de base de datos'
$clave = Read-Host 'Contraseña de base de datos' -AsSecureString
$env:SUPABASE_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $clave).Password
cd backend
mvn spring-boot:run
```

La configuración no crea esquema ni datos. Para validar la conexión real revisar el arranque JDBC/JPA y ejecutar SELECT 1 en una consola de base de datos. Un 200 de health aislado no certifica conectividad. Al terminar retirar las variables secretas de la terminal:

```powershell
Remove-Item Env:SUPABASE_DB_PASSWORD
Remove-Item Env:SUPABASE_DB_USER
Remove-Item Env:SUPABASE_JDBC_URL
Remove-Item Env:SPRING_PROFILES_ACTIVE
```

**Estado actual:** integración preparada; conexión real pendiente por ausencia de credenciales.

## Seguridad y control de versiones

Solo GET /api/health es público. Otros endpoints requieren autenticación, aún no implementada. BCrypt preparado y CSRF activo. Roles previstos: CLIENTE, OPERACIONES, GERENCIA. No se deben probar login ni sesiones en Sprint 0.

.gitignore excluye entornos locales, secretos, node_modules, target, dist, cachés, logs e IntelliJ; conserva los .env.example. La carpeta recibida no es un repositorio Git inicializado. No se realizó push, commit ni creación de ramas remotas. Al inicializar Git en una etapa autorizada, revisar git status y git ls-files antes de agregar archivos.

Documentación: [Arquitectura](docs/ARQUITECTURA.md), [Decisiones](docs/DECISIONES_ARQUITECTURA.md), [Estado y verificaciones](docs/SPRINT_0.md). La planificación original se conserva como referencia; las decisiones aprobadas prevalecen y excluyen rentabilidad, pendiente de validación docente.
