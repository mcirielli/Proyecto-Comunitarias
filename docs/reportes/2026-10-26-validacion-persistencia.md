# Validación de persistencia de maestros

Estado: ventana cerrada; integración BLOQUEADA, Sprint 1 abierto.
Fecha: 26/10/2026, America/Buenos_Aires.

## Revisión previa y alcance

Leídos AGENTS.md, SUPERPROMPT, las dos entregas académicas completas (extracción
de texto PDF), ULTIMO y reporte enlazado, DECISIONES_ARQUITECTURA, SPRINT_1,
API_MAESTROS y ESTRUCTURA_SUPABASE_COMPARTIDA. Inspeccionados configuración,
entidades, servicio, DTOs, repositorios, seguridad y tests. No fue necesario
ampliar el alcance con material docente adicional.

Contradicciones: login aprobado reemplaza selector del superprompt; recorte de
rentabilidad sigue pendiente de validación docente; historial exigido por las
entregas no está cubierto por precio actual de producto_proveedor. El tablero
histórico de SPRINT_1 no refleja por sí solo el código local parcial de maestros.
Ninguna de estas observaciones autoriza cerrar tareas ni ampliar este incremento.

Había cambios locales: SecurityConfig modificado, maestros/tests/documentación
sin seguimiento y numerosas copias Andres-PC. Se preservaron; el diff de seguridad
preexistente sigue siendo la restricción ROLE_OPERACIONES. No commit/push/DDL.

## Configuración y bloqueo observado

SUPABASE_JDBC_URL, SUPABASE_DB_USER y SUPABASE_DB_PASSWORD: las tres ausentes en
el entorno del proceso. Solo se consultó disponibilidad; no se buscaron valores
en archivos ajenos ni se imprimieron credenciales. Spring no carga .env por sí solo.
La integración fija exclusivamente supabase; dev continúa sin DataSource.
Se mantienen ddl-auto=none, sql.init.mode=never y open-in-view=false.
No se modificó configuración JDBC de producción ni mappings sin evidencia real.

Para desbloquear: proporcionar las tres variables al proceso PowerShell/IDE que
lanza Maven, usando conexión JDBC PostgreSQL de Supabase (host/puerto/base y SSL
según su configuración), usuario con permisos de lectura/escritura y secuencias,
red accesible y esquema/search_path correcto. No alcanza configurar solo IntelliJ
Database ni usar una URL REST/API key. No compartir sus valores en el reporte.

## Cambios de esta etapa

- SupabaseTestEnvironment.java: falla antes de crear DataSource ante variables
  ausentes o perfiles adicionales; mensaje con nombre de variable, sin su valor.
- SupabaseJdbcIT.java: conserva SELECT 1 mediante DataSource de Spring; agrega
  inspección de tablas efectivamente resueltas por to_regclass/search_path,
  columnas, tipos con longitud, nulabilidad, identidad y restricciones PostgreSQL.
  Verifica seis columnas por tabla, PK id y UNIQUE cuit de proveedores; la salida
  requiere revisión contra mappings antes de afirmar compatibilidad completa.
- MaestrosSupabaseIT.java: alta/consulta producto y proveedor desde servicio real,
  flush y clear de EntityManager, más rechazo de CUIT duplicado por integridad.
  Datos ficticios IT_COMUNITARIAS, email example.invalid y CUIT de prueba no fiscal.
  Transacciones de test con rollback incluso ante excepción; no UPDATE/DELETE
  sobre registros existentes. Las secuencias PostgreSQL pueden avanzar aunque
  haya rollback: se admiten huecos de identidad, no quedan filas de prueba.
  Falta confirmar longitudes y restricciones adicionales con esquema accesible.
- ApiExceptionHandlerTest.java: dos pruebas verifican respuestas 400/409 genéricas
  sin propagar mensajes internos. Configuración existente excluye mensaje y stack
  de respuestas de error de Boot. No se verificó por HTTP un fallo real de BD;
  esto tampoco acredita sanitización de todos los logs operativos posibles.
