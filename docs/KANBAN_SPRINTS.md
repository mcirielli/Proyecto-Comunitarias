# Kanban por sprints — Comunitarias

Fuente operativa central. Sprint 1 conserva su [planificación formal](PLANIFICACION_SPRINT_1.md) y [criterios/DoD](SPRINT_1.md); este tablero resume sus estados y no reemplaza ni duplica su planificación. Última actualización documental: sesión actual. Los resultados de ejecución son reportados por el usuario salvo indicación explícita.

## Lectura y evidencia

Estados: Por hacer, En progreso, En revisión, Terminado y Bloqueado. S1-01 usa En revisión en esta vista; corresponde a «En revisión final» del documento operativo. Cada ID representa una sola tarea. Un documento de diseño acredita planificación, no funcionalidad ni pruebas. Commit/PR por tarea: completar al ejecutarse; no se inventan enlaces. Números de integrantes no se asocian automáticamente a nombres.

## Sprint 0 — cerrado

P0 en esta vista es clasificación retrospectiva de infraestructura, no una prioridad atribuida a la autorización original.

**APROBADO CON OBSERVACIONES**, 9/10/2026, sin bloqueantes de su alcance. Ver [cierre formal](SPRINT_0.md) y [reporte](REPORTE_SPRINT_0.md). Backend/frontend/security/estructura y preparación JDBC implementados. Builds estándar, UI, IntelliJ SELECT 1 y push reportados; seis tests repetidos sobre clases existentes y comprobaciones HTTP/CORS observadas en auditoría. Fallos nativos de verify/Vite se conservaron; JDBC desde Spring sigue pendiente. Login y módulos funcionales no eran requisito de S0.

| ID | Sprint | Historia/tarea | Prioridad | Responsable | Revisor | Estado | Dependencias | Criterios de aceptación | Evidencia | Observaciones |
|---|---|---|---|---|---|---|---|---|---|---|
| S0-01 | 0 | Estructura | P0 retrospectiva | Equipo; asignación individual no acreditada | Auditoría de cierre | Terminado | Autorización Sprint 0 | Criterios originales de infraestructura, sin negocio | SPRINT_0.md y REPORTE_SPRINT_0.md | Observaciones conservadas en cierre; terminado con alcance S0 |
| S0-02 | 0 | Backend Java/Spring/Maven | P0 retrospectiva | Equipo; asignación individual no acreditada | Auditoría de cierre | Terminado | Autorización Sprint 0 | Criterios originales de infraestructura, sin negocio | SPRINT_0.md y REPORTE_SPRINT_0.md | Observaciones conservadas en cierre; terminado con alcance S0 |
| S0-03 | 0 | Frontend React/health | P0 retrospectiva | Equipo; asignación individual no acreditada | Auditoría de cierre | Terminado | Autorización Sprint 0 | Criterios originales de infraestructura, sin negocio | SPRINT_0.md y REPORTE_SPRINT_0.md | Observaciones conservadas en cierre; terminado con alcance S0 |
| S0-04 | 0 | Preparación PostgreSQL | P0 retrospectiva | Equipo; asignación individual no acreditada | Auditoría de cierre | Terminado | Autorización Sprint 0 | Criterios originales de infraestructura, sin negocio | SPRINT_0.md y REPORTE_SPRINT_0.md | Observaciones conservadas en cierre; terminado con alcance S0 |
| S0-05 | 0 | Seguridad inicial | P0 retrospectiva | Equipo; asignación individual no acreditada | Auditoría de cierre | Terminado | Autorización Sprint 0 | Criterios originales de infraestructura, sin negocio | SPRINT_0.md y REPORTE_SPRINT_0.md | Observaciones conservadas en cierre; terminado con alcance S0 |
| S0-06 | 0 | Control de versiones | P0 retrospectiva | Equipo; asignación individual no acreditada | Auditoría de cierre | Terminado | Autorización Sprint 0 | Criterios originales de infraestructura, sin negocio | SPRINT_0.md y REPORTE_SPRINT_0.md | Observaciones conservadas en cierre; terminado con alcance S0 |
| S0-07 | 0 | Documentación | P0 retrospectiva | Equipo; asignación individual no acreditada | Auditoría de cierre | Terminado | Autorización Sprint 0 | Criterios originales de infraestructura, sin negocio | SPRINT_0.md y REPORTE_SPRINT_0.md | Observaciones conservadas en cierre; terminado con alcance S0 |

## Sprint 1 — 10–16/10/2026, alcance aprobado

