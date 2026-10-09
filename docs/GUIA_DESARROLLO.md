# Guía de desarrollo — Comunitarias

Para estudiantes con base de Java/POO. Antes de programar: [pendientes y fuentes](MVP_PENDIENTES.md), [Kanban](KANBAN_SPRINTS.md), [plan formal S1](PLANIFICACION_SPRINT_1.md), [decisiones](DECISIONES_ARQUITECTURA.md) y AGENTS.md. Esta guía enseña el flujo; no implementa ni autoriza funcionalidades adicionales.

## 1. Preparar el entorno

1. Obtener acceso al repositorio privado y autenticación GitHub local. No introducir tokens en URL de remotos ni documentos.
2. Clonar el fork acordado y abrir la carpeta completa en IntelliJ IDEA:

~~~powershell
git clone https://github.com/mcirielli/Proyecto-Comunitarias.git
cd Proyecto-Comunitarias
git status
git remote -v
~~~

3. Instalar/configurar JDK **21**. En IntelliJ seleccionar Project SDK y SDK del módulo backend 21; revisar JDK de Maven importer/runner. Abrir backend/pom.xml y Add as Maven Project/importar Maven. No hay Maven Wrapper en el repositorio: usar Maven del IDE o una instalación disponible.
4. Backend real: Java 21/Spring Boot 3.5.16, Maven, Web/JPA/Security/Validation/PostgreSQL/Test. No actualizar versiones para comenzar. Para terminal: JAVA_HOME/PATH y Maven >=3.6.3; comprobar java -version y mvn -version. [Requisitos oficiales Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
5. Frontend en frontend/: package.json/lockfile fijan dependencias; Node >=22.12.0, npm. Docker usa Node 24.21.0; usar un Node compatible fuera de Docker. React/TypeScript no se importan como módulo Maven.

## 2. Ejecutar y probar desde IDE/terminal

En IntelliJ ejecutar ComunitariasApplication con **solo dev**, sin credenciales; el perfil predeterminado también es dev. No activar dev junto a supabase. El backend local usa 8080. Detener contenedor backend o cambiar BACKEND_HOST_PORT para evitar conflicto con la ejecución del IDE.

~~~powershell
# Desde la raíz
cd backend
mvn clean verify
if ($LASTEXITCODE -ne 0) { throw 'Resolver Maven/tests antes de continuar' }
$env:SPRING_PROFILES_ACTIVE = 'dev'
mvn spring-boot:run
~~~

JUnit: InfrastructureTest tiene seis casos de infraestructura. Ejecutar desde IntelliJ o Maven; no asumir un verify aprobado si solo se corrieron tests sobre clases existentes. Unitarios de reglas/patrones futuros sin Supabase; integración JDBC separada, con credenciales externas. Si falla, conservar diagnóstico y no usar skipTests para construir una entrega.

En otra terminal desde raíz:

~~~powershell
cd frontend
if (!(Test-Path -LiteralPath .env)) { Copy-Item .env.example .env }
npm.cmd ci
if ($LASTEXITCODE -ne 0) { throw 'Resolver instalación antes de continuar' }
npm.cmd run build
if ($LASTEXITCODE -ne 0) { throw 'Resolver build antes de continuar' }
npm.cmd run dev
~~~

Abrir localhost:5173. Default de health.ts: API localhost:8080; frontend/.env puede establecer VITE_API_BASE_URL público. Vite config actual no tiene proxy API. Base vacía solo funciona con proxy del mismo origen, como Nginx Docker; no copiar ese valor a nativo sin preparar proxy.

**IDE:** facilita debug/breakpoints y compila con herramientas del host. **Docker:** construye fuentes en imágenes y ejecuta versiones de contenedor; no sincroniza automáticamente cada edición. Ver [guía Docker](GUIA_DOCKER.md). No se necesita Java/Node/Maven del host para levantar Compose.

## 3. Organización real y responsabilidades

Base Java: backend/src/main/java/ar/edu/uade/comunitarias/.

| Ruta | Para qué sirve | Estado comprobado |
|---|---|---|
| domain/ | Entidades/reglas y objetos del negocio | Existe solo package-info.java, sin entidades |
| service/ | Casos de uso, validaciones/coordinación y transacciones | Existe solo package-info.java |
| repository/ | Interfaces de acceso a persistencia, Spring Data JPA | Existe solo package-info.java; sin repositorios |
| controller/ | Contrato HTTP y delegación | HealthController técnico |
| dto/ | Entrada/salida REST sin exponer entidades JPA | HealthResponse técnico |
| mapper/ | Convertir DTO/domain con responsabilidad delimitada | Existe solo package-info.java |
| config/ | Seguridad, perfiles y configuración técnica | SecurityConfig, CORS/BCrypt/filtros iniciales |
| strategy/, state/ | Participantes de los patrones cuando se implementen | Futuras, no existen; no crearlas sin tarea autorizada |
| src/main/resources/ | application.properties y perfiles | dev/supabase existentes |
| src/test/java/ | Tests JUnit | InfrastructureTest existente |
| frontend/src/components/ | Piezas de presentación reutilizables | ApiStatus.tsx |
| frontend/src/pages/ | Pantallas | HomePage.tsx técnica |
| frontend/src/services/ | Fetch y contratos HTTP frontend | health.ts técnico; no lógica comercial |
| frontend/src/hooks/, types/ | Abstracciones/tipos reutilizables cuando sean necesarios | Futuras; hoy tipos viven en componentes/servicio |
| database/ | SQL versionado aprobado/datos ficticios | Solo README.md; schema.sql y seed.sql no existen |

No crear paquetes por estética ni presentar package-info como patrón completo. Flujo objetivo: **React → REST Controller → Service → Domain/Repository → PostgreSQL**. Solo React→HealthController/DTO está implementado; health no atraviesa persistencia. React decide presentación, el servidor valida precios/permisos/estados.

## 4. De requerimiento a funcionalidad orientada a objetos

1. Leer fuente, criterio de tarea y decisiones; distinguir obligatorio, aprobado y propuesto.
2. Identificar actor y caso de uso: quién solicita, precondiciones, resultado y errores.
3. Consultar UML académico; actualizar diseño candidato si cambia y obtener aprobación pendiente. No representar como código una clase aún propuesta.
4. Identificar entidades, atributos, relaciones y responsabilidades; precisar invariantes y valores monetarios BigDecimal.
5. Diseñar clases e interfaces Java con encapsulamiento: operaciones que mantienen reglas en lugar de setters sin control. Crear solo participantes necesarios al alcance autorizado.
6. Implementar reglas en Domain y coordinar casos de uso en Service; aplicar transacción cuando cambian varias entidades. Controller no calcula negocio.
7. Persistir mediante interfaces Repository cuando corresponda; constraints SQL complementan validaciones, no las sustituyen. Modelo/esquema deben aprobarse antes de entidades/SQL.
8. Definir DTO de entrada/salida, validación y errores; Mapper convierte explícitamente. No exponer entidades JPA.
9. React llama servicio HTTP real; mostrar carga, error, reintento/vacío y respetar sesión/CSRF del contrato futuro. No simular éxito comercial.
10. Crear unitarios de invariantes/patrones, integración de persistencia/REST y prueba del flujo; revisar PR, actualizar UML/trazabilidad y aportar evidencias para DoD.

### Ejemplo didáctico: registrar un producto (futuro S1-05)

Actor Operaciones solicita alta de producto. Antes: modelo y contrato/permisos aprobados, S1-03/04. Después: producto persistido, ID estable aunque cambie proveedor. Campos concretos, unicidad y reglas de edición todavía no están aprobados.

| Archivo propuesto, no existente | Responsabilidad | Verificación propuesta |
|---|---|---|
| domain/Producto.java | Identidad, atributos e invariantes aprobados | No representar el proveedor como identidad del producto |
| dto/CrearProductoRequest.java / ProductoResponse.java | Entrada válida y salida pública | No exponer JPA ni credenciales |
| repository/ProductoRepository.java | Persistencia y consultas necesarias | Integración contra esquema revisado |
| service/ProductoService.java | Autorizar/coordinar alta y transacción | Caso válido/inválido sin efectos parciales |
| mapper/ProductoMapper.java | Conversión de representaciones | Correspondencia de campos; sin reglas de compra |
| controller/ProductoController.java | Recibir REST y delegar al servicio | HTTP/DTO/validación/permisos acordados |
| frontend/src/pages/... y services/... | Formulario y consumo API | Error/carga/success solo tras resultado real |
| Tests pertinentes | Regla unitaria y flujo integrado | Sin Supabase para unitarios, integración explícita |

Estos nombres ilustran responsabilidades; no se generaron archivos ni se aprobaron rutas/campos automáticamente.

### POO, SOLID y GRASP aplicados

- Encapsulamiento: Pedido mantiene sus invariantes al cambiar de estado; no dejar que la UI modifique stock/precio por sí sola. Abstracción: Repository/CompraStrategy describen contratos útiles. Composición: Pedido contiene detalles y delega a PedidoState; no inventar jerarquías para todo.
- Polimorfismo: CompraPorDemandaStrategy y CompraQuinterosStrategy implementan la misma interfaz con algoritmos diferentes; State usa objetos concretos. Evitar reemplazar ambos patrones con enums/if gigantes.
- SOLID: responsabilidad única por capa; agregar variante Strategy sin alterar todas las reglas (abierto/cerrado); implementaciones respetan su contrato; interfaces pequeñas; servicios dependen de abstracciones Repository. No crear abstracciones sin una necesidad real.
- GRASP: experto en información ubica reglas donde están sus datos; Controller recibe eventos y delega; alta cohesión/bajo acoplamiento separan HTTP/persistencia/negocio; Creator se aplica a objetos relacionados, sin Factory nueva obligatoria.

Spring proporciona inyección, HTTP, seguridad/transacciones/persistencia. Docker empaqueta y ejecuta. **Ninguno sustituye POO, UML ni reglas del dominio.**

## 5. Git, forks y Pull Requests

Repositorios: [Cirielli](https://github.com/mcirielli/Proyecto-Comunitarias), [Andrés](https://github.com/acedenosanchez/Proyecto-Comunitarias). Configuración local observada: origin apunta al fork Cirielli, upstream a Andrés. Cada integrante debe comprobar git remote -v; no asumir mismos remotos/permisos ni cambiarlos automáticamente. El superprompt propone un repositorio/develop; la colaboración real usa estos dos repositorios relacionados y main/ramas. La política estable del equipo debe acordarse; no introducir develop como requisito ejecutado.

Antes de cambiar rama, git status debe estar limpio o el trabajo propio preservado con acuerdo; no borrar, resetear ni mezclar cambios ajenos. Flujo recomendado para fork acordado:

~~~powershell
git switch main
git pull --ff-only origin main
git switch -c feature/s1-05-productos
# Realizar solo la tarea autorizada y ejecutar sus pruebas
git status
git diff --check
git diff --stat
# Preparar rutas explícitas de la tarea, evitando git add .
# git add <rutas-revisadas>
# git diff --cached --check
# git diff --cached
# git commit -m "feat: registrar productos con validacion"
# git push -u origin feature/s1-05-productos
~~~

Comandos de add/commit/push están comentados porque cada entrega requiere revisar rutas y autorización; esta guía no los ejecuta. Un PR debe describir problema/resultado, alcance, decisiones y comandos/resultados reales; incluir pruebas fallidas/pendientes. Solicitar revisión cruzada, resolver comentarios y conflictos conservando trabajo, no hacer force push.

**PR dentro de Cirielli:** rama feature/docs → main de Cirielli; verificar repositorio base/head al abrir. **Integración hacia Andrés:** después de revisión del equipo, abrir PR de rama/main del fork Cirielli hacia main de Andrés, con permiso y base/head correctos. No hacer push directo a upstream. Un solo PR puede cruzar repositorios si así se acuerda, evitando duplicar diffs.

**Tras merge en Andrés**, sincronizar local/fork sin sobrescribir historial (solo con remotos comprobados y árbol limpio):

~~~powershell
git fetch upstream
git switch main
git pull --ff-only origin main
git merge --ff-only upstream/main
# Solo tras revisar destino y autorización de publicación:
# git push origin main
~~~

Si ff-only falla hay divergencia: detener, revisar grafo y preparar merge/PR con el equipo; no usar reset --hard ni push --force. Si Andrés fue integrado por squash y los historiales divergen, acordar sincronización explícita. [Guía oficial de sincronización de forks](https://docs.github.com/en/pull-requests/how-tos/work-with-forks/syncing-a-fork).

Evidencia: usuario reportó PR #1 integrado; log local 65cd3c1 dice Merge pull request #1 from mcirielli/main. No se comprobó remoto actual.

## 6. Ambientes y Render

Local dev (IDE o Compose) permite HTTP sin BD. Perfil supabase conecta backend→PostgreSQL remoto con credenciales externas. React nunca conecta directo a BD. Render aprobado como destino de frontend estático y backend Docker, pero su despliegue concreto es **reportado, con configuración y verificación pendientes**. No asumir servicios/URLs/rama/perfil ni Compose remoto.

Antes de publicar una rama o hacer merge, comprobar qué rama observa Render y si hay auto-deploy. Para cambios solo documentales usar mensaje de commit **[skip render]** como precaución documentada; no activar deploy hooks, previews ni despliegues en esta tarea. [Render: despliegues y omisión de auto-deploy](https://render.com/docs/deploys). Datos públicos a completar en S1-09: URLs, tipo de servicio, repo/rama/SHA, contexto/build/dist, PORT/perfil, CORS y cookies. Los secretos se configuran localmente o en panel, nunca en PR.
