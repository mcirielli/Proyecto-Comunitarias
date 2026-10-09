# Sprint 1 — documento operativo

**Período: 10 al 16 de octubre de 2026.** Registro inicial: 9 de octubre de 2026. Alcance formalizado por instrucción del usuario. Estado actual: **Sprint en progreso. S1-01 En revisión final: pruebas funcionales y revisión cruzada aprobadas según el usuario; pendiente comprobar secretos en las capas de las imágenes entregadas (sección 12). El resto de tareas continúa Por hacer.** La formalización inicial fue solo documental; la autorización posterior habilitó únicamente S1-01.

La [planificación existente](PLANIFICACION_SPRINT_1.md) es la fuente única del alcance; este documento registra trabajo, aceptación, Definition of Done (DoD), riesgos y cierre. Las [decisiones](DECISIONES_ARQUITECTURA.md) distinguen aprobaciones de pendientes. No se autoriza publicación pública, commit/push automático ni avance al Sprint 2.

## 1. Resultado del sprint

Backend/frontend ejecutables con Docker Compose, PostgreSQL administrado en Supabase y conexión JDBC demostrada desde Spring; login con sesiones/BCrypt/CSRF/roles; gestión de productos/proveedores/precios históricos; consulta real de oferta en React; Render preparado y documentado, sin publicar.

Ocho P0 obligatorias: S1-01, S1-02, S1-03, S1-04, S1-05, S1-06, S1-09 y S1-10. Fuera de Sprint 1: S1-07 crear/consultar pedidos y S1-08 confirmación/State, trasladadas al Sprint 2. También quedan para posteriores etapas compras/Strategy, inventario, facturación, pagos y reportes. No crear sus entidades/tablas/servicios/pantallas como preparación prematura.

## 2. Tablero y revisión cruzada

| Tarea | P | Autor principal / apoyo | Revisor | Estado actual | Dependencia que condiciona la entrega |
|---|---|---|---|---|---|
| S1-01 Docker/Compose | P0 | 1 / 4 | 3 y 4: aprobada sin observaciones, según usuario | En revisión final | Funcionalidad y controles iniciales aprobados; pendiente verificar contenido de capas de las imágenes entregadas (sección 12) |
| S1-02 JDBC | P0 | 2 / 1 | 1 | Por hacer | Credenciales propias; S1-01 para prueba contenedor |
| S1-03 Modelo y SQL | P0 | 2 / 3 y 4 | 3 | Por hacer: comenzar por diseño | Aplicación SQL/entidades espera modelo e historial aprobados y S1-02 |
| S1-04 Login/roles | P0 | 3 / 4 | 2 y 4 | Por hacer: comenzar por contrato | Persistencia requiere S1-03; contrato local CSRF/sesión/permisos; producción por resolver |
| S1-05 Maestros/precios | P0 | 2 / 4 | 3 | Por hacer | S1-03, historial aprobado; acceso integrado S1-04 |
| S1-06 React/oferta | P0 | 4 / 2 y 3 | 2 y 3 | Por hacer: comenzar por diseño | Integración requiere modelo/datos/contratos, S1-04/S1-05 |
| S1-09 Preparación Render | P0 | 1 / 3 y 4 | 4 | Por hacer | Artefactos S1-01/02; detalles producción pendientes; publicar prohibido |
| S1-10 Pruebas/cierre | P0 | Los cuatro | Revisión cruzada | Por hacer | Pruebas desde cada tarea; cierre tras evidencia de los ocho P0 |

Los estados de las demás tareas conservan la formalización; S1-01 se actualiza con la ejecución descrita abajo. La validación Docker en Windows fue reportada por el usuario, no ejecutada por el agente; no se verificó JDBC en esta etapa. Cambiar a En desarrollo cuando comience la tarea; En revisión al aportar evidencia; Terminado solo con criterios y DoD cumplidos. Bloqueado debe incluir causa concreta, responsable de resolver y siguiente acción. Dependencias planificadas no se presentan como fallos ya observados.

## 3. Tareas, archivos futuros y criterios de aceptación

Los archivos mencionados para las tareas posteriores son destinos futuros. S1-01 se implementó tras autorización específica, como detalla el registro de ejecución. No se implementó ninguna otra tarea del sprint.

### S1-01 — Dockerizar backend/frontend

Responsable 1; apoyo 4. Archivos previstos: Dockerfiles y .dockerignore en contextos backend/frontend, archivo Compose raíz, ejemplos de entorno y documentación local cuando se autorice implementar. Multi-stage, Java 21, Node/Nginx, proxy /api/ y primer arranque dev aprobados expresamente en la autorización específica de S1-01; selección implementada en el registro de decisiones.

- docker compose config válido, sin guardar salida con secretos en documentación.
- Build desde checkout/fuentes y lockfile, sin copiar target/node_modules/dist del host ni saltar pruebas para ocultar errores.
- Backend usa Java 21; frontend se construye/ejecuta de forma reproducible y ambos servicios levantan con Compose.
- Health responde 200/UP; React consume la API real desde el navegador usando URL accesible, no hostname interno de Compose sin proxy.
- Se comprueban estados API disponible/no disponible y reintento. Puertos, orígenes y comando de parada documentados.
- Solo frontend/backend, sin servicio PostgreSQL local ni componentes innecesarios. Variables JDBC explícitas solo para backend.
- .dockerignore excluye secretos/generados y archivos ajenos; no hay secretos en capas, argumentos de construcción, logs ni VITE_*.

Evidencia: versiones, comandos build/config/up y sus resultados, HTTP y captura/manual del navegador sin datos sensibles.

### S1-02 — JDBC real desde Spring

Responsable 2; apoyo/revisión 1. Archivos previstos: prueba de integración JDBC y documentación de ejecución; configuración existente solo si hay motivo revisado. No crear endpoint público de diagnóstico BD como requisito.

- Obtener Connection desde el DataSource administrado por Spring y ejecutar SELECT 1; verificar valor 1. Abrir conexión desde IntelliJ por sí solo no cumple.
- Ejecutar con solo perfil supabase y SUPABASE_JDBC_URL/DB_USER/DB_PASSWORD reales mediante entorno; conservar Session Pooler/SSL preparado. No activar dev simultáneamente ni asumir que Spring carga .env.
- Repetir comprobación desde el backend en contenedor cuando S1-01 esté disponible. Registrar evidencia por entorno, no extrapolar el éxito local.
- No crear tablas para esta prueba. GET /api/health sigue indicando disponibilidad HTTP, no conexión PostgreSQL.
- Prueba separada de suite unitaria: ausencia de credenciales no puede presentarse como integración aprobada. Documentar comando/selección del test.
- Un intento controlado con configuración inválida identifica el fallo sin imprimir contraseña ni afectar registros. Restaurar variables locales correctas después; no alterar credenciales del proyecto Supabase.

