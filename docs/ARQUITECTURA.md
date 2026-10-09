# Arquitectura del Sprint 0

React (presentación) → REST/DTO → controladores Spring → servicios → dominio → repositorios JPA → PostgreSQL en Supabase. En este sprint solo está implementada la ruta técnica React → GET /api/health. Los paquetes service, domain, repository y mapper reservan responsabilidades sin clases de negocio.

| Capa | Responsabilidad futura | Estado actual |
|---|---|---|
| Frontend | Interacción y consumo HTTP | Inicio y estado real de API |
| Controller/DTO | Contratos y delegación | HealthController y HealthResponse |
| Service | Casos de uso y transacciones | Paquete reservado |
| Domain | Reglas y entidades | Paquete reservado |
| Repository | Persistencia JPA | Dependencia y paquete, sin repositorios |
| Config | Perfiles, seguridad y CORS | Implementado |
| PostgreSQL | Persistencia e integridad | Configuración preparada, conexión pendiente |

Dependencias backend: Web (REST/Tomcat/JSON), Data JPA (persistencia futura), Security (protección y BCrypt), Validation (validación futura), PostgreSQL Driver (JDBC), Test (JUnit 5 e infraestructura de pruebas). Sin Lombok, H2, JWT, OAuth ni servicios distribuidos.

Frontend: React/React DOM; Vite y plugin React; TypeScript y tipos; Tailwind CSS y plugin Vite. Fetch nativo; sin librería adicional de HTTP.

El perfil dev excluye autoconfiguración de base de datos y repositorios para permitir arrancar sin credenciales. El perfil supabase activa JDBC/JPA; ddl-auto=none y spring.sql.init.mode=never evitan crear o modificar tablas. No activar dev junto a supabase.

Seguridad: GET /api/health público, demás rutas requieren autenticación; form login, HTTP Basic y logout desactivados. La futura autenticación por sesión y los permisos por rol se implementarán en otra etapa. No hay usuarios ni endpoints de sesión. CORS admite solicitudes del frontend local, sin comodines; CSRF sigue activo.

Referencias técnicas: [Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Tailwind con Vite](https://tailwindcss.com/docs/installation/using-vite), [Supabase con Spring Boot](https://supabase.com/docs/guides/getting-started/quickstarts/spring-boot). Ver DECISIONES_ARQUITECTURA.md para el alcance aprobado y README raíz para ejecución.
