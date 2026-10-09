# Planificación formal del Sprint 1 — Comunitarias

**Período aprobado: 10 al 16 de octubre de 2026.** Formalización: 9 de octubre de 2026, a partir de la instrucción expresa del usuario. Estado inicial: plan aprobado en alcance; implementación no iniciada por esta formalización.

Este archivo actualiza la planificación existente. Es la única planificación vigente del Sprint 1; el documento operativo [SPRINT_1.md](SPRINT_1.md) desarrolla aceptación, DoD y estados. No constituye una segunda planificación. La presente ejecución autoriza únicamente la actualización documental: no se generan código, SQL, entidades, Dockerfiles ni configuraciones ejecutables.

## 1. Objetivo y compromiso aprobado

Entregar ejecución reproducible backend/frontend con Docker Compose, conexión JDBC real desde Spring Boot a Supabase y el primer incremento funcional **login → gestión de productos/proveedores/precios → consulta de oferta en React**. Preparar Render para publicación posterior, sin desplegar públicamente.

Compromiso obligatorio: **S1-01, S1-02, S1-03, S1-04, S1-05, S1-06, S1-09 y S1-10, todas P0**. S1-07/S1-08 no son opcionales del Sprint 1: quedan trasladadas al Sprint 2. No hay trabajo P1/P2 que permita reintroducir pedidos durante este sprint.

Se mantienen monolito por capas, Java 21, Spring Boot, Maven/JUnit 5, React/Vite/TypeScript/Tailwind y PostgreSQL administrado en Supabase. Sin microservicios, Kubernetes, Redis, mensajería ni componentes innecesarios. Las versiones actuales no se sustituyen por esta formalización.

## 2. Fuentes, decisiones y punto de partida

Fuentes principales: [AGENTS.md](../AGENTS.md), [SUPERPROMPT.md](../context/SUPERPROMPT.md), [decisiones de arquitectura](DECISIONES_ARQUITECTURA.md), [cierre Sprint 0](SPRINT_0.md), [reporte de auditoría](REPORTE_SPRINT_0.md), y las entregas académicas:

- [Proyecto Comunitarias — Grupo 2](../context/entregas/Proyecto%20_Comunitarias_%20-%20Grupo%202%20%282%29.pdf): centralización y trazabilidad, historial de precios y seguimiento.
- [Actividad 2 — Diseño del MVP](../context/entregas/Actividad%202%20-%20Proyecto%20Comunitarias%20-%20Grupo%202%20%281%29.pdf): procesos, capas y cinco patrones. El material de context/material-uade se consultará al validar sus participantes concretos.

Sprint 0: cerrado APROBADO CON OBSERVACIONES. Se repitieron seis tests con éxito sobre clases existentes, health 200, protección 401 y CORS. El build completo Maven no pudo reproducirse en el entorno auditor; el build alternativo Vite sí pasó. Los builds estándar exitosos, pantalla «API disponible», IntelliJ/SELECT 1 y primer push privado fueron reportados por el usuario. Esas evidencias no acreditan JDBC desde Spring. S1-02 y S1-10 resuelven las verificaciones propias de este incremento; aquí no se ejecutaron nuevas pruebas de aplicación.

### Decisiones aprobadas

- Docker Compose para ejecutar backend/frontend en desarrollo local; sustituye «No utilizar Docker» del superprompt y la prohibición histórica de Sprint 0 para el trabajo futuro de Sprint 1. No reescribe el alcance histórico de Sprint 0.
- Preparar Render: frontend como sitio estático y backend como contenedor Docker. **La publicación efectiva no está autorizada**, tampoco como tarea opcional.
- Mantener PostgreSQL en Supabase y el stack/monolito actual. No crear un PostgreSQL local en Compose.
- Login simplificado con cuentas precargadas, sesiones, BCrypt, CSRF y roles; sin JWT/OAuth. Persistencia y detalles de sesión aún requieren diseño.
- Historial de precios obligatorio; su representación física sigue pendiente.