Evidencia: fecha/perfil/entorno, resultado SELECT 1 y comando ejecutado con valores sensibles omitidos.

### S1-03 — Modelo y esquema inicial

Responsable 2; revisión 3 y 4. Archivos futuros: modelo/diagrama aprobado, database/schema.sql y seed.sql u organización SQL acordada; solo entonces entidades pertinentes. No generar ni aplicar SQL durante la presente formalización.

- Presentar modelo con campos, PK/FK, cardinalidades, unicidad, índices necesarios, nulabilidad y precisión monetaria; incluir Usuario/rol, maestros, relación/historial y oferta según aprobación.
- Aprobar representación del historial y modelo antes de escribir/aplicar SQL o crear entidades. Registrar aprobación concreta y versión del modelo en los documentos existentes.
- Definir esquema destino y cuenta/permisos de aplicación en Supabase, datos ficticios y procedimiento de aplicación seguro; si hay datos existentes, revisar respaldo antes de acciones que los afecten.
- SQL versionado sin credenciales reales, sin tablas Pedido/DetallePedido ni módulos de Sprint 2; ejecutar solo en destino acordado.
- Demostrar PK/FK/restricciones y lectura/escritura real desde Spring; aplicación no recrea automáticamente el esquema al arrancar. Mantener ddl-auto=none y sql.init.mode=never.
- Datos iniciales permiten login y oferta del incremento; no versionar contraseñas reales ni seleccionar por defecto cuentas de demostración públicas. Su provisión debe estar documentada.

Evidencia: aprobación, SQL/revisión, destino sin credenciales, comando/procedimiento y pruebas de integridad/persistencia.

### S1-04 — Autenticación, sesiones, CSRF y roles

Responsable 3; apoyo 4; revisión 2. Archivos futuros: dominio/repositorio de usuarios según modelo aprobado, SecurityConfig, contrato/controlador o handlers de sesión, DTOs y pruebas; React integra login/logout. Roles Cliente, Operaciones y Gerencia, con autoridades ROLE_CLIENTE/ROLE_OPERACIONES/ROLE_GERENCIA.

- Cuentas precargadas conforme a modelo/datos revisados; hashes BCrypt en persistencia; no devolver hash ni contraseña en DTOs/logs.
- Login correcto establece sesión real, consulta de sesión devuelve identidad/rol y logout invalida acceso anterior. Credenciales inválidas se rechazan sin filtrar datos sensibles.
- GET health permanece público; rutas funcionales protegidas. GET sin sesión devuelve 401; operación autenticada con rol no autorizado devuelve 403 según matriz revisada. No otorgar automáticamente acceso de Operaciones a Gerencia sin decidirlo.
- CSRF activo en operaciones de escritura y flujo de sesión según contrato; token válido permite acción, token ausente/incorrecto la rechaza con 403. Obtener/renovar token después de login/logout cuando corresponda. Una escritura sin token puede devolver 403 antes de autenticar: no exigir 401 indiscriminadamente.
- Fetch maneja sesión/credenciales y token CSRF; cookie de sesión HttpOnly; configuración local y futura HTTPS documentadas. No guardar una contraseña ni sustituir sesión por token localStorage.
- Casos de tests: login correcto/incorrecto, consulta/expiración o invalidación según contrato, logout, 401, permisos 403, CSRF y BCrypt.
- Sin registro público, recuperación de contraseña, JWT/OAuth ni ampliaciones innecesarias. Detalles de cookies/duración y matriz de permisos se revisan antes de concretarlos.

Producción: documentar alternativas de dominios/proxy y cookies sin seleccionar una como aprobada. La prueba local no depende de tener dominio o de publicar Render.

### S1-05 — Productos, proveedores e historial

Responsable 2; apoyo 4; revisión 3. Archivos futuros: dominio aprobado, repositories, services, controllers, DTOs y mapeo/pruebas; React para la gestión necesaria del alcance.

- Operaciones puede registrar/consultar productos y proveedores y registrar/consultar precios de proveedor. Alcance exacto de edición/baja se acuerda en contratos; no agregar CRUD completo automáticamente.
- ID Producto estable independientemente del proveedor. Relación producto/proveedor y código/condiciones conforme a modelo aprobado.
- Registrar un nuevo precio conserva la historia; consultar actual/histórico con fechas/vigencia según regla aprobada. Probar cambio y recuperación del valor anterior; resolver correcciones/solapamientos antes de implementar.
- Validación de campos y dinero BigDecimal; precisión SQL acordada. Controller delega; Service Layer coordina; persistencia mediante Repository; no exponer entidades JPA.
- Operaciones multirregistro atómicas; entradas inválidas rechazadas sin persistencia parcial; acceso y datos protegidos según contrato.
- Pruebas unitarias de reglas/historial independientes de Supabase y pruebas de persistencia integradas diferenciadas.

### S1-06 — Oferta real desde React

Responsable 4; soporte API/datos 2 y autenticación 3. Archivos futuros: páginas/componentes/servicios HTTP frontend y DTO/controlador/servicio/repositorio de oferta backend pertinentes al modelo.

- Cliente autenticado consulta oferta persistida mediante Fetch y API real. Si se usan datos precargados, su modelo/vigencia y origen del precio de venta fueron aprobados.
- Se muestran producto y precio de venta; el contrato Cliente no expone precio/condiciones de compra del proveedor.
- Comprobar carga, oferta vacía/inexistente según contrato, error backend, pérdida de sesión y reintento; no simular éxito ni sustituir API por datos hardcodeados.
- Pantallas de login y gestión/consulta integradas conforme a permisos; frontend no decide reglas comerciales que deben estar en Java.
- Sin carrito, creación/consulta/confirmación de pedidos ni pantallas de State. API health sigue disponible en pantalla técnica cuando corresponda.

### S1-09 — Preparación Render

Responsable 1; apoyo 3 y 4. Archivos futuros: guía de preparación/despliegue, configuración de puerto/perfil y descripción/configuración declarativa revisada si se considera necesaria; no agregar un Blueprint como dependencia obligatoria sin motivo.