Mantiene autores, apoyos y revisores vigentes. S1-01 tiene funcionalidad y revisión 3/4 aprobadas; falta inspección de contenido de capas de las dos imágenes entregadas. Auditar todas las imágenes antiguas del equipo no es requisito. El despliegue Render fue realizado según el usuario, con verificación funcional y configuración pendientes: S1-09 no se cierra por ese reporte.

| ID | Sprint | Historia/tarea | Prioridad | Responsable | Revisor | Estado | Dependencias | Criterios de aceptación | Evidencia | Observaciones |
|---|---|---|---|---|---|---|---|---|---|---|
| S1-01 | 1 | Docker Compose frontend/backend | P0 | 1; apoyo 4 | 3 y 4: aprobaron | En revisión | Sin dependencia funcional | Build limpio; 2 servicios healthy; API/SPA/UI reales; secretos fuera de capas | SPRINT_1.md §9–12; pruebas/revisión reportadas | Revisión final: falta contenido de capas de imágenes entregadas |
| S1-02 | 1 | JDBC real desde Spring y contenedor | P0 | 2; apoyo 1 | 1 | Por hacer | S1-01 runtime; credenciales propias | SELECT 1 desde DataSource local/contenedor; fallo controlado | SPRINT_1.md: criterios; sin prueba Spring | IntelliJ no sustituye JDBC Spring |
| S1-03 | 1 | Modelo aprobado y esquema SQL inicial | P0 | 2; apoyo 3/4 | 3; modelo también 4 | Por hacer | S1-02; aprobación modelo/historial | PK/FK/restricciones; seed ficticio; lectura/escritura; ddl-auto=none | Plan formal; database/README.md, sin SQL | Comenzar diseño; aplicar SQL/entidades solo tras aprobación |
| S1-04 | 1 | Login, sesión, BCrypt, CSRF y roles | P0 | 3; apoyo 4 | 2 y 4 | Por hacer | S1-03; contrato sesión/permisos | Login/logout reales; BCrypt; CSRF; 401/403 y aislamiento | Solo SecurityConfig inicial; sin login | Comenzar contrato; cookies públicas pendientes |
| S1-05 | 1 | Productos, proveedores e historial | P0 | 2; apoyo 4 | 3 | Por hacer | S1-03/04; historial aprobado | ID estable; historia conservada; DTO/Service/Repository; BigDecimal | Sin código funcional | No agregar CRUD ampliado por defecto |
| S1-06 | 1 | Oferta persistida desde React | P0 | 4; apoyo 2/3 | 2 y 3 | Por hacer | S1-03/04/05; oferta aprobada | Cliente consulta venta; compra no expuesta; carga/vacío/error/sesión | Solo HomePage técnica | Comenzar diseño; no pedidos |
| S1-09 | 1 | Preparación y validación Render | P0 | 1; apoyo 3/4 | 4 | Por hacer | S1-01/02; configuración y cookies | Documentar/verificar backend Docker y frontend estático; PORT/perfil/CORS/JDBC; checklist | Despliegue reportado por usuario; datos técnicos pendientes | No equivale a task completa; no publicar ni activar deploy aquí |
| S1-10 | 1 | Integración, pruebas y cierre | P0 | Los cuatro | Revisión cruzada | Por hacer | Ocho P0 y su DoD | Login→maestros/precios→oferta; builds/tests/JDBC; sin secretos; cierre honesto | S1-01 evidencia parcial; S0 cerrado | No declarar sprint cerrado por Docker |

## Sprint 2 — 17–23/10/2026, ventana propuesta

Alcance y asignaciones pendientes de aprobación; no autoriza implementar. P0 significa prioridad propuesta para cerrar el circuito académico, no nuevo compromiso aprobado. Procesos de compra solo se adelantan si pedidos/stock y capacidad lo permiten.

