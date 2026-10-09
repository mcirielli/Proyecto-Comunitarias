# Decisiones de arquitectura

Autorización del Sprint 0; registro del 9 de octubre de 2026. Las entregas académicas son la referencia funcional principal. Las siguientes decisiones fueron aprobadas expresamente por el usuario y prevalecen sobre propuestas previas. Su registro no implica implementación funcional en Sprint 0.

| Decisión aprobada | Alcance |
|---|---|
| Login real simplificado | Spring Security, usuarios precargados y roles Cliente, Operaciones y Gerencia; futuras autoridades ROLE_CLIENTE, ROLE_OPERACIONES y ROLE_GERENCIA. Sin JWT ni OAuth. |
| Inventario | Reservas automáticas y movimientos de ingreso, egreso y ajuste. |
| Precios | Conservar historial de precios de proveedores. |
| Compra Quinteros | Autorización manual de Gerencia. |
| Administración | Facturación interna simplificada y registro de pagos. |
| Reportes | Solo totales de ventas y compras; sin cálculo de rentabilidad. |

**Recorte académico pendiente de validación docente:** excluir rentabilidad reduce el alcance de la Actividad 2 (Gestión y Reportería, módulos gerenciales) y del superprompt. Limitar reportes a ventas y compras también reduce los reportes allí descritos. No se presenta este recorte como conformidad docente ya obtenida.

La planificación original en PLANIFICACION_MVP.md permanece como propuesta histórica: sus referencias a margen/rentabilidad y reportes ampliados quedan sustituidas por este registro. La decisión de login real sustituye el selector de perfiles sugerido por el superprompt.

## Configuración del Sprint 0

- Monolito por capas; backend Java 21, Spring Boot 3.5.16 y Maven. Esta versión estable permite conservar JUnit 5 gestionado por spring-boot-starter-test.
- React, Vite, TypeScript y Tailwind mediante su plugin oficial de Vite. Las versiones declaradas se concretan en el lockfile cuando la instalación se complete.
- Perfil dev por defecto: HTTP sin DataSource/JPA. Perfil supabase: JDBC real con variables obligatorias, sin crear esquema. Usar solo uno a la vez.
- GET /api/health indica disponibilidad de la API, no estado de PostgreSQL.
- CORS limitado a los orígenes locales configurados. CSRF activo; sin mecanismos de autenticación funcionales ni usuario aleatorio de Spring Boot. BCrypt disponible como bean.

No se aprueban por este registro reglas adicionales de negocio, algoritmos, fórmulas ni el esquema definitivo. Strategy, State y las entidades permanecen fuera de Sprint 0.