Las decisiones aprobadas previamente sobre reservas/movimientos, autorización manual Quinteros, facturación/pagos y reportes de ventas/compras sin rentabilidad permanecen. La exclusión de rentabilidad sigue siendo recorte académico pendiente de validación docente.

Solo se actualiza la restricción Docker del superprompt. Sus referencias anteriores a selector de perfiles, rentabilidad o Vercel se conservan por la restricción de edición y se interpretan con las decisiones aprobadas posteriores; no se restauran como requisitos vigentes.

## 3. Backlog definitivo

| ID | Prioridad | Objetivo / historia | Responsable | Dependencias y puertas de aprobación | Resultado verificable |
|---|---|---|---|---|---|
| S1-01 | P0 | Como desarrollador, ejecutar backend y frontend mediante Docker Compose | Integrante 1; apoyo 4 | Docker/Compose aprobados; comprobar herramientas y definir detalles de imagen/servidor | Build desde fuentes; Compose válido; ambos servicios disponibles; React consulta health real; sin PostgreSQL local ni secretos en imagen |
| S1-02 | P0 | Comprobar JDBC real Spring Boot → Supabase | Integrante 2; apoyo 1 | Credenciales propias, perfil supabase; S1-01 para repetición en contenedor | SELECT 1 desde DataSource Spring en ejecución local y en contenedor; error controlado sin credenciales; health no se usa como prueba de BD |
| S1-03 | P0 | Definir y aplicar esquema SQL inicial y datos de demostración | Integrante 2; revisión 3 y 4 | Preparar modelo puede comenzar ya; aplicar SQL depende de S1-02 y aprobación explícita del modelo/historial | Modelo aprobado, PK/FK/restricciones, SQL versionado, datos ficticios y lectura/escritura real; sin tablas de pedidos ni recreación automática |
| S1-04 | P0 | Como usuario precargado, iniciar/cerrar sesión y usar operaciones de mi rol | Integrante 3; apoyo 4 | Diseño puede comenzar ya; persistencia depende de S1-03 y contrato de sesión/permisos revisado | Sesión real, BCrypt, CSRF y roles Cliente/Operaciones/Gerencia; login inválido rechazado; logout invalida; 401/403 apropiados; sin JWT/OAuth |
| S1-05 | P0 | Como Operaciones, gestionar productos, proveedores e historial de precios | Integrante 2; apoyo 4 | S1-03, aprobación del historial; S1-04 para acceso integrado | ID estable; precios anteriores conservados; validación/BigDecimal; DTOs, Repository y Service Layer; pruebas independientes de Supabase |
| S1-06 | P0 | Como Cliente, consultar la oferta desde React | Integrante 4; apoyo 2 para API/datos | Contratos pueden prepararse ya; integración depende de S1-03/S1-04/S1-05 | Oferta real desde API con precios de venta; sin precios de compra expuestos al Cliente; estados carga/vacío/error y sesión; sin pedidos |
| S1-09 | P0 | Preparar configuración y documentación Render | Integrante 1; apoyo 3 y 4 | Preparación puede comenzar ya; validar artefactos depende de S1-01/S1-02; sesiones/cookies de producción aún pendientes | Backend Docker y frontend estático preparados; puerto, variables, CORS, build, health, checklist de publicación/reversión documentados; sin publicación efectiva |
| S1-10 | P0 | Ejecutar pruebas, integración, revisión cruzada y cierre | Los cuatro | Pruebas se desarrollan con cada tarea; cierre depende de los ocho P0 y sus criterios | Builds/pruebas efectivamente ejecutados, evidencia JDBC/Compose/UI, permisos y CSRF; documentación actualizada; veredicto honesto, sin resultados supuestos |

Todos los criterios detallados del documento operativo forman parte de estas tareas. Una tarea no se considera terminada por tener archivos escritos o por contar con una aprobación documental.

