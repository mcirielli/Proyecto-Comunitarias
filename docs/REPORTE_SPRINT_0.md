# Reporte del Sprint 0 — Comunitarias

Fecha del registro: 9 de octubre de 2026. Alcance: infraestructura, configuración y estructura inicial, tareas S0-01 a S0-07 autorizadas por el usuario.

Este reporte reconstruye la ejecución de esta sesión y contrasta sus resultados con los archivos actuales. No constituye una nueva ejecución de compilaciones o pruebas. Distingue implementación escrita, comprobaciones efectivamente realizadas y validaciones pendientes.

## 1. Resumen ejecutivo

Se crearon 33 archivos de proyecto durante Sprint 0: configuración raíz, backend Maven/Spring Boot, frontend React/Vite/TypeScript/Tailwind, documentación de base de datos y documentación técnica. Se conservó la planificación previa y no se modificaron los documentos académicos.

El backend tiene una clase principal, un endpoint técnico público, DTO, configuración de seguridad/CORS y perfiles de desarrollo y Supabase. El frontend tiene una pantalla inicial y una llamada Fetch real al endpoint técnico, con estados de disponibilidad, timeout y reintento. El acceso JDBC está preparado mediante variables de entorno, sin esquema de negocio.

**Estado real: infraestructura escrita, pero validación ejecutable incompleta. Sprint 0 no se declara cerrado.** Maven y npm no pudieron descargar dependencias por restricciones de acceso a la red. El backend no llegó a compilar ni a ejecutar pruebas; el frontend no pudo compilar ni arrancar. Los puertos locales consultados no tenían servidores activos. No se comprobó el consumo React→API en navegador ni la conexión con Supabase.

La configuración de seguridad está escrita, pero no verificada en ejecución. No hay login funcional, usuarios persistidos, permisos por rol implementados ni módulos de negocio. No se inició Sprint 1.

## 2. Estructura del repositorio

Árbol de archivos de proyecto generados y referencias preexistentes relevantes. Las rutas Java se muestran completas dentro de su paquete para facilitar la continuación del trabajo.

~~~text
Comunitarias/
├── AGENTS.md                                  [preexistente]
├── .env.example
├── .gitignore
├── README.md
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/ar/edu/uade/comunitarias/
│       │   │   ├── ComunitariasApplication.java
│       │   │   ├── config/SecurityConfig.java
│       │   │   ├── controller/HealthController.java
│       │   │   ├── dto/HealthResponse.java
│       │   │   ├── domain/package-info.java
│       │   │   ├── mapper/package-info.java
│       │   │   ├── repository/package-info.java
│       │   │   └── service/package-info.java
│       │   └── resources/
│       │       ├── application.properties
│       │       ├── application-dev.properties
│       │       └── application-supabase.properties
│       └── test/java/ar/edu/uade/comunitarias/
│           └── InfrastructureTest.java
├── frontend/
│   ├── .env.example
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   ├── tsconfig.app.json
│   ├── tsconfig.node.json
│   ├── vite.config.ts
│   └── src/
│       ├── main.tsx
│       ├── index.css
│       ├── vite-env.d.ts
│       ├── components/ApiStatus.tsx
│       ├── pages/HomePage.tsx
│       └── services/health.ts
├── database/
│   └── README.md
├── docs/
│   ├── ARQUITECTURA.md
│   ├── DECISIONES_ARQUITECTURA.md
│   ├── SPRINT_0.md
│   ├── PLANIFICACION_MVP.md                    [preexistente]
│   └── REPORTE_SPRINT_0.md                    [este reporte]
└── context/                                  [preexistente, conservado]
    ├── .gitignore
    ├── README.md
    ├── SUPERPROMPT.md
    ├── entregas/                             [2 PDF]
    └── material-uade/                        [6 PDF y 12 PPTX]
~~~

