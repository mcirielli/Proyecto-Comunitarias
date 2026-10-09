# Sprint 0 — infraestructura inicial

Registro: 9 de octubre de 2026. Alcance exclusivamente S0-01 a S0-07. **Implementación preparada; validación ejecutable pendiente por restricciones del entorno. Sprint 0 no se declara cerrado.**

## Estado de tareas

| Tarea | Estado |
|---|---|
| S0-01 | Estructura completada; fuentes académicas y planificación original conservadas |
| S0-02 | Maven/Java 21, Spring Boot 3.5.16, paquetes, CORS, perfiles y health creados; compilación pendiente |
| S0-03 | React/Vite/TypeScript/Tailwind y Fetch real preparados; instalación, build y prueba en navegador pendientes |
| S0-04 | JDBC Supabase y ejemplos configurados; sin esquema ni credenciales; conexión pendiente |
| S0-05 | Health público, otras rutas protegidas, BCrypt preparado y CSRF activo; sin login/usuarios |
| S0-06 | .gitignore comprobado; raíz sin repositorio Git inicializado; sin commit/push/ramas remotas |
| S0-07 | README, arquitectura, decisiones y este estado creados |

## Verificaciones realmente ejecutadas

| Verificación | Resultado |
|---|---|
| Herramientas | Node 24.21.0, npm 11.19.0; JDK 23 y 26 localizados; Java 21 no localizado; Maven fuera de PATH, encontrado en IntelliJ |
| Maven verify | Intentado con Maven incorporado y JDK 23, compilación configurada para release 21. Falló antes de compilar: Maven Central bloqueado (Permission denied: getsockopt), parent POM no resoluble |
| Pruebas backend | Seis pruebas JUnit 5 creadas. No ejecutadas: verify no llegó a la fase test |
| npm install | Falló por acceso a registry.npmjs.org (EACCES). No se completó instalación ni se generó package-lock.json |
| npm run build | Ejecutado; falló porque tsc no está instalado |
| npm run dev | Ejecutado; falló porque vite no está instalado |
| GET local /api/health | Intentado; ECONNREFUSED, backend no activo. No se verificó una respuesta HTTP correcta |
| Frontend local | Consulta a puerto 5173: ECONNREFUSED. Consumo React→API no verificado en ejecución |
| Formato de archivos | XML del POM y JSON frontend parseados correctamente; no equivale a compilación |
| .gitignore | git check-ignore en repositorio temporal dentro de .cache: ignora .env, node_modules, target, dist, IntelliJ, cachés y clave privada de ejemplo; .env.example conservados |
| Secretos | Revisión de archivos creados sin secretos reales detectados; variables Supabase ausentes y sin .env real creado. No hay repositorio raíz para auditar archivos versionados/historial |
| Fuentes originales | SHA-256 de ambas entregas PDF, superprompt, context/README y planificación coincide con el estado previo; ningún PDF/PPTX fue editado |

Las pruebas preparadas verifican health público y JSON, protección de ruta futura, CORS local y preflight, rechazo de otro origen y BCrypt. Se ejecutan con perfil dev y servidor HTTP real en puerto aleatorio, sin Supabase.

## Dependencias

Backend declaradas: starters Web, Data JPA, Security, Validation y Test (JUnit 5), driver PostgreSQL. Frontend declaradas: React/React DOM 19.3, Vite 7, TypeScript 5.9, Tailwind 4, plugins React/Tailwind y tipos. No se afirma instalación exitosa; falta resolver dependencias y conservar el lockfile resultante.

## Pendientes para cerrar Sprint 0

1. Seleccionar/instalar JDK 21 y configurar Maven; habilitar acceso a Maven Central y npm desde el entorno de desarrollo.
2. Ejecutar mvn verify con Java 21 y corregir cualquier fallo real que aparezca; ejecutar npm install y npm run build, conservar package-lock.json.
3. Arrancar ambos proyectos y verificar en navegador disponibilidad, rechazo de rutas protegidas y comportamiento al detener la API.
4. Configurar credenciales propias de Supabase, iniciar solo perfil supabase y verificar JDBC/SELECT 1; no inferir conexión desde health.
5. Validar con el docente el recorte de rentabilidad y reportes.

## Próximas tareas sugeridas para cuatro integrantes

- Integrante 1: verificar backend en JDK 21 y las seis pruebas.
- Integrante 2: verificar build y consumo real React/API; revisar accesibilidad y estados de error.
- Integrante 3: configurar y comprobar Supabase sin crear esquema de negocio.
- Integrante 4: revisar decisiones/documentación, exclusiones Git y validación docente del recorte.

Una vez cerradas estas verificaciones, presentar el siguiente sprint para autorización. No se inició Sprint 1 ni se implementaron entidades, repositorios de negocio, servicios funcionales, Strategy, State, compras, inventario, facturación, reportes o autenticación completa. Logs/cachés de verificaciones permanecen en .cache y están excluidos de Git.