- Definir backend Web Service Docker, contexto/Dockerfile y ejecución Java 21; frontend Static Site con npm ci && npm run build y dist. Compose no se presenta como un despliegue único en Render.
- Preparar escucha 0.0.0.0/PORT compatible con SERVER_PORT local; variables de ejecución Supabase y origen CORS explícito; VITE_API_BASE_URL solo público en build.
- Documentar health HTTP, verificaciones JDBC específicas, perfiles exclusivos y HTTPS/proxy/cookies. Dejar alternativa producción pendiente identificada, sin afirmar login público probado.
- Checklist antes de publicar: permiso expreso, acceso al repo privado, plan/costos aprobados si corresponde, secretos, builds/tests, JDBC, CORS, sesión/CSRF y smoke test desde navegador.
- Checklist de reversión identifica versión anterior de aplicación y efectos del esquema; revertir contenedor no revierte SQL automáticamente.
- No crear servicios públicos ni publicar, no activar auto-deploy, no comprar dominio/plan ni hacer commit/push. Pendiente de producción no se presenta como prueba ejecutada.

### S1-10 — Pruebas, integración y cierre

Los cuatro. Evidencias en este documento y documentación del proyecto al implementarse.

- Ejecutar mvn clean verify con Java 21; npm ci y npm run build; verificar construcción y ejecución Compose. Los comandos son criterios futuros, no ejecutados aquí.
- Conservar las seis pruebas de infraestructura del Sprint 0 y sumar pruebas pertinentes de login/logout, permisos, CSRF, validaciones/historial y oferta. Si un contrato exige adaptar una prueba, justificarlo y conservar cobertura de la garantía anterior.
- Separar tests unitarios sin Supabase de integración JDBC real; no declarar aprobado un test omitido por ausencia de credenciales.
- Verificar circuito login → gestión maestros/precios → consulta oferta y logout, persistencia tras reinicio de aplicación, aislamiento de roles y error/reintento. Sin pedidos.
- Revisión cruzada y evidencia de ausencia de secretos en fuentes, archivos versionados, contextos/capas de imagen, logs y frontend. No afirmar auditoría exhaustiva de historial sin realizarla.
- Cierre: resultado por cada criterio, comandos/resultados, errores y limitaciones, veredicto y pendientes. Ninguna P0 se marca completa con prueba no ejecutada. El cierre debe informar explícitamente cualquier P0 incumplida; no trasladarla silenciosamente.

## 4. Definition of Done

Para cada tarea y para el sprint:

1. Alcance y decisiones dependientes resueltos; aplicación de SQL tiene modelo aprobado. No expandir pedidos, State/Strategy ni negocio fuera de las ocho P0.
2. Cambio revisado por otro integrante; reglas y contratos documentados; separación MVC/Repository/Service Layer demostrable, sin patrones ficticios.
3. Código cuando exista compila con stack actual; tests pertinentes realmente ejecutados y resultados conservados. No ocultar fallos ni saltar pruebas para cerrar.
4. Integración real frontend/backend/Supabase donde corresponda, credenciales externas y seguridad verificadas.
5. Reproducción local/Compose documentada desde fuentes; variables, dependencias y perfiles claros; no depender de artefactos del host.
6. Sin secretos versionados/embebidos; sin cambios académicos, publicación o commits/push no autorizados.
7. Tablero y documentación actualizados con resultados comprobados/reportados/pendientes separados. Los cuatro pueden explicar su trabajo.

Sprint terminado: los ocho P0 satisfacen aceptación y DoD. Tener Render publicado, State, pedidos o cinco patrones completos no es criterio de Sprint 1. La preparación Render sí es obligatoria, incluso si la decisión final de cookies públicas continúa registrada como pendiente.

## 5. Decisiones pendientes y riesgos

| Pendiente/riesgo | Responsable de preparar resolución | Impacto / siguiente acción |
|---|---|---|
| Modelo/historial sin aprobación concreta | 2; revisión 3/4 | Presentar alternativas y modelo; no SQL/entidades antes de aprobación |
| Oferta y precios de venta | 2/4 | Acordar vigencia, selección actual y datos de demostración |
| Sesión, token CSRF y matriz de permisos locales | 3/4 | Revisar contrato; no inventar facultades de Gerencia |
| Cookies/topología de producción | 1/3 | Comparar subdominios, URLs separadas y proxy; preparar sin publicar |
| Nginx/Node/multi-stage e imágenes | 1/4 | Nginx/Node/multi-stage aprobados y tags explícitos elegidos; build sin caché exitoso reportado por el usuario. Digests no registrados; no son un criterio obligatorio de cierre de S1-01 |
| Credenciales/conectividad JDBC | 2/1 | Obtener variables propias por canales locales; verificar Spring antes de persistencia |
| Builds no reproducidos completamente en auditoría S0 | 1 y todos | Repetir clean verify/build; conservar diagnósticos y resultado real |
| Capacidad/experiencia Docker | Todos | Registrar disponibilidad y revisión; ningún P0 se elimina automáticamente |
| Plan de Render, arranque en frío y costos | 1 | Documentar riesgos para futura publicación; sin contratación ni gasto |
| Recorte rentabilidad/reportes | Equipo | Validación docente pendiente; no ampliar ni declarar conformidad obtenida |

La topología de producción no bloquea pruebas locales de sesión; el modelo aprobado sí condiciona SQL y persistencia. Si una tarea queda bloqueada, registrar causa exacta y continuar las subtareas independientes.

## 6. Agenda y tareas trasladadas

10 octubre: herramientas/builds, JDBC local, diseño Compose/Render/modelo/historial/contratos. 11: Compose y JDBC contenedor; revisión/aprobación modelo antes de SQL. 12–13: esquema autorizado, login/maestros y frontend en paralelo. 14: oferta e integración. 15: preparación Render y regresión. 16: revisión/cierre. Es secuencia objetivo, no trabajo confirmado.

S1-07 y S1-08 se conservan con sus IDs en Sprint 2 (17–23 octubre): crear/consultar pedidos y confirmar con State. No forman parte de criterios/compromiso de Sprint 1 y no se rescatan como opcionales. Modelo de pedidos, reservas/transiciones y Strategy se acordarán en esa etapa. Calendario posterior completo en PLANIFICACION_SPRINT_1.md, hasta el 31 de octubre.

## 7. Inicio sin nueva actualización general

Se puede comenzar, en una siguiente etapa de implementación autorizada, por revisión de herramientas/builds y preparación Docker/Render (1), diagnóstico JDBC y propuesta de modelo/historial (2), contrato y matriz de sesión/CSRF/permisos (3), y diseño React/contratos de oferta (4). No hace falta formalizar nuevamente el backlog. Solo esperan aprobación las decisiones concretas aún abiertas y la publicación efectiva.

## 8. Registro inicial de formalización (histórico)