También existe .idea/ con configuración local preexistente. Las verificaciones generaron .cache/ con caché/logs npm, archivos de resolución Maven y un repositorio Git temporal para comprobar exclusiones. .cache/ y .idea/ están excluidos por el .gitignore raíz. El repositorio temporal no convierte la raíz del proyecto en un repositorio Git.

No hay package-lock.json, esquema SQL de negocio ni Maven Wrapper. Los paquetes domain, service, repository y mapper contienen únicamente package-info.java; no implementan funcionalidades. No se crearon clases Strategy o State.

## 3. Backend

### Proyecto y herramientas

- Coordenadas Maven: ar.edu.uade:comunitarias:0.0.1-SNAPSHOT.
- Parent: org.springframework.boot:spring-boot-starter-parent:3.5.16.
- Propiedad java.version: 21; objetivo de compilación Java 21 gestionado por el parent.
- Plugin de construcción: spring-boot-maven-plugin.
- Entrada: ar.edu.uade.comunitarias.ComunitariasApplication, con @SpringBootApplication.
- Nombre de aplicación: comunitarias; puerto 8080 por defecto, configurable con SERVER_PORT.

Java 21 está configurado en el proyecto, pero no fue localizado en el equipo durante la ejecución. Se encontraron Corretto 23.0.2 y OpenJDK 26.0.2. El intento Maven utilizó Corretto 23, manteniendo el objetivo del proyecto en 21; falló antes de la compilación. No existe evidencia de ejecución correcta bajo Java 21.

Maven no estaba en PATH. Se localizó el Maven incorporado en IntelliJ IDEA 2026.2.0.1 y se invocó su launcher Java. No se registró una versión Maven exacta. El README pide Maven 3.6.3 o superior y describe su selección en IntelliJ.

### Dependencias declaradas

| Dependencia | Scope | Propósito |
|---|---|---|
| spring-boot-starter-web | compile por defecto | HTTP REST, servidor embebido y JSON |
| spring-boot-starter-data-jpa | compile por defecto | Infraestructura de persistencia futura |
| spring-boot-starter-security | compile por defecto | Filtros de seguridad y BCrypt |
| spring-boot-starter-validation | compile por defecto | Validación futura de solicitudes |
| org.postgresql:postgresql | runtime | Driver JDBC |
| spring-boot-starter-test | test | JUnit 5 e infraestructura de pruebas |

Las versiones transitivas se delegan al parent. Son dependencias declaradas, no una instalación resuelta exitosamente. No se agregaron Lombok, H2, JWT, OAuth ni bibliotecas de negocio.

### Perfiles y propiedades

| Archivo | Comportamiento configurado |
|---|---|
| application.properties | Nombre, puerto, CORS, perfil dev por defecto, desactivación del usuario automático y controles de JPA/errores |
| application-dev.properties | Excluye DataSourceAutoConfiguration, HibernateJpaAutoConfiguration y JpaRepositoriesAutoConfiguration, además del usuario automático |
| application-supabase.properties | URL, usuario y contraseña obligatorios por entorno; driver PostgreSQL y pool Hikari |

El perfil dev está diseñado para permitir probar HTTP sin PostgreSQL. El perfil supabase prepara el acceso real. No se deben activar ambos juntos, pues las exclusiones de dev impiden inicializar la base de datos.

Se configura open-in-view=false, ddl-auto=none y spring.sql.init.mode=never. El código no incluye entidades JPA ni inicialización de esquema. Los mensajes y stack traces de errores no se exponen mediante las propiedades de respuesta configuradas.

### Endpoint técnico

| Método y ruta | Acceso configurado | Respuesta definida en código |
|---|---|---|
| GET /api/health | Público | JSON con application=Comunitarias y status=UP |

HealthController devuelve HealthResponse. Es un indicador de disponibilidad HTTP; no consulta PostgreSQL ni comprueba credenciales. La respuesta es la definida en código, **no una respuesta exitosa observada durante la ejecución**. No hay otros endpoints implementados, incluidos los de sesión.