| ID | Sprint | Historia/tarea | Prioridad | Responsable | Revisor | Estado | Dependencias | Criterios de aceptación | Evidencia | Observaciones |
|---|---|---|---|---|---|---|---|---|---|---|
| S2-01 | 2 | Modelo/contratos de pedidos y stock | P0 | Por asignar | Por asignar | Por hacer | S1-03/04/05/06 | Aprobar campos/transiciones/reservas y criterios de concurrencia | Propuesta documental | 8–12 h-persona |
| S1-07 | 2 | Crear y consultar pedidos | P0 | Por asignar | Por asignar | Por hacer | S2-01; S1-03/04/05/06 | Precio del servidor e histórico; detalles válidos; transacción; Cliente aislado | Traslado formal, ID conservado | 16–24 h-persona; no duplicar con otro ID |
| S1-08 | 2 | Confirmación y seguimiento con State | P0 | Por asignar | Por asignar | Por hacer | S1-07; S2-01 | Contexto Pedido, interfaz PedidoState y clases; transición válida persiste; inválida rechazada; reconstrucción | Traslado formal; State aún sin código | 16–24 h-persona; secuencia/reglas requieren aprobación |
| S2-02 | 2 | Base de stock y reservas | P0 | Por asignar | Por asignar | Por hacer | S2-01; productos persistidos | Físico/reservado/disponible; reserva automática transaccional; pruebas sin doble compromiso | Decisión stock aprobada; reglas concretas pendientes | 16–24 h-persona; coordinar State |
| S2-03 | 2 | Compra por demanda y base Strategy | P0 | Por asignar | Por asignar | Por hacer | S1-07; S2-02; proveedores/precios | Consolidar demanda y faltantes con regla aprobada; CompraStrategy y CompraPorDemandaStrategy; orden trazable | E2 §2.1/5.4; diseño, sin código | 20–32 h-persona; capacidad condiciona; segunda Strategy S3-02 |
| S2-05 | 2 | Oferta/newsletter y clientes mínimos | P0 | Por asignar | Por asignar | Por hacer | S1-03/04/05/06; decisión de alcance | Publicación mínima o precarga aprobada frente a E2; clientes/cuentas trazables; consulta real | E2 §2.1/3; alcance concreto por aprobar | 10–16 h-persona; no correo automático ni CRUD ampliado por defecto |
| S2-04 | 2 | Integración y cierre de pedidos | P0 | Por asignar; participa equipo | Por asignar | Por hacer | Tareas Sprint 2 integradas | React→pedido→State; permisos; tests; UML y pendientes explícitos | Sin pruebas ejecutadas | 12–18 h-persona |

## Sprint 3 — 24–28/10/2026, ventana propuesta

Alcance y asignaciones pendientes de aprobación; no autoriza implementar. P0 significa prioridad propuesta para cerrar el circuito académico, no nuevo compromiso aprobado. Procesos de compra solo se adelantan si pedidos/stock y capacidad lo permiten.

| ID | Sprint | Historia/tarea | Prioridad | Responsable | Revisor | Estado | Dependencias | Criterios de aceptación | Evidencia | Observaciones |
|---|---|---|---|---|---|---|---|---|---|---|
| S3-01 | 3 | Recepción y movimientos de inventario | P0 | Por asignar | Por asignar | Por hacer | S2-02/03; reglas recepción aprobadas | Recepción contra orden; ingreso/egreso/ajuste trazables y atómicos; sin duplicación | E2 §2/4; decisión aprobada | 20–30 h-persona; parciales/excedentes por decidir |
| S3-02 | 3 | Quinteros, autorización y segunda Strategy | P0 | Por asignar | Por asignar | Por hacer | S1-04/05; S2-03; S3-01 integración | Gerencia autoriza manualmente; CompraQuinterosStrategy sin pedidos; interfaz común; orden/recepción/stock | E2 §2.2/5.4; decisiones aprobadas | 20–30 h-persona; autorización registrada y negativa probada |
| S3-03 | 3 | Preparación, despacho y entrega | P0 | Por asignar | Por asignar | Por hacer | S1-08; S2-02; S3-01 | State hasta ENTREGADO; reserva/egreso sin duplicación; despacho/entrega registrados | E2 §2.1/5.5; sin logística propia | 16–24 h-persona; instante reserva/egreso por aprobar |
| S3-04 | 3 | Factura interna y cobranzas | P0 | Por asignar | Por asignar | Por hacer | S3-03; modelo Factura/Pago aprobado | Facturar solo entregado; precios históricos; pago y saldo coherentes; permisos | E2 §2.1/4; simplificación aprobada | 16–24 h-persona; parciales/sobrepago por decidir |
| S3-05 | 3 | Reportes y dashboard aprobados | P0 | Por asignar | Por asignar | Por hacer | S2-03; S3-02/04; definición totales | Totales ventas/compras reconciliables, período y bases explícitos; sin rentabilidad | Decisión reportes; recorte docente pendiente | 12–18 h-persona; consultas stock son operativas, no reporte ampliado |
| S3-06 | 3 | Tres procesos, roles y regresión | P0 | Por asignar; participa equipo | Por asignar | Por hacer | S3-01 a S3-05 | A/B/C reales; tests unitarios/integración separados; accesos no autorizados rechazados | Sin pruebas de negocio ejecutadas | 16–24 h-persona |
| S3-07 | 3 | UML y defensa de cinco patrones | P0 | Por asignar; participa equipo | Por asignar | Por hacer | Clases reales y S3-06 | Clases/componentes/flujo coherentes; participantes concretos; cuatro explican código | Diagramas académicos de diseño, no implementación | 8–12 h-persona |
| S3-08 | 3 | Walkthrough y entrega | P0 | Por asignar; participa equipo | Por asignar | Por hacer | S3-06/07 | Demo ≤5 min A/B/C real; error State; ejecución reproducible; respaldo local | Fecha 31/10 confirmada; guion por ensayar | 8–12 h-persona; 29–30 correcciones, 31 entrega |