| Evidencia Sprint 1 | Estado inicial |
|---|---|
| mvn clean verify / suite backend | No ejecutado en esta formalización |
| npm ci / npm run build | No ejecutado en esta formalización |
| Docker build / Compose / navegador | No ejecutado en esta formalización |
| SELECT 1 desde Spring local/contenedor | Pendiente de S1-02; éxito IntelliJ reportado en S0 no lo sustituye |
| Modelo e historial aprobados / SQL aplicado | Pendiente; no hay SQL ni entidades creadas aquí |
| Login/roles/CSRF/historial/oferta funcionales | Pendientes de implementación y pruebas |
| Render público | No autorizado; no ejecutado |
| Veredicto Sprint 1 | Pendiente de cierre del 16 de octubre |

Actualizar esta tabla con fecha, comando, resultado y origen de evidencia al trabajar. La formalización documental no se contabiliza como pruebas de aplicación aprobadas.

## 9. Ejecución autorizada S1-01 — 9 de octubre de 2026

**Estado al finalizar la implementación: En progreso, no Terminada (histórico). Estado vigente: En revisión; ver sección 10.** El usuario aprobó multi-stage backend/frontend, Java 21, Node/Nginx, proxy /api/, solo dos servicios y primer arranque dev sin PostgreSQL. El alcance general no cambia; PLANIFICACION_SPRINT_1.md no se modificó. Las alternativas Nginx/multi-stage ya no están pendientes para el entorno local; la topología de producción aún lo está.

### Archivos implementados

- backend/Dockerfile: fuentes Maven/Java 21, clean verify sin omitir tests; runtime JRE 21 y usuario comunitarias; curl para healthcheck.
- frontend/Dockerfile: npm ci con lockfile, npm run build y base API vacía solo en imagen; dist servido por Nginx.
- backend/.dockerignore y frontend/.dockerignore: lista mínima de fuentes/configuración; excluyen .env/credenciales/Git/node_modules/target/dist y archivos innecesarios.
- frontend/nginx.conf: SPA fallback y proxy /api/ hacia backend:8080; mantiene prefijo/query/Host y estados de seguridad, resolver DNS Docker.
- compose.yaml: backend/frontend solamente, red bridge, puertos locales configurables, dev fijo, healthchecks y espera backend healthy.
- compose.supabase.yaml: override preparatorio, no ejecutado, con tres variables existentes obligatorias y solo perfil supabase. No crea servicio PostgreSQL ni SQL.
- README.md y DECISIONES_ARQUITECTURA.md actualizados; este documento registra ejecución y pendientes.

No se cambiaron fuentes React, POM, package.json/lockfile, propiedades Spring ni SecurityConfig. Fuera de Docker la base API conserva localhost:8080 o frontend/.env; en Docker VITE_API_BASE_URL vacío genera URL del mismo origen. CORS del contenedor recibe orígenes explícitos localhost/127.0.0.1 del puerto frontend; no se debilitaron filtros ni CSRF. No hay autenticación funcional ni entidades/migraciones añadidas.

### Comandos realmente ejecutados y resultados

Las verificaciones se lanzaron mediante procesos Node porque el entorno del agente dispone de restricciones de ejecución. Docker no se instaló en esta tarea. Maven fuera de PATH se invocó con el launcher de IntelliJ y Temurin 21.0.12.1; su objetivo real y argumentos están registrados en .cache/s1-01/VERIFICACIONES.json. Logs locales allí, ignorados por Git. npm se invocó con node/npm-cli.js, sin modificar scripts del proyecto.

| Verificación | Resultado observado | Límite |
|---|---|---|
| Get-Command docker y ruta habitual Docker Desktop | Sin comando Docker; docker.exe habitual inexistente | No certifica disponibilidad en la sesión normal del usuario |
| docker compose config | Intentado; spawn docker ENOENT (código -4058) | No se validó con Compose, tampoco basta revisión estática |
| docker compose build --no-cache / up -d / ps | No ejecutados: falta ejecutable Docker | Pendientes de Windows; ninguna imagen/servicio declarado exitoso |
| mvn -o -B clean verify, Java 21, copia limpia .cache/s1-01/backend | Código 1; compiler release 21, Cannot close compiler resources, referencia starter-tomcat | No llegó a tests/empaquetado; no equivale a compilación aprobada |
| Reintento con caché Maven copiada al workspace | Código 1; clean no pudo eliminar target de copia temporal | No se eliminó target original ni se ocultó el fallo |
| Reintento en nueva copia backend-clean sin target previo | Código 1; Cannot close compiler resources, referencia jackson-datatype-jdk8 | Causa exacta no resuelta; no se cambió configuración para saltarlo |
| mvn -o -B surefire:test en backend con clases existentes | BUILD SUCCESS, 6 tests, 0 fallos, 0 errores, 0 omitidos, Java 21 y perfil dev | Pruebas reales sobre clases existentes, no clean verify ni imagen Docker |
| npm ci en copia limpia sin node_modules/dist | Código 1; EACCES al descargar yallist-3.1.1.tgz de registry.npmjs.org | Instalación limpia no completada; las dependencias originales se conservaron |
| npm run build en frontend, base API vacía y dependencias existentes | Código 1 al cargar vite.config.ts por acceso denegado; tsc -b llegó sin errores a Vite | Build estándar no exitoso |
| node node_modules/vite/bin/vite.js build --configLoader runner, misma base vacía | Código 0; Vite 7.3.7, 31 módulos transformados | Usa node_modules existentes; no sustituye npm ci ni build Docker |
| Revisión del bundle generado | Base vacía concatenada con /api/health; sin http://backend ni localhost:8080 en ese bundle | Verificación de artefacto, no llamada de navegador observada |
| Fetch http://localhost:8080/api/health y http://localhost:8081/api/health | ECONNREFUSED en ambos | No había servicios; no son pruebas de contenedores ni del proxy |
| Nginx -t, SPA fallback y «API disponible» en Docker | No ejecutados | Pendientes de contenedores y navegador reales |

npm ci utilizó --cache del workspace, --fetch-retries=0, --fetch-timeout=10000, --no-audit y --no-fund; los dos últimos desactivan tareas adicionales npm, no omiten compilación/pruebas. El npm run build posterior utilizó las dependencias existentes porque la instalación aislada no pudo completarse; se distingue expresamente de la secuencia limpia solicitada. Log npm: .cache/npm/_logs/2026-10-09T04_59_43_803Z-debug-0.log. No se ejecutó auditoría de vulnerabilidades npm ni se afirma que haya pasado.

