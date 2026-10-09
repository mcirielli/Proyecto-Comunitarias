# Sprint 0 — cierre formal

Fecha: 9 de octubre de 2026. Alcance exclusivamente infraestructura S0-01 a S0-07.

**APROBADO CON OBSERVACIONES. Sprint 0 cerrado. Bloqueantes reales identificados: ninguno.** Se aceptan las evidencias reportadas por el usuario y se distinguen de las repetidas en auditoría. No se inicia Sprint 1.

## Criterios y estado final

| Tarea | Estado | Evidencia / límite |
|---|---|---|
| S0-01 Estructura | Completada | Carpetas y archivos requeridos presentes; fuentes académicas conservadas |
| S0-02 Backend | Completada con observación | Java 21, Spring Boot 3.5.16, Maven, perfiles, paquetes y health. BUILD SUCCESS reportado; seis tests repetidos con éxito; verify completo no repetido exitosamente |
| S0-03 Frontend | Completada con observación | React/Vite/TypeScript/Tailwind, Fetch real y lockfile versionado. Build estándar y «API disponible» reportados; TypeScript y build alternativo comprobados |
| S0-04 PostgreSQL | Completada para preparación | JDBC y variables configurados; IntelliJ/SELECT 1 exitosos reportados. Prueba del datasource Spring pendiente |
| S0-05 Seguridad inicial | Completada | Health público, otras rutas protegidas, CORS y BCrypt comprobados; CSRF activo; roles documentados |
| S0-06 Versionado | Completada | .gitignore comprobado, Git local/remoto presentes, referencias main/origin/main coinciden; primer push y privacidad reportados |
| S0-07 Documentación | Completada | Arquitectura, decisiones, README y registros de estado/cierre disponibles |

## Resultados comprobados en auditoría

- Maven surefire:test offline mediante launcher IntelliJ y JDK Temurin 21.0.12.1: BUILD SUCCESS, 6 tests, 0 fallos, 0 errores, 0 omitidos. Usa clases compiladas existentes; no sustituye verify completo.
- GET localhost:8080/api/health: 200 y JSON application=Comunitarias/status=UP. GET /api/pedidos sin autenticar: 401. No hay controlador comercial de pedidos.
- CORS: origen localhost:5173 aceptado en GET/OPTIONS; origen externo rechazado con 403. BCrypt probado por JUnit.
- npm run build llegó a Vite sin errores TypeScript, pero falló al cargar configuración por acceso denegado. node node_modules/vite/bin/vite.js build --configLoader runner pasó (código 0, 31 módulos). No se cambió configuración.
- HTTP frontend localhost:5173: 200 y raíz React. Fetch real revisado en código; pantalla «API disponible» es evidencia del usuario, sin nueva observación visual del auditor.
- Git: remoto mcirielli/Proyecto-Comunitarias, main...origin/main=0/0 según referencias locales; exclusiones correctas. No se encontraron rutas sensibles versionadas ni candidatos en revisión de patrones. No se auditó exhaustivamente historial ni se consultó GitHub.

## Evidencias reportadas por el usuario

Maven BUILD SUCCESS; seis tests sin fallos/errores; npm run build exitoso; health 200/UP; React muestra «API disponible»; IntelliJ conectado a Supabase y SELECT 1 exitoso; /api/pedidos devuelve 401; primer push exitoso a repositorio privado y main sincronizada. El auditor no repitió SELECT 1, verificación visual ni consulta remota GitHub. Las comprobaciones directas anteriores corroboran parte de esas evidencias.

## Errores y limitaciones conservados

La repetición de verify falló primero por repositorio local C:\.m2\repository no accesible. Reintentada con caché del usuario llegó a javac release 21, pero falló con «Cannot close compiler resources» y referencia a spring-boot-starter-tomcat-3.5.16.jar. Causa exacta sin resolver; no se presenta como verify exitoso. El entorno restringido también impidió la carga estándar de vite.config.ts; el loader alternativo sí funcionó. Consola estándar falló con helper_unknown_error; se utilizaron procesos Node.

Git inicialmente rechazó dubious ownership por usuario sandbox; se usó safe.directory por comando, sin configuración global. Surefire emitió avisos Mockito/Byte Buddy por agente dinámico, class sharing y demora SecureRandom, sin fallos. El detalle de comandos, resultados y el registro original se conservan en [REPORTE_SPRINT_0.md](REPORTE_SPRINT_0.md).

## Configuración aceptada

dev es perfil por defecto y excluye DataSource/JPA; supabase requiere las tres variables SUPABASE_JDBC_URL/DB_USER/DB_PASSWORD. No usar ambos perfiles juntos. Las variables no existen en el proceso del auditor; la conexión IntelliJ no acredita la conexión de Spring. ddl-auto=none y sql.init.mode=never: sin esquema automático. Health solo comprueba HTTP.

Spring no carga .env automáticamente; usar variables del proceso/IntelliJ. VITE_API_BASE_URL es público y no debe contener secretos. CORS con orígenes locales explícitos; CSRF activo, BCrypt preparado; login/formLogin/HTTP Basic/logout no implementados como flujo funcional. CLIENTE, OPERACIONES y GERENCIA quedan documentados, sin permisos por rol todavía.

## Pendientes trasladados y próximos pasos

1. Conservar logs reproducibles de mvn verify y npm run build en el entorno normal con Java 21; investigar los errores de recursos si también aparecen allí.
2. Verificar JDBC desde Spring con solo perfil supabase antes del trabajo de persistencia. Sin crear esquema ni credenciales versionadas.
3. Registrar evidencia visual y comprobar error/reintento de React al detener API.
4. Obtener validación docente del recorte de rentabilidad/reportes, aún pendiente.
5. Presentar alcance y backlog del Sprint 1 para aprobación: integrante 1 backend/contratos/pruebas; 2 frontend/integración; 3 persistencia y modelo propuesto; 4 documentación, revisión académica y coordinación.

No son faltantes de aceptación del Sprint 0 el login real, entidades, migraciones, módulos de negocio, Strategy, State ni pantallas operativas. Permanecen para etapas posteriores autorizadas. La preparación JDBC permitía dejar su verificación real pendiente según la autorización inicial. Las observaciones no justifican implementar módulos comerciales ni modificar decisiones aprobadas.

## Cambios de cierre

Solo se actualizan este documento y REPORTE_SPRINT_0.md. Código/configuraciones/decisiones y documentos académicos no modificados; sin commits/push. Las pruebas generan artefactos locales ignorados. El reporte conserva íntegramente el registro anterior como histórico; sus estados «pendiente/no cerrado» fueron sustituidos por este cierre, sin ocultar los errores de la ejecución inicial.