## Vista resumida

| Sprint | Terminado | En revisión | Por hacer | Cierre |
|---|---:|---:|---:|---|
| 0 | 7 | 0 | 0 | Cerrado con observaciones |
| 1 | 0 | 1 | 7 | No cerrado |
| 2 propuesto | 0 | 0 | 7 | Planificación pendiente de aprobación |
| 3 propuesto | 0 | 0 | 8 | Planificación pendiente de aprobación |

No se registran Bloqueado sin intento o impedimento concreto; las puertas de aprobación se muestran en dependencias. S1-07/08 aparecen solo en Sprint 2 y conservan su referencia histórica.

## Esfuerzo, ruta crítica y capacidad

Estimación preliminar propuesta, no histórica: Sprint 2 **98–150 h-persona**, Sprint 3 **116–174 h-persona**, total **214–324 h-persona**, más **20–30% de contingencia** por aprendizaje/integración: aproximadamente **257–421 h-persona**, sin contar los siete P0 pendientes del Sprint 1. Incluye desarrollo, integración React y pruebas de cada tarea; no suma capacidad ya disponible. Confirmar horas reales de los cuatro antes de comprometer estas ventanas. Para Sprint 3 de cinco días, 116–174 h equivaldrían a 5,8–8,7 h diarias por persona antes de contingencia: riesgo alto.

Ruta crítica: modelo/historial → JDBC/SQL → sesión/maestros/oferta → pedidos/State → reservas/compras → recepción/entrega → factura/pagos → reportes → regresión/UML/demo. La demora en S1-03 condiciona todo el MVP. Trabajar en contratos/UML/pruebas propuestas sin crear entidades antes de aprobación. Adelantar recepción/Quinteros al Sprint 2 exige capacidad confirmada y acuerdo, no duplicar tareas.

Priorizar circuitos mínimos A/B/C y cinco patrones reales. P1 propuesta: mejoras visuales/filtros simples; P2 propuesta: automatizaciones, exportaciones y enriquecimientos, fuera del compromiso. No retirar P0 o procesos académicos silenciosamente: proponer recorte y obtener aprobación, y validación docente si afecta entregas. 29–30/10: corrección, ensayo y grabación; 31/10: verificación/entrega, sin nuevas funcionalidades.

## GitHub y Render: evidencia actual

- Usuario reporta push al fork de Cirielli, PR #1 hacia Andrés y merge. Historia local observada: **65cd3c1**, mensaje Merge pull request #1 from mcirielli/main, dos padres; corrobora registro de merge, no garantiza estado remoto actual. [PR #1 reportado](https://github.com/acedenosanchez/Proyecto-Comunitarias/pull/1). No se consultó su contenido remoto.
- Remotos locales comprobados: origin=Cirielli, upstream=Andrés. Configuración de otros integrantes por verificar.
- Render: **despliegue realizado según el usuario, con verificación funcional y configuración pendientes**. No hay archivo Render versionado ni URL/nombre/servicio/rama aportados. No se ejecutó deploy en esta tarea. Publicación anteriormente no autorizada es una restricción histórica de aquella etapa; este reporte posterior no autoriza nuevas publicaciones ni valida automáticamente S1-09.
- Checklist Render por completar: URL frontend/backend; tipos de servicio y repo/rama/SHA; contexto/Dockerfile/build/dist; auto-deploy y filtros; PORT/escucha; perfil efectivo; variables por nombre (nunca valores secretos); CORS/HTTPS/cookies/CSRF; SPA/health; JDBC real y smoke test login/oferta cuando existan; reversión. Compose es local, no mecanismo de despliegue asumido en Render.

## Primeras acciones

Cerrar evidencia de capas S1-01 en paralelo con **S1-02 JDBC desde DataSource Spring** (integrante 2 con apoyo 1). Preparar modelo/historial/oferta para aprobación S1-03 y contrato sesión/permisos S1-04. No interpretar esta documentación como autorización de implementación adicional.