Maven surefire:test emitió avisos de agente dinámico Mockito/Byte Buddy, class sharing y demora SecureRandom; no provocaron fallos. Se conservaron los seis tests de infraestructura y no se agregaron opciones para ocultarlos. No se afirma que todos los fallos de compilación sean exclusivamente permisos: la causa concreta del cierre de recursos continúa pendiente de diagnóstico.

### Matriz de aceptación anterior a la validación Windows (histórica)

| Criterio | Estado |
|---|---|
| Dos servicios, dev fijo, sin PostgreSQL/SQL y fuentes/lockfile | Implementado y revisado estáticamente; ejecución pendiente |
| Dockerfiles multi-stage, exclusiones, proxy y fallback SPA | Implementados; sintaxis Nginx/Compose y runtime aún no comprobados |
| Seguridad del backend existente conservada | Fuentes intactas; seis pruebas existentes pasaron; comportamiento por proxy pendiente |
| React relativo Docker, funcionamiento nativo conservado | Fuentes sin cambios y bundle relativo comprobado; navegador Docker pendiente |
| Build limpio backend/frontend sin artefactos host | Dockerfiles lo requieren; ejecución no comprobada, intentos nativos fallidos |
| Compose config/build/up/ps y healthchecks | Pendientes de Docker en Windows |
| GET directo/backend, proxy, 401/SPA y error/reintento UI | Pendientes; Fetch actuales fallaron sin servidores |
| Secretos fuera de fuentes/contextos/imágenes | No credenciales reales añadidas; revisión estática/exclusiones; inspección de imágenes pendiente |
| Documentación ejecución/parada/reconstrucción/logs | README actualizado con comandos Windows y diagnóstico |

### Checklist original para Windows (estado vigente en sección 10)

Desde raíz, con Docker Desktop iniciado en Linux containers:

~~~powershell
docker compose config
docker compose build --no-cache
docker compose up -d
docker compose ps
docker compose exec frontend nginx -t
curl.exe -i http://localhost:8080/api/health
curl.exe -i http://localhost:8081/api/health
curl.exe -i http://localhost:8080/api/recurso-futuro
curl.exe -i http://localhost:8081/api/recurso-futuro
curl.exe -i http://localhost:8081/ruta-de-prueba-spa
docker compose exec backend printenv SPRING_PROFILES_ACTIVE
docker compose logs --tail=100 backend frontend
~~~

Esperado: ambos healthy, solo dev; health directo/proxy 200 con UP; rutas protegidas 401; fallback SPA 200 con HTML. Abrir localhost:8081, comprobar «API disponible» y Network a localhost:8081/api/health. Detener solo backend, reintentar UI y ver no disponible; iniciar backend, esperar salud y recuperar disponible. Ver README para puertos alternativos, inspección de runtime, rebuild y down/up. No tomar los resultados esperados como observados.

En entorno nativo con Java 21/Maven y Node compatibles, ejecutar mvn clean verify desde backend y npm.cmd ci seguido de npm.cmd run build desde frontend, deteniéndose si falla la instalación. Dockerfiles también ejecutan verify y build completos; ninguna prueba se omite para construir.

Este fue el diagnóstico anterior a la validación Windows: implementación de archivos disponible, validación ejecutable incompleta. La sección 10 actualiza las evidencias y los pendientes; solo después de satisfacer aceptación y revisión cruzada puede pasar S1-01 a Terminada. S1-02 sigue Por hacer: override Supabase preparado, sin activación ni prueba JDBC. No se inició otra tarea ni se publicó/commiteó/pusheó.

## 10. Primera revisión de cierre de S1-01 — registro histórico

**Estado de esta primera revisión: En revisión. El estado vigente y los pendientes actualizados figuran en la sección 11.** La implementación y su funcionamiento principal tienen evidencia reportada favorable. Aún faltan comprobaciones obligatorias de aceptación y DoD; no se marca Terminada ni se modifica el estado de ninguna otra tarea.

### Origen y alcance de la evidencia

Los resultados siguientes fueron **observados por el usuario en su Windows y comunicados en esta sesión**. El agente no ejecutó estos comandos ni observó ese navegador; no se adjuntaron logs completos, capturas o fecha/hora de ejecución. Se aceptan como evidencia reportada, diferenciada de las verificaciones propias de la sección 9. En esta revisión el agente únicamente leyó este documento y la configuración Docker vigente; no repitió pruebas de ejecución ni modificó código.

| Comando o comprobación reportada | Resultado informado | Alcance y limitación |
|---|---|---|
| docker compose build --no-cache | Backend y frontend construidos correctamente | Build limpio reportado de ambas imágenes; sin logs nuevos ni conteo de tests aportado |
| docker compose up -d | Ambos servicios iniciados | Arranque reportado |
| docker compose ps | Ambos contenedores healthy | Salud reportada de backend y frontend; el healthcheck frontend comprueba Nginx/estáticos, no Supabase |
| http://localhost:8081 | React carga y muestra «API disponible» | Integración visual reportada; URL exacta de la solicitud en Network no aportada |
| http://localhost:8080/api/health | Responde UP | Respuesta directa reportada; código HTTP no indicado explícitamente |
| http://localhost:8081/api/health | Responde UP mediante Nginx | Proxy funcional reportado; código HTTP no indicado explícitamente |
| GET /api/recurso-futuro | HTTP 401 | Protección reportada; no se especificó si se probó por 8080, por 8081 o por ambos |
| GET /ruta-de-prueba-spa | HTTP 200 con HTML | Fallback SPA reportado |
| nginx -t | Configuración correcta, test successful | Sintaxis Nginx validada según el usuario |
| SPRING_PROFILES_ACTIVE | dev | Perfil observado según el usuario; no se activó supabase para esta validación |
| docker compose stop backend y docker compose start backend | Ejecutados correctamente | Parada/reinicio reportados; no se informó el estado visual tras cada reintento |

La lectura de los Dockerfiles confirma Java 21, etapas separadas y ejecución de **mvn --batch-mode --no-transfer-progress clean verify**, **npm ci** y **npm run build**. Con esos archivos, el build sin caché exitoso reportado implica que dichas etapas terminaron correctamente dentro de Docker, sin copiar target/node_modules/dist del host. Esta conclusión combina el reporte del usuario con la configuración leída; no se presenta como ejecución propia ni como un nuevo resultado de seis tests. No es necesario repetir builds nativos para sustituir esta evidencia de construcción en Docker.

Los fallos previos del entorno del agente permanecen en la sección 9. El éxito en Windows aporta evidencia favorable para la construcción Docker, pero no demuestra que se haya corregido la causa de esos errores nativos. El único conteo de pruebas observado por el agente sigue siendo el de surefire:test sobre clases existentes: seis, sin fallos ni errores.