## 4. Frontend

El proyecto comunitarias-frontend es privado, versión 0.0.1, con módulos ES y requisito Node >=22.12.0. Se encontraron Node 24.21.0 y npm 11.19.0.

| Paquete | Rango declarado |
|---|---|
| react, react-dom | ^19.3.0 |
| vite | ^7.1.9 |
| typescript | ~5.9.3 |
| tailwindcss, @tailwindcss/vite | ^4.1.14 |
| @vitejs/plugin-react | ^5.0.4 |
| @types/react, @types/react-dom | ^19.2.0 |
| @types/node | ^24.0.0 |

No se resolvieron las versiones finales ni se generó lockfile. Vite registra los plugins React y Tailwind; Tailwind se importa desde index.css. El servidor está configurado con host localhost, puerto 5173 y strictPort=true.

TypeScript usa target ES2022, resolución Bundler, modo estricto, módulos ESNext y noEmit. tsconfig.json referencia las configuraciones de aplicación y Vite. La aplicación usa JSX react-jsx y librerías DOM; la configuración Vite utiliza tipos Node.

Scripts declarados: dev ejecuta vite; build ejecuta tsc -b y luego vite build; preview ejecuta vite preview. Declararlos no significa que hayan funcionado.

### Consulta real de disponibilidad

main.tsx monta HomePage mediante createRoot y StrictMode. HomePage muestra el título Comunitarias y el estado técnico mediante ApiStatus. health.ts usa Fetch nativo hacia VITE_API_BASE_URL + /api/health, sin datos simulados y con cache=no-store. El valor por defecto es http://localhost:8080 y se elimina una barra final de la URL base.

Solo se presenta disponibilidad tras una respuesta HTTP exitosa y JSON con los valores esperados. La página cancela la solicitud después de ocho segundos, limpia solicitudes al desmontar y permite reintento manual. Los estados son checking, available y unavailable; se muestran errores y un anuncio accesible con role=status/aria-live.

Vite carga frontend/.env, cuyo ejemplo solo contiene VITE_API_BASE_URL. No se creó una .env real. Los valores VITE_* son públicos en el navegador; no deben contener credenciales.

Esta conducta fue revisada en código; **no se verificó en navegador ni con una API activa**. Tampoco se ejecutaron pruebas automatizadas de frontend.

## 5. PostgreSQL / Supabase

La integración está preparada en application-supabase.properties con las variables SUPABASE_JDBC_URL, SUPABASE_DB_USER y SUPABASE_DB_PASSWORD. No hay valores reales por defecto.

Configuración del pool: máximo tres conexiones; connection-timeout e initialization-fail-timeout de 10.000 ms. La .env.example raíz usa host, usuario y contraseña ficticios claramente marcados. El README indica obtener los datos del panel Connect, usar Session Pooler en puerto 5432 y agregar sslmode=require a la URL JDBC. No hay una prueba realizada de SSL ni del pool.

Spring Boot no carga automáticamente el archivo .env: las variables deben existir en el proceso o en la configuración de ejecución de IntelliJ. El README incluye instrucciones PowerShell para introducirlas sin escribir una contraseña real en archivos versionables.

**Estado real: conexión pendiente.** Se comprobó la ausencia de las tres variables en el entorno durante Sprint 0. No se intentó iniciar el perfil supabase con credenciales reales ni se ejecutó SELECT 1. No se creó esquema, tablas, seeds ni entidades de negocio. El archivo database/README.md documenta esta limitación.

Un futuro 200 de /api/health en perfil dev tampoco acreditaría conexión con Supabase. La validación JDBC deberá realizarse expresamente.

## 6. Seguridad y decisiones implementadas

SecurityConfig define SecurityFilterChain, PasswordEncoder BCrypt y CorsConfigurationSource.