### Tareas trasladadas al Sprint 2

| ID conservado para trazabilidad | Alcance | Dependencias futuras | Criterios de referencia, todavía no implementados |
|---|---|---|---|
| S1-07 | Crear y consultar pedidos | S1-03/S1-04/S1-05/S1-06; aprobación del modelo y reglas de Pedido | Detalles y precios históricos persistidos; cantidades válidas; precio decidido por servidor; transacción; aislamiento por Cliente |
| S1-08 | Confirmar pedidos con State | S1-07; aprobación de transiciones y relación con reservas | Clases de State y delegación reales; transición válida persistida; salto inválido rechazado; reconstrucción y pruebas |

No crear Pedido/DetallePedido ni sus tablas, repositorios, servicios, endpoints o pantallas en Sprint 1. State y Strategy se planifican para Sprint 2; el cambio de ubicación no elimina su exigencia académica del MVP.

## 4. Arquitectura de ejecución y despliegue

| Entorno | Frontend | Backend | Persistencia |
|---|---|---|---|
| Desarrollo local | Contenedor React; modo de servir/build por concretar | Contenedor Java 21/Spring Boot | Supabase remoto por JDBC |
| Despliegue futuro | Render Static Site, artefactos dist | Render Web Service Docker | Mismo tipo de persistencia Supabase; credenciales propias del entorno |

Compose se limita a ejecutar los servicios locales; Render se prepara con dos servicios independientes. Frontend no conecta a PostgreSQL ni reemplaza Java mediante la API REST de Supabase.

Preparación técnica prevista:

- Definir construcción backend con Maven/JDK 21 y ejecución Java 21; imágenes/versiones y estrategia multietapa por concretar.
- Construcción frontend reproducible con lockfile y npm ci; VITE_API_BASE_URL público fijado en build. Nginx para servir el contenedor local fue propuesto, no aprobado como selección técnica definitiva. Comparar con Vite de desarrollo antes de resolver.
- Excluir .env, credenciales, Git, dependencias/generados locales y context de los contextos Docker. No copiar secretos ni introducirlos en capas/argumentos de build.
- Transmitir explícitamente SUPABASE_JDBC_URL, SUPABASE_DB_USER y SUPABASE_DB_PASSWORD en ejecución; no inventar credenciales ni colocarlas en VITE_*.
- Mantener perfiles dev y supabase excluyentes. dev no activa JDBC; la prueba real debe usar supabase. ddl-auto=none, sql.init.mode=never y health HTTP se conservan.
- Mantener la preparación JDBC Session Pooler/SSL documentada; obtener host/usuario reales del panel Connect. Confirmar conectividad desde el contenedor antes de aplicar SQL.
- Preparar compatibilidad del puerto Render PORT y enlace 0.0.0.0 sin romper SERVER_PORT/local. Detalle de precedencia de variables por revisar en S1-09.
- Documentar frontend Static Site, build npm ci && npm run build, publicación dist y rutas SPA si se introduce navegación cliente. No crear servicios públicos ni activar despliegue automático.
- CORS con orígenes explícitos, CSRF activo y cookies de sesión. La topología definitiva de producción queda pendiente; no resolverla desactivando CSRF o introduciendo JWT.

La URL que usa Fetch se resuelve en el navegador: no asumir que el hostname interno de Compose «backend» sea accesible desde el navegador del usuario. El acceso local directo o proxy debe documentarse y comprobarse.