### Contraste de aceptación y Definition of Done

| Criterio | Resultado de esta revisión |
|---|---|
| Compose config válido | build/up reportados muestran que Compose pudo procesar la configuración; falta resultado explícito del comando obligatorio docker compose config |
| Build desde fuentes y lockfile, sin artefactos host ni omitir tests | Satisfecho por reporte de build sin caché y lectura de COPY/RUN/.dockerignore; sin nuevos logs detallados de tests |
| Java 21, multi-stage, ambos servicios y healthchecks | Configuración comprobada estáticamente; build, arranque y healthy reportados satisfactorios |
| Health 200/UP directo y por proxy | UP reportado en ambas URLs; falta registrar explícitamente HTTP 200 para ambas |
| React consume API real mediante URL accesible | «API disponible» reportado y configuración relativa comprobada; falta confirmar en Network localhost:8081/api/health |
| API no disponible y recuperación mediante reintento | Pendiente: stop/start exitosos no acreditan las transiciones visuales |
| Solo dos servicios, dev, sin PostgreSQL local/SQL | Dos servicios y dev comprobados en Compose; perfil dev y dos contenedores healthy reportados. JDBC/Supabase no corresponde cerrar en S1-01 |
| Proxy conserva seguridad y fallback SPA | SPA 200 HTML y ruta protegida 401 reportados; falta precisar/verificar 401 por el proxy y acceso directo |
| Exclusiones y ausencia de secretos | .dockerignore y ausencia de argumentos sensibles comprobados estáticamente; falta evidencia de revisión de archivos versionados, imágenes/capas, logs y bundle final |
| Ejecución, puertos, parada, reconstrucción y logs documentados | Documentación existente registrada en sección 9; aplicación reportada en 8081 y backend en 8080 |
| DoD: alcance y separación de responsabilidades | Sin ampliación de alcance ni cambios de código en esta revisión; no exige login, entidades, SQL ni patrones funcionales en S1-01 |
| DoD: compilación/tests e integración | Build Docker e integración principal reportados satisfactorios; se conservan resultados y errores anteriores sin ocultarlos |
| DoD: revisión por otro integrante y comprensión del equipo | Pendiente: no se aportó constancia de revisión cruzada ni confirmación de que los cuatro pueden explicar su trabajo |
| DoD: documentación/evidencias y acciones autorizadas | Documento actualizado con origen de evidencia; sin commit/push, publicación ni cambios de otras tareas en esta revisión |

### Comprobaciones exactas pendientes para marcar Terminada

1. **Configuración y HTTP:** ejecutar docker compose config y registrar éxito sin compartir valores sensibles; confirmar HTTP 200/UP directo y por proxy, y HTTP 401 en ambas URLs protegidas. El reporte actual no identifica la URL del 401 ni los códigos de health.

   ~~~powershell
   docker compose config
   curl.exe -i http://localhost:8080/api/health
   curl.exe -i http://localhost:8081/api/health
   curl.exe -i http://localhost:8080/api/recurso-futuro
   curl.exe -i http://localhost:8081/api/recurso-futuro
   ~~~

2. **Navegador y reintento:** en Network confirmar que React solicita http://localhost:8081/api/health. Con frontend abierto, detener backend, pulsar reintento y observar «API no disponible»; iniciar backend, esperar healthy, reintentar y observar «API disponible». Registrar ambos resultados. Los comandos stop/start ya tienen evidencia reportada; falta el comportamiento de React.
3. **Secretos:** registrar una revisión sin divulgar valores de archivos versionados/contextos, configuración y capas de las imágenes construidas, logs y JavaScript servido. Confirmar que no contienen credenciales reales, archivos .env ni variables JDBC expuestas al frontend. Las exclusiones estáticas no certifican por sí solas todos los artefactos finales. No se exige una auditoría exhaustiva del historial Git ni publicar salidas sensibles.
4. **Revisión cruzada:** registrar quién revisó S1-01 (revisores previstos: integrantes 3 y 4), qué comprobó y resultado; confirmar con el equipo la comprensión del funcionamiento y comandos, conforme a DoD 2 y 7.

No se identificó un fallo funcional nuevo a partir del reporte. El impedimento para cerrar es la falta de estas evidencias, no la implementación de login, JDBC, entidades o despliegue Render. S1-02 y todas las demás tareas conservan sus estados y criterios anteriores. No se realizó commit ni push.

## 11. Segunda revisión de cierre S1-01 — evidencias adicionales y seguridad (histórica)

**Estado en esta segunda revisión: En revisión. El estado vigente se encuentra en la sección 12.** Se incorporan las nuevas observaciones comunicadas por el usuario en Windows. No se repitieron por el agente; no se confunden con la revisión estática ejecutada aquí. Las secciones 9 y 10 conservan resultados históricos, incluidos errores anteriores. No cambia ninguna otra tarea.

### Nuevas pruebas reportadas por el usuario

| Comprobación | Resultado reportado | Criterio actualizado |
|---|---|---|
| docker compose config --quiet | Exitoso, sin errores | Validación explícita Compose satisfecha; modo quiet evita divulgar configuración interpolada |
| GET localhost:8080/api/health | HTTP 200 | Health directo satisfecho junto al UP reportado previamente |
| GET localhost:8081/api/health | HTTP 200 | Health por Nginx satisfecho junto al UP previo |
| GET localhost:8080/api/recurso-futuro | HTTP 401 | Protección directa satisfecha; no acredita aún 401 por el proxy |
| DevTools Network | React solicita /api/health desde localhost:8081 y recibe HTTP 200 | Consumo real, relativo y del mismo origen satisfecho |
| Backend detenido | React muestra «API no disponible», HTTP 502 en DevTools | Estado de error reportado y comprobado por el usuario |
| Backend reiniciado | React vuelve a «API disponible», HTTP 200 en DevTools | Recuperación visual reportada y comprobada por el usuario |

Se acepta el comportamiento de error/recuperación informado como evidencia del criterio de estados UI; no se exige repetirlo. No se afirmó observación directa del navegador por el agente. Builds sin caché, healthy, SPA, Nginx y perfil dev mantienen la evidencia favorable reportada anteriormente.

### Revisión de seguridad ejecutada por el agente (solo lectura)