- Solo GET /api/health tiene permitAll; el resto usa authenticated.
- formLogin, HTTP Basic y logout están desactivados.
- Se configura HttpStatusEntryPoint con 401 para solicitudes que requieren autenticación. No fue verificado en ejecución; CORS/CSRF pueden rechazar solicitudes antes con otro estado.
- CSRF permanece activo. No se implementó entrega de tokens ni flujo de sesiones para React.
- Se excluye UserDetailsServiceAutoConfiguration para evitar el usuario/contraseña aleatorio de Spring Boot.
- No hay usuarios, proveedor funcional de autenticación, formulario de login, endpoints de sesión, JWT u OAuth.
- BCrypt está preparado como bean; no se han cifrado contraseñas de usuarios reales.

CORS se aplica a /api/**. Por defecto acepta http://localhost:5173 y http://127.0.0.1:5173; se configura mediante CORS_ALLOWED_ORIGINS. Los métodos permitidos son GET, POST, PUT, PATCH, DELETE y OPTIONS. Los headers permitidos son Content-Type, X-CSRF-TOKEN y X-XSRF-TOKEN; allowCredentials=true y maxAge=3600 segundos. No se utiliza un origen comodín.

Los roles Cliente, Operaciones y Gerencia, con futuras autoridades ROLE_CLIENTE, ROLE_OPERACIONES y ROLE_GERENCIA, se documentaron. No existen reglas hasRole ni usuarios precargados todavía: su implementación funcional quedó fuera de Sprint 0.

DECISIONES_ARQUITECTURA.md también registra las decisiones aprobadas para etapas posteriores: reservas automáticas, movimientos de stock, historial de proveedores, autorización manual Quinteros, facturación interna y pagos. Ninguna de esas funciones se implementó en este sprint.

Los reportes futuros se limitaron a ventas y compras, sin rentabilidad. **La exclusión de rentabilidad es un recorte académico pendiente de validación docente**, no una conformidad obtenida. La planificación anterior se conserva como referencia histórica y las decisiones aprobadas prevalecen sobre sus propuestas anteriores.

## 7. Pruebas y verificaciones realizadas

### 7.1 Herramientas y compilaciones

La consola estándar exec_command falló con helper_unknown_error/setup refresh had errors. Se utilizó Node REPL y child_process para leer/escribir archivos y ejecutar herramientas locales. Este fallo de herramienta no equivale a un fallo de compilación del proyecto.

Se consultaron herramientas en PATH y ubicaciones locales; se ejecutaron comprobaciones de versión Node/npm y java -version para Corretto. Git devolvió que la raíz no era un repositorio. No se inicializó Git en la raíz ni se realizó commit/push.

El primer intento del launcher Maven falló con código 100:

~~~text
java.lang.Exception: classworlds configuration not specified nor found in the classpath
~~~

Se corrigió la invocación agregando classworlds.conf; no fue necesario modificar el POM por este error. La ejecución equivalente a Maven verify se realizó desde backend con el siguiente launcher y argumentos (rutas y opciones usadas en la sesión):

~~~text
C:/Users/Noxie-PC/.jdks/corretto-23.0.2/bin/java.exe
-Dclassworlds.conf=C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.0.1/plugins/maven-plugin/lib/maven3/bin/m2.conf
-Dmaven.home=C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.0.1/plugins/maven-plugin/lib/maven3
-Dmaven.multiModuleProjectDirectory=C:/Users/Noxie-PC/OneDrive/Documentos/Comunitarias/backend
-Dmaven.repo.local=C:/Users/Noxie-PC/OneDrive/Documentos/Comunitarias/.cache/maven
-Daether.connector.connectTimeout=5000
-Daether.connector.requestTimeout=5000
-classpath C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.0.1/plugins/maven-plugin/lib/maven3/boot/plexus-classworlds-2.11.0.jar
org.codehaus.plexus.classworlds.launcher.Launcher -B verify
~~~

La lista anterior representa argumentos separados suministrados al proceso, no un comando listo para pegar sin comillas. El resultado final fue código 1 al intentar descargar el parent desde Maven Central:

~~~text
Non-resolvable parent POM
Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:3.5.16
Permission denied: getsockopt
'parent.relativePath' points at no local POM
~~~

El fallo se produjo al resolver el parent. No llegaron a ejecutarse compilación, tests, empaquetado o arranque del backend. No existe un resultado de BUILD SUCCESS ni un informe de pruebas aprobadas.

npm se invocó mediante node y C:/Program Files/nodejs/node_modules/npm/bin/npm-cli.js desde frontend. Se ejecutaron los argumentos:

~~~text
install --cache C:/Users/Noxie-PC/OneDrive/Documentos/Comunitarias/.cache/npm --fetch-retries=0 --fetch-timeout=10000 --no-audit --no-fund
run build
run dev -- --host 127.0.0.1
~~~

| Ejecución | Resultado real |
|---|---|
| npm install | Código 1: FetchError/EACCES en registry.npmjs.org/@tailwindcss%2fvite |
| npm run build | Código 1: tsc no se reconoce como comando; no se llegó a vite build |
| npm run dev | Código 1: vite no se reconoce como comando |

No se completó la instalación. No se ejecutó auditoría npm ni se generó package-lock.json. Una consulta exploratoria a npm mediante shell falló por el espacio en C:/Program Files; se sustituyó por la invocación directa node/npm-cli.js. Ese error de ruta quedó corregido en la invocación, pero la descarga siguió bloqueada.

El log npm quedó en .cache/npm/_logs/2026-10-09T03_01_24_793Z-debug-0.log. Las cachés son artefactos locales ignorados.

### 7.2 Pruebas backend preparadas, no ejecutadas

InfrastructureTest contiene seis tests JUnit 5 con @SpringBootTest, servidor en puerto aleatorio, @ActiveProfiles("dev") y HttpClient Java:

| Test | Comprobación prevista |
|---|---|
| healthIsPublicAndReturnsApiStatus | HTTP 200, JSON y nombre/estado de aplicación |
| futureEndpointsRequireAuthentication | Rechazo 401 para una ruta futura sin autenticar |
| localFrontendCanReadHealthWithCors | Origen local y header Access-Control-Allow-Origin |
| localPreflightIsAccepted | Preflight OPTIONS local aceptado |
| otherOriginsAreRejected | Rechazo CORS 403 para otro origen |
| passwordEncoderUsesBcrypt | Hash BCrypt y verificación de contraseña correcta/incorrecta |

Son comprobaciones de infraestructura independientes de Supabase. No son tests de reglas de negocio. **Cero tests confirmados como ejecutados/aprobados**, porque Maven no alcanzó esa fase.

Durante la revisión se corrigieron escapes de los literales JSON de InfrastructureTest antes de finalizar Sprint 0. La versión corregida tampoco pudo compilarse por el bloqueo de resolución Maven.

### 7.3 HTTP e integración

Se intentaron Fetch reales a http://127.0.0.1:8080/api/health y http://127.0.0.1:5173 con límite de tres segundos. Ambos fallaron con ECONNREFUSED: no había servidor activo.

No se obtuvo un 200 real de health; no se verificaron en ejecución el 401, los headers CORS, BCrypt, CSRF o el consumo React→API. No se abrió una aplicación frontend funcionando en navegador. No se ejecutaron los comandos de arranque backend o SELECT 1 que aparecen como instrucciones futuras en README.

### 7.4 Formato, exclusiones y fuentes

- Se parseó backend/pom.xml mediante ElementTree de Python y los JSON de frontend mediante json.loads: ambos chequeos finalizaron con código 0. Esto valida formato, no semántica Maven/TypeScript ni compilación.
- Se inicializó únicamente un repositorio temporal en .cache/ignore-check para ejecutar git check-ignore --no-index con core.excludesFile apuntando al .gitignore raíz.
- Se comprobaron como ignoradas .env, frontend/.env, frontend/node_modules/example, backend/target/example, .idea/workspace.xml, .cache/example, frontend/dist/example y private.key. Son nombres de prueba, no secretos reales creados.
- .env.example y frontend/.env.example resultaron no ignorados, como corresponde.
- La revisión de los archivos creados no detectó secretos reales. Las tres variables Supabase estaban ausentes y no se creó una .env real.
- La raíz no tenía Git inicializado. Por lo tanto, no se certificó ausencia de secretos en archivos versionados ni en historial: ese historial no pudo auditarse como repositorio del proyecto.
- Se compararon SHA-256 previos/posteriores de ambas entregas PDF, context/SUPERPROMPT.md, context/README.md y docs/PLANIFICACION_MVP.md: coincidieron. Ningún PDF/PPTX fue editado; la comprobación de hashes específica no incluyó todo el material UADE.

Las restricciones de red son el bloqueo observado. No se afirma que el código restante esté libre de errores: los fallos de compilación o ejecución que puedan aparecer después de resolver dependencias aún no se han evaluado.

## 8. Archivos creados y modificados

En Sprint 0 se crearon 33 archivos de proyecto, listados a continuación. No se modificaron archivos preexistentes del proyecto. La corrección de literales de la prueba se realizó sobre un archivo recién creado. Las cachés/logs y el repositorio temporal se contabilizan separadamente como artefactos de verificación, no dentro de los 33.

| Archivo creado | Propósito |
|---|---|
| .gitignore | Exclusiones de secretos, dependencias, generados e IDE. |
| .env.example | Variables de ejemplo públicas para backend y URL frontend. |
| README.md | Requisitos, comandos, configuración local y limitaciones. |
| backend/pom.xml | Proyecto Maven, Java 21, parent Spring Boot y dependencias. |
| backend/src/main/java/ar/edu/uade/comunitarias/ComunitariasApplication.java | Entrada Spring Boot. |
| backend/src/main/java/ar/edu/uade/comunitarias/config/SecurityConfig.java | Filtros, CORS y BCrypt. |
| backend/src/main/java/ar/edu/uade/comunitarias/controller/HealthController.java | GET técnico /api/health. |
| backend/src/main/java/ar/edu/uade/comunitarias/dto/HealthResponse.java | Contrato de respuesta health. |
| backend/src/main/java/ar/edu/uade/comunitarias/domain/package-info.java | Reserva del paquete dominio, sin entidades. |
| backend/src/main/java/ar/edu/uade/comunitarias/mapper/package-info.java | Reserva del paquete de mapeo. |
| backend/src/main/java/ar/edu/uade/comunitarias/repository/package-info.java | Reserva del paquete, sin repositorios funcionales. |
| backend/src/main/java/ar/edu/uade/comunitarias/service/package-info.java | Reserva del paquete, sin servicios funcionales. |
| backend/src/main/resources/application.properties | Propiedades compartidas y perfil por defecto. |
| backend/src/main/resources/application-dev.properties | HTTP sin base de datos. |
| backend/src/main/resources/application-supabase.properties | JDBC y pool con variables obligatorias. |
| backend/src/test/java/ar/edu/uade/comunitarias/InfrastructureTest.java | Seis pruebas de infraestructura no ejecutadas. |
| frontend/package.json | Dependencias, scripts y requisito Node. |
| frontend/index.html | Documento raíz de la aplicación. |
| frontend/tsconfig.json | Referencias de compilación. |
| frontend/tsconfig.app.json | Configuración TypeScript de la aplicación. |
| frontend/tsconfig.node.json | Configuración TypeScript de Vite. |
| frontend/vite.config.ts | Plugins y servidor local. |
| frontend/.env.example | URL pública de API. |
| frontend/src/vite-env.d.ts | Tipos de entorno Vite. |
| frontend/src/index.css | Importación Tailwind y estilos básicos. |
| frontend/src/main.tsx | Montaje React. |
| frontend/src/components/ApiStatus.tsx | Presentación del estado API. |
| frontend/src/pages/HomePage.tsx | Inicio, consulta y reintento. |
| frontend/src/services/health.ts | Fetch real y validación de respuesta. |
| database/README.md | Estado de preparación de base de datos. |
| docs/ARQUITECTURA.md | Capas, responsabilidades y estado actual. |
| docs/DECISIONES_ARQUITECTURA.md | Decisiones aprobadas y recorte académico. |
| docs/SPRINT_0.md | Registro breve de tareas, errores y pendientes. |

Se conservaron AGENTS.md, context/ y docs/PLANIFICACION_MVP.md. No se modificó la planificación original para reemplazar sus propuestas: DECISIONES_ARQUITECTURA.md registra qué decisiones prevalecen.

Este REPORTE_SPRINT_0.md se crea después de la implementación por solicitud expresa del usuario y no forma parte del conteo original de 33. Para esta tarea de reporte se autoriza y se escribe únicamente este archivo; no se reintentan instalaciones ni se modifican configuraciones.

## 9. Pendientes técnicos

### Bloqueantes de validación del Sprint 0

1. Localizar/instalar JDK 21 y configurar JAVA_HOME y la herramienta Maven. La instalación global no se realizó en la sesión.
2. Permitir descargas desde Maven Central y npm en el entorno de desarrollo; resolver dependencias sin sustituirlas por simulaciones.
3. Ejecutar Maven verify con JDK 21 y corregir los errores reales que surjan. Actualmente no existe compilación backend validada.
4. Completar npm install, conservar package-lock.json y ejecutar npm run build. Los rangos de versiones no garantizan por sí solos una instalación reproducible.
5. Arrancar backend y frontend y verificar health, errores de disponibilidad, CORS y seguridad con aplicación real.
6. Proporcionar credenciales locales propias, iniciar solo supabase y verificar JDBC mediante una consulta técnica real; no crear esquema definitivo todavía.
7. Revisar el estado de Git del proyecto y su preparación antes de una futura inicialización/publicación autorizada. No hay commit ni remoto configurado en esta sesión.

### Pendientes funcionales fuera de este sprint

Usuarios precargados, autenticación real y permisos por rol; entidades y relaciones JPA; repositorios y servicios de negocio; oferta/productos/proveedores y precios históricos; pedidos; Strategy y State; reservas y movimientos; compras por demanda y Quinteros con autorización; recepción; facturación/pagos; reportes de ventas/compras y pantallas operativas.

Son funcionalidades acordadas para etapas posteriores o pendientes de diseño detallado, no faltantes a implementar durante el Sprint 0 autorizado. No se definió el esquema definitivo ni algoritmos adicionales. No debe interpretarse la presencia de una dependencia JPA o un paquete vacío como funcionalidad terminada.

La exclusión de rentabilidad y el alcance reducido de reportes requieren validación docente. El registro de decisiones no aporta esa validación.

## 10. Estado del Sprint 0

Criterio: completada significa entregable escrito y comprobado dentro del alcance disponible; parcialmente completada significa configuración escrita con verificación ejecutable pendiente. Las pruebas de runtime no se sustituyen por revisión estática.

| Tarea | Estado | Entregado | Pendiente |
|---|---|---|---|
| S0-01 Estructura | Completada | Directorios requeridos, archivos raíz y fuentes conservadas | Ninguno estructural identificado |
| S0-02 Backend | Parcialmente completada | POM, entrada, paquetes, perfiles, CORS y health | Compilar con Java 21, ejecutar tests y comprobar arranque/health |
| S0-03 Frontend | Parcialmente completada | React/Vite/TypeScript/Tailwind y Fetch real en código | Instalar, generar lockfile, compilar y verificar en navegador |
| S0-04 PostgreSQL | Parcialmente completada | JDBC, ejemplos y documentación sin secretos | Credenciales y prueba real; conexión no confirmada |
| S0-05 Seguridad inicial | Parcialmente completada en validación | Configuración mínima y roles documentados | Tests efectivos de protección/CORS/BCrypt; login funcional fuera de alcance |
| S0-06 Control de versiones | Completada para preparación solicitada | .gitignore y comprobaciones en repositorio temporal | Raíz sin Git; auditoría versionada/publicación no realizada |
| S0-07 Documentación | Completada | Arquitectura, decisiones, estado y README | Actualizar resultados solo después de futuras verificaciones reales |

No se consideran pendientes de S0 las entidades, el esquema definitivo, login completo ni pantallas comerciales, pues estaban prohibidos por el alcance. Sí son pendientes de cierre las compilaciones, pruebas e integración técnica no verificadas.

**Conclusión del estado:** no hay aceptación técnica completa del Sprint 0. El material permite retomar las verificaciones, pero no acredita un sistema ejecutándose ni una base de datos conectada.

## 11. Próximos pasos recomendados para iniciar Sprint 1

Las siguientes son recomendaciones, no tareas ejecutadas ni autorización de avance.

### Primero cerrar la base técnica

| Responsable sugerido | Trabajo inmediato | Evidencia de cierre esperada |
|---|---|---|
| Integrante 1 | Java 21/Maven, resolución backend y seis pruebas | verify exitoso con resultados de tests y arranque real |
| Integrante 2 | Dependencias frontend, lockfile, build e integración | build exitoso y pantalla leyendo health real; error al detener API |
| Integrante 3 | Credenciales/JDBC Supabase | Conexión verificada y consulta técnica real, sin esquema de negocio |
| Integrante 4 | Documentación, controles Git y consulta docente | Registro de verificaciones y respuesta sobre el recorte académico |

Comandos locales ya documentados, **a ejecutar posteriormente** (no aprobados como exitosos en este reporte):

~~~powershell
# Terminal backend; Java 21 y Maven configurados
cd backend
mvn verify
mvn spring-boot:run
~~~

~~~powershell
# Otra terminal, desde la raíz
cd frontend
Copy-Item .env.example .env
npm.cmd install
npm.cmd run build
npm.cmd run dev
~~~

Si ya existe frontend/.env, conservar sus valores locales en vez de sobrescribirlos. Consultar el README raíz para las variables y el perfil supabase. No activar dev junto a supabase. Nunca versionar valores reales de contraseña ni publicarlos como VITE_*.

### Después presentar el alcance del Sprint 1

Una vez cerrados o explícitamente aceptados los pendientes, acordar contratos y modelo inicial y presentar una etapa acotada para aprobación. Mantener las decisiones de login, historial, stock y autorización; los reportes deberán respetar el recorte aprobado y su validación docente pendiente. Seleccionar un incremento verificable, sin generar simultáneamente todos los módulos.

No dar por aprobadas automáticamente reglas de negocio que la planificación marcó como propuestas. Solicitar aprobación para decisiones arquitectónicas nuevas. Conservar trazabilidad entre entrega académica, decisión aprobada, implementación y prueba real.

## Fuentes del reporte

- Autorización y resultados observados del Sprint 0 en esta sesión.
- [Estado breve del Sprint 0](SPRINT_0.md).
- [Arquitectura](ARQUITECTURA.md) y [decisiones aprobadas](DECISIONES_ARQUITECTURA.md).
- [README de ejecución](../README.md), [POM](../backend/pom.xml), [package.json](../frontend/package.json), configuraciones y fuentes enumeradas en el inventario.
- [Planificación previa conservada](PLANIFICACION_MVP.md), como referencia histórica y no como aprobación de todas sus propuestas.

No se utilizaron nuevos resultados de pruebas ni fuentes externas para redactar este reporte.