- docs/LOGIN_PROPUESTA.md: contrato concreto, matriz mínima, sesión/CSRF/cookies,
  ALTER TABLE propuesto dentro del documento y archivos React futuros. No aplicado.
- docs/reportes/ULTIMO.md: enlaza este reporte.

No dependencias Maven/npm nuevas. Para leer PDFs se instaló pypdf 6.21.2 como
herramienta temporal en tmp/pdf-reader, fuera de las dependencias de aplicación.

## Comandos y resultados reales

Java Temurin 21.0.12.1. Maven localizado en IntelliJ:
`C:/Program Files/JetBrains/IntelliJ IDEA 2026.2.0.1/plugins/maven-plugin/lib/maven3/bin/mvn.cmd`.
Se fijó JAVA_HOME a `C:/Users/Noxie-PC/.jdks/temurin-21.0.12.1`.
Usar ese ejecutable como `mvn` en los comandos siguientes:

```powershell
mvn -o -Dmaven.repo.local=C:/Users/Noxie-PC/.m2/repository -f backend/pom.xml test
mvn -o -Dmaven.repo.local=C:/Users/Noxie-PC/.m2/repository -f backend/pom.xml '-Dtest=SupabaseJdbcIT,MaestrosSupabaseIT' test
```

Primera ejecución dentro de sandbox falló intentando repositorio C:/.m2/repository.
Se repitió fuera del sandbox con autorización y ruta de caché explícita; loopback
necesario para InfrastructureTest. Instalación temporal PDF también requirió
autorización de red; el primer intento fue bloqueado por sandbox.

Suite original: 13 aprobadas. Suite final: **15 pruebas, 0 fallos, 0 errores,
0 omitidas, BUILD SUCCESS** (infraestructura 6, servicio 7, errores 2).
Compilan los nuevos tests de integración aunque no pertenecen a selección estándar.

Selección explícita de integración: **5 pruebas, 0 fallos de aserción, 5 errores
de contexto, 0 omitidas, BUILD FAILURE**; causa: falta SUPABASE_JDBC_URL, primera
variable requerida por el inicializador. Las otras dos también están ausentes.
No se ejecutó SELECT 1 ni consultas de metadatos/altas. No hay evidencia de
conectividad, compatibilidad real JPA, rechazo real de duplicado ni rollback real.
Logs locales en tmp/pruebas-locales.log y tmp/integracion-supabase.log, no entrega
versionada. El comando de integración ejecutado redirigía salida y filtraba resumen:
su shell devolvió 0 por el filtro final, pero Maven reportó BUILD FAILURE.

git diff --check sin errores de whitespace (avisos LF/CRLF). Durante la sesión
apareció también modificación local en compose.yaml, ajena a este trabajo y
preservada. Se eliminó únicamente tmp/pdf-reader tras verificar su ruta absoluta;
los logs de pruebas permanecen en tmp.

## Pendientes y siguiente paso

1. Configurar variables en proceso local y repetir selección explícita. Revisar
   metadatos contra mappings, incluyendo índices únicos independientes, políticas
   RLS/permisos, triggers y restricciones extra si aparecen problemas. La referencia
   compartida no sustituye este contraste. Corregir solo con evidencia, sin DDL automático.
2. Repetir verificación desde contenedor según S1-02; todavía no ejecutada.
3. Revisar contrato LOGIN_PROPUESTA y esquema real de usuarios; aprobar cambio de
   hash antes de aplicarlo. Provisionar cuentas por procedimiento seguro.
4. Autorizar e implementar login, después React: login → alta → consulta persistida.
   Probar HTTP con sesión real, roles y CSRF. Hoy no existe login funcional.

S1-02/03/05 no terminadas; S1-04/06 siguen pendientes. Historial, revisión cruzada,
cierre de Sprint 1 y demás requisitos académicos no se declaran satisfechos.