- Git disponible: versión 2.55.0.windows.3. El primer git ls-files falló con código 128 por dubious ownership. Se repitió con git -c safe.directory=<ruta del proyecto> ls-files -z, limitado a ese proceso: código 0. No se modificó configuración Git ni se efectuaron commits/push.
- Inventario Git: **60 archivos**, **40 textos** examinados en las copias de trabajo y **20 binarios** excluidos del análisis textual (incluidos documentos académicos). Se leyó además el contenido de los textos del índice mediante git show :<ruta>, sin imprimirlo. No se auditó el historial completo ni el contenido de PDF/PPTX.
- Búsqueda de indicadores: claves privadas, formatos conocidos de tokens GitHub/AWS/Supabase, estructura JWT y contraseñas embebidas en JDBC. **Sin coincidencias** en los textos examinados ni en el índice. Una búsqueda adicional de asignaciones password/secret/token/api-key en el índice identificó una referencia de configuración en application-supabase.properties, clasificada como referencia a entorno, sin copiar su valor. Esto es una detección limitada por patrones, no una garantía universal de ausencia de secretos.
- Los únicos nombres sensibles del inventario fueron .env.example y frontend/.env.example: ejemplos revisados, no archivos .env reales. .gitignore excluye entorno, credenciales y generados; las exclusiones no eliminan por sí mismas un secreto que ya estuviera indexado.
- Leídos ambos Dockerfiles, ambos .dockerignore, compose.yaml, compose.supabase.yaml y nginx.conf. Contextos mínimos de fuentes/lockfile, sin copiar entorno ni artefactos del host; exclusiones de credenciales, claves, Git y generados. No hay ARG sensibles; frontend solo define base API pública vacía. Compose predeterminado publica puertos en loopback, contiene dos servicios y fija exclusivamente dev; el override referencia variables JDBC solo en backend y sustituye el perfil por supabase. No se activó.
- Proxy conserva /api/ y estados HTTP sin convertir errores de API en fallback SPA; la comprobación real de 401 por Nginx continúa pendiente.
- Bundle **local** existente: index.html, un JavaScript y un CSS en frontend/dist. No se detectaron los patrones anteriores ni nombres de variables de usuario/contraseña Supabase o prefijo JDBC en esos archivos. No se reconstruyó; este bundle local no certifica el contenido de la imagen construida en Windows ni el actualmente servido por Nginx.
- Docker --version: no ejecutable disponible (ENOENT). **No se inspeccionaron imágenes, capas, configuración de contenedores, logs Docker ni bundle servido.** No se presentaron estas verificaciones como aprobadas.

No se mostraron contraseñas, tokens, cadenas JDBC completas ni valores sensibles. No se detectó un hallazgo positivo de secreto en el alcance examinado.

### Comandos seguros para las comprobaciones locales pendientes

Desde la raíz, con Docker Desktop y los servicios dev activos. Para verificar únicamente el código por el proxy, sin imprimir cuerpo ni cabeceras:

~~~powershell
curl.exe --silent --output NUL --write-out "%{http_code}" http://localhost:8081/api/recurso-futuro
~~~

Esperado: **401**. Registrar el resultado real.

Para una inspección automatizada de solo lectura de fuentes indexadas, configuración de imágenes/contenedores, historial de construcción, todas las capas guardadas por Docker, logs y bundle servido, puede ejecutarse el siguiente bloque con **Python 3 (py -3)**. No extrae archivos ni modifica imágenes. Captura las salidas internamente y muestra solamente etiqueta, resultado y categorías; no compartir salidas crudas de inspect/history/logs/save. La lectura de capas puede consumir memoria. Ante error se informa NO VERIFICADO, nunca aprobado. Los patrones son indicios y deben complementarse con revisión local por los integrantes; no detectan todos los posibles secretos.

~~~powershell
@'
import io, os, re, subprocess, tarfile
rules = {
    "clave privada": rb"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----",
    "token conocido": rb"(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{30,}|AKIA[A-Z0-9]{16}|sb_secret_[A-Za-z0-9_-]{15,})",
    "JWT": rb"eyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}",
    "JDBC con credencial": rb"jdbc:postgresql:[^\s\"']*(?:password=|://[^\s:/]+:[^\s@]+@)",
}
known = [os.environ[k].encode() for k in ("SUPABASE_DB_PASSWORD",) if os.environ.get(k)]
def run(args):
    p = subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if p.returncode: raise RuntimeError("comando fallido")
    return p.stdout + (p.stderr if args[:2] == ["docker", "logs"] else b"")
def scan(data):
    hits = {name for name, pattern in rules.items() if re.search(pattern, data)}
    if any(value in data for value in known): hits.add("secreto del entorno presente")
    return hits
def report(label, hits):
    print(label + ": " + ("REVISAR [" + ", ".join(sorted(hits)) + "]" if hits else "sin indicios en patrones aplicados"))
try:
    paths = run(["git", "ls-files", "-z"]).split(b"\0")
    hits = set()
    for path in filter(None, paths):
        textpath = os.fsdecode(path)
        if re.search(r"(?i)(?:^|/)(?:\.env(?:\..*)?|credentials[^/]*|secrets[^/]*)(?:/|$)|\.(?:pem|key|p12|jks)$", textpath) and not textpath.endswith(".example"):
            hits.add("archivo sensible indexado")
        hits.update(scan(run(["git", "show", ":" + textpath])))
    report("indice Git", hits)
except Exception: print("indice Git: NO VERIFICADO")
for service in ("backend", "frontend"):
    try:
        image = run(["docker", "compose", "images", "-q", service]).decode().strip()
        container = run(["docker", "compose", "ps", "-q", service]).decode().strip()
        if not image or not container: raise RuntimeError("no disponible")
        for label, args in (
            ("imagen/config", ["docker", "image", "inspect", image]),
            ("contenedor/config", ["docker", "inspect", container]),
            ("historial", ["docker", "history", "--no-trunc", "--format", "{{.CreatedBy}}", image]),
            ("logs", ["docker", "logs", container]),
        ):
            report(service + "/" + label, scan(run(args)))
        hits = set()
        with tarfile.open(fileobj=io.BytesIO(run(["docker", "save", image])), mode="r:*") as archive:
            for member in archive:
                if not member.isfile(): continue
                payload = archive.extractfile(member).read()
                hits.update(scan(payload))
                if member.name.endswith("/layer.tar") or member.name == "layer.tar":
                    with tarfile.open(fileobj=io.BytesIO(payload), mode="r:*") as layer:
                        for item in layer:
                            if re.search(r"(?i)(?:^|/)(?:\.env(?:\..*)?|credentials[^/]*|secrets[^/]*)(?:/|$)", item.name):
                                hits.add("archivo sensible en capa")
        report(service + "/capas", hits)
    except Exception: print(service + ": NO VERIFICADO (error o acceso insuficiente)")