Referencias técnicas conservadas de la propuesta original (no nuevos resultados de pruebas): [Docker Compose y variables](https://docs.docker.com/compose/how-tos/environment-variables/set-environment-variables/), [Spring Boot/Supabase](https://supabase.com/docs/guides/getting-started/quickstarts/spring-boot), [Docker Render](https://render.com/docs/docker), [Static Sites](https://render.com/docs/static-sites), [Web Services](https://render.com/docs/web-services), [rewrites](https://render.com/docs/redirects-rewrites), [limitaciones del plan gratuito](https://render.com/docs/free). Revisar estas condiciones antes de una futura publicación; no se contrató plan ni se autorizó gasto.

## 5. Modelo, contratos y patrones

Modelo conceptual candidato, **no aprobado como esquema**: Usuario/rol, Cliente si su vínculo con la cuenta lo requiere, Producto, Proveedor, ProductoProveedor, representación del historial de precios y OfertaSemanal/DetalleOferta. No duplicar entidades innecesariamente. Aprobar campos, cardinalidades, restricciones y datos iniciales antes de escribir/aplicar SQL o generar entidades.

MVC: React, controladores REST y modelo/servicios separados. Repository: interfaces JPA de las entidades efectivamente aprobadas. Service Layer: reglas, validaciones y transacciones en servicios de productos, proveedores, precios y oferta. Clases concretas y UML se documentan a medida que existan, sin afirmar implementación adelantada. State y Strategy quedan fuera del incremento Sprint 1.

Contratos a definir: token CSRF y sesión (iniciar/consultar/cerrar); consulta/alta de productos y proveedores; registro/consulta de precios de proveedor; oferta vigente. Rutas exactas, DTOs, validaciones, métodos, permisos y estados HTTP se revisan en conjunto antes de integrar. No agregar rutas de pedidos ni CRUD completos sin una justificación y alcance aprobado.

La oferta precargada y precios de venta explícitos eran propuestas del plan original. Es necesario aprobar los datos/reglas de vigencia y el contrato de ausencia de oferta como parte del modelo inicial. El precio de compra no se expone al Cliente. No se crea publicación automatizada de newsletter.

## 6. Responsables y revisión cruzada

| Integrante | Trabajo asignado | Revisor principal |
|---|---|---|
| 1 | S1-01 Docker/Compose; S1-09 preparación Render | 3 para seguridad; 4 para frontend |
| 2 | S1-02 JDBC; S1-03 esquema; S1-05 productos/proveedores/precios; soporte API/datos S1-06 | 1 para JDBC; 3 para modelo/permisos |
| 3 | S1-04 autenticación, sesiones, CSRF y permisos | 2 para persistencia; 4 para integración |
| 4 | React para login, maestros y oferta; S1-06; integración de pantallas | 3 para sesión; 2 para contratos/datos |
| Los cuatro | S1-10 pruebas, revisión cruzada, evidencia y cierre | Ningún autor aprueba solo su propio trabajo |

Asignación por número pendiente de asociar a nombres. Evitar edición simultánea de modelo, SecurityConfig y contratos compartidos. Revisiones técnicas pueden comenzar ya; no se realizan commits/push ni creación de ramas remotas por esta formalización.

## 7. Decisiones pendientes y bloqueos específicos

| Pendiente | Alternativas / contenido a presentar | Trabajo condicionado |
|---|---|---|
| Modelo SQL inicial | Campos, relaciones, unicidad, rol como enum/tabla, cuenta/Cliente, precisión monetaria y esquema destino Supabase | Escritura/aplicación SQL y entidades S1-03; persistencia S1-04/05/06 |
| Historial de precios | Versionar ProductoProveedor vs relación estable con historial separado; vigencias, precio actual, corrección de registros y solapamientos | Parte de S1-03/S1-05; conservar historia ya es obligatorio |
| Oferta | Modelo y origen del precio de venta, vigencia, selección de oferta actual y datos precargados | S1-03/S1-06; no derivar venta de compra sin regla aprobada |
| Sesiones/cookies producción | Subdominios propios; URLs separadas con comprobación de cookies; proxy /api como alternativa a validar | Configuración definitiva de sesión pública S1-09; no bloquea diseño y prueba local de S1-04 |
| Contrato local de autenticación y permisos | Formato login, entrega/renovación CSRF, cookies/Fetch, duración sesión y matriz de roles | Implementación concreta S1-04 e integración React; sin ampliar Gerencia por suposición |
| Detalles Docker | Imágenes/versiones, multietapa, servidor estático Nginx vs Vite local, puerto/acceso al navegador | Partes específicas S1-01, no reapertura de la decisión Docker |
| SQL/datos y validación | Procedimiento manual versionado, destino y cuenta de aplicación, respaldo si hay datos existentes; datos demo y sus contraseñas externas | Aplicación en Supabase; no aplicar SQL destructivo por defecto |
| Validación docente | Recorte rentabilidad y reportes respecto de entregas | Conformidad académica final; no afirmarla obtenida |

Estos pendientes no invalidan el alcance aprobado. Resolverlos mediante propuestas concretas antes de la acción dependiente; no solicitar nuevamente aprobación general de Docker/Render ni del backlog. Las reglas de reservas/transiciones de pedidos se resolverán al iniciar Sprint 2, sin implementarlas ahora.

## 8. Cronograma de ejecución

| Fecha | Foco y dependencias | Entregable previsto |
|---|---|---|
| 9 octubre | Formalización documental y revisión de pendientes | Una planificación vigente y documento operativo; ninguna implementación |
| 10 octubre | Revisión de herramientas; diseño contenedores/Render; JDBC local; preparar modelo/historial/contratos | Diagnóstico inicial y propuestas concretas para aprobación |
| 11 octubre | S1-01; JDBC en contenedor al estar disponible; revisión modelo/historial | Ejecución local reproducible y evidencia de conexión; aprobación del modelo antes de SQL |
| 12–13 octubre | Aplicar S1-03 solo tras aprobación; S1-04/S1-05; React en paralelo con contratos revisados | Persistencia, seguridad y maestros en integración progresiva |
| 14 octubre | S1-06; pruebas integradas de sesión, permisos, precios y oferta | Circuito real login/gestión/consulta, sin pedidos |
| 15 octubre | S1-09; regresión S1-10; documentación y resolución de errores | Preparación Render y evidencia reproducible sin publicar |
| 16 octubre | Revisión cruzada y cierre S1-10 | Veredicto, criterios cumplidos y pendientes reales |

Las fechas son secuencia objetivo, no evidencia de trabajo ni garantía de capacidad. Si una aprobación o credencial no llega, continuar los frentes independientes y registrar el bloqueo de la tarea afectada. No trasladar un P0 silenciosamente ni declararlo aprobado sin ejecutar sus verificaciones.

### Hasta la entrega del 31 de octubre de 2026

| Período | Plan posterior, sujeto a autorización de cada etapa | Resultado objetivo |
|---|---|---|
| 10–16 octubre | Sprint 1, ocho P0 aprobados | Docker/JDBC/login/maestros/precios/oferta y preparación Render |
| 17–23 octubre | Sprint 2: S1-07/S1-08 trasladadas; compras/Strategy, autorización Quinteros, recepción, reservas/movimientos y ampliación State | Pedidos y circuito de abastecimiento/inventario/entrega |
| 24–28 octubre | Sprint 3: facturación interna, pagos, reportes solo ventas/compras, integración y UML | Procesos principales y primera grabación |
| 29–30 octubre | Corrección/ensayo/grabación; sin nuevas funciones | Walkthrough de cinco minutos y defensa |
| 31 octubre | Verificación y entrega académica | Evidencias y artefactos preparados por el equipo |

Sprint 2/3 son planificación indicativa y no autorización de implementar ahora. Publicar requiere instrucción expresa adicional, incluso si Render ya está preparado. Demo local queda como respaldo; no añadir infraestructura para eludir límites de planes gratuitos. La ruta crítica es modelo/historial aprobados → SQL → sesión/maestros → oferta → pruebas; pedidos ya no forman parte de la ruta crítica del Sprint 1.

## 9. Riesgos, evidencias y documentación

Riesgos: aprendizaje Docker, credenciales/conectividad, demoras en aprobar modelo/historial, build completo no reproducido en auditoría, cookies de producción, disponibilidad del equipo y validación docente. Registrar horas disponibles al inicio; no se fijó una capacidad horaria garantizada. No recortar los ocho P0 sin instrucción expresa.

El DoD y los casos de prueba están en [SPRINT_1.md](SPRINT_1.md). Evidencia requerida: comandos/resultados reales, Java 21, builds limpios, tests anteriores y nuevos, SELECT 1 del DataSource Spring local/contenedor, Compose y circuito React, permisos/CSRF/precios históricos y ausencia de secretos. No mezclar evidencia reportada con prueba repetida ni usar health como acreditación JDBC.

Documentación prevista durante futura implementación: comandos locales/Compose, variables/perfiles, contratos API, UML de clases/capas efectivamente implementadas, guía de preparación Render y procedimiento de publicación/reversión sin ejecutarlo. Actualizar este archivo solo si cambia el compromiso; el documento operativo registra progreso, pruebas y cierre.

## 10. Trazabilidad de la propuesta original

El archivo fue creado originalmente con la propuesta íntegra de la sesión. Esta revisión sustituye su contenido operativo dentro del mismo archivo y conserva la evolución por la siguiente matriz; no se crea una segunda planificación ni se mantiene una propuesta anterior como estado vigente.

SHA-256 del contenido anterior a esta formalización: b38254349b7cfc50c069bae6b62ae318ca3099514c177842ef21be91caee69db.

| Tema de la propuesta inicial | Resolución explícita vigente |
|---|---|
| Docker y Render como nuevas propuestas | Aprobados para desarrollo local/preparación; sustitución de prohibición Docker registrada |
| Compromiso mínimo S1-01 a S1-06, S1-09/10 | Ocho tareas P0 obligatorias, sin condicionar su compromiso a capacidad |
| S1-07/S1-08 P1, objetivo ampliado de Sprint 1 | Trasladadas a Sprint 2; IDs conservados; no implementar aunque quede tiempo |
| Circuito incluía creación/confirmación de pedidos | Incremento vigente termina en gestión de maestros/precios y consulta de oferta |
| Integrante 3 combinaba login, pedidos y State | Sprint 1: solo autenticación/sesiones/CSRF/permisos; pedidos y State posteriores |
| State inicial en Sprint 1 | State y Strategy se implementan Sprint 2; MVC/Repository/Service Layer en incremento Sprint 1 |
| Publicación efectiva candidata P2 | Fuera de autorización; solo preparar Render |
| Nginx, imágenes multietapa, detalle de cookies, historial y modelo | Propuestas técnicas conservadas como alternativas pendientes, no aprobadas automáticamente |
| Reservas al preparar y confirmación sin reserva propuestas | Regla operativa pendiente para Sprint 2; sin efectos de stock en Sprint 1 |
| Riesgos/capacidad/validación docente | Conservados; capacidad se registra al inicio, no reduce automáticamente el P0 |
| Calendario al 31 de octubre | Sprint 1 fijo; posteriores indicativos e incorporan tareas trasladadas |

## 11. Primeros pasos sin bloqueos documentales

Después de esta formalización no hace falta otra actualización general del alcance para iniciar el sprint. En la siguiente etapa autorizada pueden comenzar revisión de Docker/Compose/JDK/Node/Maven, recuperación de builds limpios, diagnóstico JDBC local con credenciales propias, diseño de contenedores/Render, preparación del modelo/historial y contratos para aprobación, y diseño React de estados de carga/error.

Aplicar SQL/crear entidades espera modelo aprobado; el historial espera decisión de representación; producción espera topología de cookies y autorización de publicación. En esta ejecución solo se formalizan documentos. No se implementa ni se publica por el hecho de registrar el compromiso.