try:
    bundle = run(["docker", "compose", "exec", "-T", "frontend", "sh", "-c", "find /usr/share/nginx/html -type f -exec cat {} \\;"])
    hits = scan(bundle)
    if re.search(rb"SUPABASE_DB_PASSWORD|SUPABASE_DB_USER|jdbc:postgresql:", bundle):
        hits.add("configuracion BD en frontend")
    report("bundle servido", hits)
except Exception: print("bundle servido: NO VERIFICADO")
'@ | py -3 -
~~~

Revisar localmente cualquier categoría REVISAR sin enviar su contenido. El script compara también la contraseña Supabase solo si existe en el entorno local; no la imprime. No es necesario establecerla ni activar supabase para S1-01. El análisis de logs captura stdout y stderr internamente. Si el archivo exportado usa capas comprimidas/blobs OCI en lugar de layer.tar, el escaneo de bytes no garantiza inspección del contenido descomprimido: revisar esas capas localmente con una herramienta que soporte ese formato y no marcar el criterio completo con esa limitación.

### Qué falta exactamente para cerrar

1. **401 mediante Nginx:** aportar resultado de GET http://localhost:8081/api/recurso-futuro. El 401 directo ya está acreditado por reporte.
2. **Artefactos Docker y logs reales:** registrar inspección local favorable de configuración/historial/capas de ambas imágenes, logs de ambos servicios y bundle servido; confirmar ausencia de credenciales reales y archivos de entorno. La revisión propia de fuentes/índice/exclusiones y bundle local queda realizada, con las limitaciones indicadas. Un resultado NO VERIFICADO o un indicio sin resolver mantiene este punto pendiente.
3. **Revisión cruzada de integrantes 3 y 4:** ambos deben efectuarla y registrar responsable, alcance, resultado y observaciones resueltas; confirmar comprensión del funcionamiento/comandos por el equipo según DoD. No se sustituye por la revisión del agente ni se presenta como realizada.

Config quiet, health 200/UP, consumo relativo y error/recuperación UI ya no están pendientes. JDBC, login, SQL y Render corresponden a otras tareas y no se exigen para cerrar S1-01. Sin modificaciones funcionales, commits ni push.

## 12. Revisión final S1-01 — aprobación del equipo y limitación de seguridad

**Estado vigente del Kanban: En revisión final.** No Terminada. Se registra la información comunicada por el usuario en esta sesión, sin atribuir al agente pruebas nuevas ni inventar comandos, fechas, capturas o resultados individuales. Esta actualización es exclusivamente documental.

### Aprobaciones reportadas

| Evidencia | Origen | Resultado y alcance |
|---|---|---|
| Revisión de integrante 3 | Reportada por el usuario | Implementación aprobada sin observaciones |
| Revisión de integrante 4 | Reportada por el usuario | Implementación aprobada sin observaciones |
| Pruebas funcionales Docker | Aprobación global reportada por el usuario | Se acepta el cierre funcional; conservadas las pruebas detalladas de secciones anteriores. No se inventa una ejecución individual nueva del 401 por proxy |
| Controles iniciales de seguridad | Reportados por el usuario | Metadatos, historial de imágenes, logs, bundle frontend y archivos visibles sin indicadores sensibles detectados |
| Inspección exhaustiva de capas históricas | Usuario confirma que no se realizó | Limitación expresa: no acredita ausencia de secretos en contenido de capas, incluidos archivos eliminados u ocultos por capas posteriores |

La revisión cruzada ya no está pendiente: ambos revisores previstos aprobaron sin observaciones. Esta aprobación no se atribuye a una inspección de capas que el usuario declara no realizada. Los controles iniciales favorables no constituyen garantía exhaustiva de ausencia de secretos. La revisión estática ejecutada por el agente en la sección 11 mantiene su alcance y limitaciones; no se ejecutó una nueva auditoría técnica en esta actualización.

### Contraste con aceptación y Definition of Done

La sección 3, criterio de aceptación S1-01, establece literalmente: **«.dockerignore excluye secretos/generados y archivos ajenos; no hay secretos en capas, argumentos de construcción, logs ni VITE_*.»** La DoD exige **«credenciales externas y seguridad verificadas»** (punto 4), **«Sin secretos versionados/embebidos»** (punto 6), resultados comprobados/reportados/pendientes separados (punto 7) y revisión por otro integrante (punto 2).

La DoD no prescribe una auditoría exhaustiva de todas las imágenes antiguas, de todo el registro Docker ni de todo el historial Git. Ese alcance histórico general **no es obligatorio**. Sin embargo, la ausencia de secretos **en las capas de las imágenes que se entregan en S1-01** sí es un criterio explícito. El historial de instrucciones y los metadatos no inspeccionan por sí solos los bytes de los archivos de cada capa; los archivos visibles solo muestran el sistema de archivos final. Las exclusiones estáticas tampoco sustituyen esa comprobación.

Por tanto, **el criterio de capas de las imágenes entregadas continúa sin evidencia suficiente**. No se cambia ni flexibiliza la aceptación para declarar la tarea Terminada. La aprobación funcional y la revisión cruzada quedan satisfechas según el reporte; los controles iniciales de seguridad quedan acreditados con su alcance real.

### Única comprobación pendiente para cerrar S1-01

Verificar localmente el contenido de **todas las capas de las dos imágenes entregadas (backend y frontend)**, contemplando archivos que posteriormente hayan sido eliminados u ocultados, y registrar:

- Identificadores de las imágenes inspeccionadas y herramienta/método utilizado.
- Confirmación de lectura efectiva del contenido de capas, con descompresión si el formato lo requiere; no basta escanear metadatos ni bytes comprimidos.
- Resultado de búsqueda/revisión de credenciales reales, claves/tokens y archivos sensibles, sin publicar sus valores; confirmar que no quedan hallazgos sin resolver.
- Responsable y resultado de esta comprobación.

Puede utilizarse la guía local de la sección 11, respetando su limitación con capas comprimidas/OCI y complementándola cuando corresponda. Un resultado NO VERIFICADO no satisface el criterio. No hace falta investigar todas las imágenes históricas ajenas a la entrega ni repetir las pruebas funcionales o la aprobación de integrantes 3 y 4. Si esta comprobación resulta favorable, S1-01 podrá pasar a Terminada con la limitación histórica general documentada.

**Kanban:** S1-01 En revisión final; S1-02, S1-03, S1-04, S1-05, S1-06, S1-09 y S1-10 conservan sus estados anteriores. No se modificaron código ni criterios de otras tareas; no se realizaron commits ni push.
