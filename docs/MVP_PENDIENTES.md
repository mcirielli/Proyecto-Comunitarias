# MVP: pendientes y trazabilidad

> Actualización 27/10/2026: el inventario y los conteos originales de este archivo
> son históricos y no reflejan el código actual. Ya funcionan login con cuentas
> locales por rol, alta/consulta y activación de productos/proveedores, persistencia
> Supabase y tests de negocio. Véanse reportes/ULTIMO.md y DEMO_LOCAL.md.
> Siguen pendientes oferta, pedidos/State, compras/Strategy, inventario,
> facturación/pagos, reportes e historial de precios. No interpretar los conteos
> históricos como medición actual ni dar esos flujos por hechos por existir tablas.

Inventario de requisitos agrupados, no conteo de endpoints. [Kanban](KANBAN_SPRINTS.md) organiza el trabajo; el [plan Sprint 1](PLANIFICACION_SPRINT_1.md) conserva el compromiso. Estado del código comprobado por lectura en esta ejecución: solo infraestructura/health y UI de estado; sin entidades, repositorios o servicios funcionales, SQL, login ni pantallas comerciales.

## Fuentes y precedencia

- E1: [entrega inicial](../context/entregas/Proyecto%20_Comunitarias_%20-%20Grupo%202%20%282%29.pdf), cuatro páginas, necesidades/centralización/historial.
- E2: [Actividad 2](../context/entregas/Actividad%202%20-%20Proyecto%20Comunitarias%20-%20Grupo%202%20%281%29.pdf), cinco páginas, tres procesos y cinco patrones seleccionados por el equipo.
- S: [superprompt](../context/SUPERPROMPT.md), especificación interna, no consigna docente independiente. Decisiones aprobadas: [registro](DECISIONES_ARQUITECTURA.md).

Se extrajo y leyó texto de las nueve páginas directamente de streams/mapas PDF locales porque pypdf no estaba disponible y su descarga falló por red. No se realizó nueva revisión visual de diagramas rasterizados; detalles de diagramas provienen también de la planificación histórica que registra su revisión anterior. No se agregan requisitos académicos inferidos de imágenes no comprobadas en esta sesión.

Decisiones posteriores aprobadas prevalecen operativamente; entregas siguen como referencia académica. Un recorte aprobado por equipo no equivale a aprobación docente. No hay nuevas verificaciones de aplicación en esta tarea documental.

## Conteo y estados

**33 requisitos agrupados: 3 completos de infraestructura, 21 pendientes (incluye parciales), 9 sujetos a decisión/validación. Cero funcionalidades comerciales completas.** Categorías excluyentes para evitar doble conteo; decisión no significa requisito opcional. Las puertas de aprobación de una tarea pendiente se describen en observaciones. Los cinco patrones se cuentan en NF-02 y se detallan abajo, sin volver a sumarlos.

## Matriz de requerimientos y trazabilidad

Cada fila enlaza requerimiento → proceso → módulo → patrón → tarea → condición verificable. Fuente expresa distingue documento académico de decisión interna. «Completo» describe solo el requisito acotado, no cierre automático de la tarea vinculada.

| ID | Requerimiento | Fuente | Proceso | Módulo | Patrón aplicable | Tarea Kanban | Estado | Criterio verificable | Implementación/decisión pendiente o recorte |
|---|---|---|---|---|---|---|---|---|---|
| INF-01 | Infraestructura por capas y stack | AGENTS; S; decisiones | Transversal | Infraestructura | MVC parcial | S0-02/03 | Completo | Java 21/Spring/React configurados; paquetes reservados | No demuestra capas de negocio |
| INF-02 | Comunicación React→API health | S §14 | Transversal | HTTP/UI | MVC parcial | S0-02/03; S1-01 | Completo | 200/UP; UI real y error/recuperación reportados | Health no prueba PostgreSQL |
| INF-03 | Protección inicial, CORS, BCrypt y secretos externos | S; decisiones S0 | Transversal | Seguridad | No específico | S0-05 | Completo | Health público; futuro GET 401; seis tests infraestructura registrados | No login ni permisos por rol implementados; no certifica capas Docker |
| INF-04 | Docker reproducible sin secretos en capas | Decisión S1-01 | Transversal | Docker | No específico | S1-01 | Pendiente | 2 imágenes desde fuentes; healthy/proxy/SPA/UI; inspección capas favorable | Funcional aprobado y revisado; falta criterio capas |
| INF-05 | JDBC y persistencia Spring→Supabase | S §6/14 | Transversal | Persistencia | Repository | S1-02/03 | Pendiente | SELECT 1 desde DataSource local/contenedor y luego escritura real | IntelliJ reportado no acredita Spring |
| INF-06 | Configuración y validación Render | Decisión S1-09; reporte usuario | Transversal | Despliegue | No específico | S1-09 | Pendiente | Repo/SHA/perfiles/puerto/CORS documentados; HTTP/JDBC probados según alcance | Desplegado según usuario, sin URL/config verificadas |
| RF-01 | Login y roles Cliente/Operaciones/Gerencia | E2 §3/6; decisión | A/B/C | Usuario/seguridad | MVC/Repository/Service Layer | S1-04 | Pendiente | Login/logout/sesión, BCrypt/CSRF, 401/403, Cliente aislado | Contrato/cookies/modelo requieren aprobación |
| RF-02 | Administración de clientes y perfiles | E2 §3 | A | Clientes/Usuario | MVC/Repository/Service Layer | S1-03/04; S2-05 | Decisión | Acordar cuentas precargadas y gestión mínima; cliente asociado a pedidos | CRUD general no aprobado; alternativa seed requiere modelo |
| RF-03 | Productos con ID independiente del proveedor | E1 §3; E2 §4/6 | A/B | Producto | MVC/Repository/Service Layer | S1-05 | Pendiente | Alta/consulta válida y persistida; ID conserva identidad | Sin Producto implementado |
| RF-04 | Proveedores, códigos y condiciones N:M | E1 §3; E2 §4 | A/B | Proveedor/ProductoProveedor | Repository/Service Layer | S1-03/05 | Pendiente | Relación consultable, código/condiciones sin duplicar producto | Modelo espera aprobación |
| RF-05 | Historial de precios proveedor | E1 §3; decisión | A/B/C | Precios | Repository/Service Layer | S1-03/05 | Decisión | Nuevo precio conserva anterior y consulta vigente/histórico | Obligatorio aprobado; estructura/correcciones/vigencias no aprobadas |
| RF-06 | Cliente consulta oferta semanal | E2 §2.1/6; S | A | Oferta | MVC/Repository/Service Layer | S1-06 | Pendiente | React consulta precio venta persistido; carga/vacío/error; compra oculta | Precarga y selección actual por aprobar |
| RF-07 | Armar/publicar newsletter/oferta | E2 §2.1 | A | Oferta | MVC/Service Layer | S2-05; S3-06 | Decisión | Aprobar si publicación manual mínima o precarga demostrable cubre entrega | Consulta S1-06 no cumple por sí sola publicar; email automático no exigido |
| RF-08 | Crear/consultar pedido con detalle y precio histórico | E1 §3; E2 §2.1 | A | Pedido | MVC/Repository/Service Layer | S1-07 | Pendiente | Cantidad válida, precio servidor, aislamiento y transacción | Trasladado Sprint 2; no controlador pedidos actual |
| RF-09 | Confirmar/seguir pedido con State | E2 §5.5; S | A | Pedido/estados | State | S1-08; S3-03 | Pendiente | Seis estados con contexto/interfaz/clases; salto inválido rechazado | Reglas concretas no aprobadas; enum no basta |
| RF-10 | Consolidar demanda y calcular faltantes | E2 §2.1/5.4 | A | Compras/demanda | Strategy/Service Layer | S2-03 | Decisión | Faltantes según stock/reservas/órdenes abiertas y no duplicación probados | Fórmula/asignación y algoritmo proveedor propuestos, no aprobados |
| RF-11 | Orden por demanda y trazabilidad de compra | E2 §2.1/4 | A | OrdenCompra | Strategy/Repository | S2-03 | Pendiente | Orden con proveedor/origen DEMANDA_CLIENTES y detalles históricos | Sin órdenes; cada regla física por aprobar |
| RF-12 | Compra Quinteros independiente de pedidos | E2 §2.2/5.4 | B | Compras | Strategy | S3-02 | Pendiente | CompraQuinterosStrategy y compra por demanda intercambiables; orden sin pedido | Oportunidad/precio/condiciones evaluados con regla revisada |
| RF-13 | Autorización manual de Gerencia | Decisión; E2 participación gerencial | B | Autorización compra | Service Layer/Strategy | S3-02 | Pendiente | Sin autorización no genera orden; autor/fecha/resultado registrados | Paso manual aprobado; contrato/negativa por definir |
| RF-14 | Recepción y actualización de stock | E2 §2.1/2.2/4 | A/B | Recepción/stock | Service Layer/Repository | S3-01 | Pendiente | Recepción vinculada a orden, ingreso trazable y transacción | Parciales/excedentes/repetición requieren reglas aprobadas |
| RF-15 | Reservas, egresos y ajustes | E2 §4; decisión | A/B | Inventario | Service Layer/State | S2-02; S3-01/03 | Decisión | Disponible/físico/reservado; reserva automática y egreso sin duplicación | Aprobados tipos; instante reserva/liberación, concurrencia y ajustes pendientes |
| RF-16 | Preparar, despachar y entregar | E2 §2.1/6 | A | Entrega/Pedido | State/Service Layer | S3-03 | Pendiente | Persistir preparación/despacho/entrega y restricciones de estados | Distribución tercerizada; no ruteo ni flota |
| RF-17 | Facturación interna | E2 §2.1/4/5.5; decisión | A | Factura | Service Layer/Repository | S3-04 | Pendiente | Solo pedido entregado; factura persistida con importe histórico | No facturación fiscal; cardinalidad/numeración pendientes |
| RF-18 | Registrar pagos y cobranza | E2 §2.1/4; decisión | A/C | Pago | Service Layer/Repository | S3-04 | Decisión | Importe/saldo coherentes; pago persistido y autorizado | Registro aprobado; pagos parciales/sobrepago/anulación no aprobados |
| RF-19 | Totales ventas y compras/dashboard | E2 §2.3; decisión recortada | C | Reportes | MVC/Service Layer/Repository | S3-05 | Pendiente | Período/base de venta/compra definidos; totales reconciliables | No usar cobranzas como ventas sin regla; sin rentabilidad |
| RF-20 | Rentabilidad y reportes ampliados | E2 §2.3/3; S | C | Gerencia | Service Layer | S3-05/07 | Decisión | Obtener validación docente del recorte o revisar compromiso | Excluidos por equipo: rentabilidad y reportes stock/clientes/proveedores/cobranzas; conformidad docente no acreditada |
| RF-21 | Ingreso desde distintas fuentes | E1 §3; E2 §6 | A/B | Carga maestros | MVC/Service Layer | S1-05; S3-06 | Decisión | Acordar carga manual/precarga suficiente y trazable | Excel/OCR/email automático no requisito de implementación explícito; no añadirlos |
| NF-01 | Modelo y esquema aprobado antes de entidades/SQL | Decisiones; S §6 | Transversal | Domain/database | Repository | S1-03; S2-01 | Decisión | Campos/relaciones/PK/FK/precisión/cuenta/datos aprobados | Solo database/README.md actual |
| NF-02 | Cinco patrones reales con responsabilidades | E2 §5; S §4 | Transversal | Capas/patrones | MVC/Repository/Service Layer/Strategy/State | S1-05/06; S1-08; S2-03; S3-02/07 | Pendiente | Clases/interfaz/delegación y comportamiento demostrables | MVC técnico parcial; resto sin clases de negocio |
| NF-03 | Tests de reglas y patrones | AGENTS; S §10 | Transversal | Pruebas | Todos aplicables | S1-10; S2-04; S3-06 | Pendiente | Unitarios sin Supabase; integración separada; State/Strategy/stock/roles | Seis tests de infraestructura existen; no tests de negocio |
| NF-04 | UML actualizado y trazabilidad | E2 diseño; S §11 | Transversal | Documentación | Cinco patrones | S3-07 | Pendiente | Diagramas clases/componentes/flujo corresponden al código y casos de uso | Diseño académico no prueba implementación |
| NF-05 | Walkthrough ≤5 minutos y defensa | S; fecha AGENTS | A/B/C | Entrega | Cinco patrones | S3-08 | Pendiente | Demostración real tres procesos y explicación por cuatro integrantes | No grabación ni ensayo acreditados |
| NF-06 | Transacciones, dinero preciso, errores y trazabilidad | E1; E2 §4/5; S §6/9 | A/B/C | Servicios/DTO | Service Layer/Repository | S1-05; S1-07; S3-01/04 | Pendiente | BigDecimal, validaciones, errores HTTP y cambios atómicos probados | Reglas concretas se aprueban con contratos |

## Procesos que deben quedar completos

**A. Venta semanal:** armar/publicar oferta → Cliente consulta → pedido y seguimiento → consolidar/abastecer faltantes → recibir/stock/reservar → preparar/despachar/entregar → factura interna/cobranza. Hoy solo health/UI, ninguna operación de este circuito. Oferta precargada versus publicación mínima necesita acuerdo; la consulta sola no acredita todo el proceso.

**B. Quinteros:** evaluar oportunidad/producto/proveedor/precio/condiciones → autorización manual Gerencia → orden independiente de pedidos → recepción → stock disponible para futuras ofertas. Reutiliza orden/recepción/inventario; algoritmo concreto de conveniencia/selección no documentado, no inventarlo como obligatorio.

**C. Gestión:** consultas operativas de pedidos/compras/stock y dashboard con totales aprobados de ventas/compras. Bases de cálculo/períodos por definir. No confundir consulta operativa de stock requerida en A/B con reporte gerencial ampliado excluido.

**Recorte académico:** E2 pide rentabilidad y reportes de stock, clientes, proveedores y cobranzas además de ventas/compras. Decisión del equipo limita reportería a totales de ventas/compras. Requiere validación docente; no hay evidencia de aprobación de cátedra. No implementar fórmula de margen histórica ni restaurar reportes ampliados sin revisión.

## Cinco patrones: diseño, implementación y prueba

| Patrón | Diseñado/documentado | Implementación real actual | Prueba actual / pendiente | Participantes Java futuros propuestos |
|---|---|---|---|---|
| MVC | E2 §5.1 y S | Parcial técnico: HomePage/health.ts, HealthController/HealthResponse; sin Model de negocio | Health/UI reportados, test health existente; negocio pendiente | ProductoController/DTO, Producto/ProductoService y React |
| Repository | E2 §5.2 y S | No: dependencia JPA y package-info no son repositorios funcionales | Ninguna persistencia de negocio probada | ProductoRepository, ProveedorRepository, PedidoRepository, OrdenCompraRepository |
| Service Layer | E2 §5.3 y S | No: paquete reservado, sin casos de uso | Ninguna regla de negocio probada | ProductoService, PedidoService, CompraService, StockService, FacturacionService, ReporteService |
| Strategy | E2 §5.4 y S | No: no interfaz ni estrategias | Probar mismos contratos con dos algoritmos y faltantes/independencia | CompraStrategy, CompraPorDemandaStrategy, CompraQuinterosStrategy; contexto CompraService |
| State | E2 §5.5 y S | No: ni enum ni contexto funcional | Transición válida/ inválida, persistencia y reconstrucción pendientes | Pedido contexto, PedidoState, RecibidoState/ConfirmadoState/EnPreparacionState/PreparadoState/DespachadoState/EntregadoState |

Son cinco patrones seleccionados académicamente, no cinco GoF por definición. Strategy necesita polimorfismo real y State delegación al objeto actual; un if/else gigante, enum o paquete vacío no acredita el patrón. Nombres futuros son ejemplos del diseño, no archivos existentes ni aprobación del esquema.

## Discrepancias: históricas y pendientes

| Diferencia | Interpretación vigente | Acción |
|---|---|---|
| Selector de perfiles S versus usuario/login E2 | Sustituido por login real simplificado aprobado | S1-04, no usar selector para simular autenticación |
| Prohibición Docker original | Sustituida expresamente por Compose/multi-stage/Nginx | S1-01; no cambiar arquitectura |
| Vercel opcional y demo local versus Render | Render preparado según decisión y desplegado según usuario; demo local permanece respaldo | Validar configuración y funcionalidad S1-09; no asumir Compose en Render |
| PLANIFICACION_MVP: margen, reportes amplios y calendario antiguo | Propuesta histórica, reemplazada por decisiones/sprint formal | Conservar original; no convertir margen en alcance aprobado |
| Plan S1: Nginx/multi-stage pendientes | Resueltos por autorización posterior en decisiones y SPRINT_1 | No reabrir aprobación ni reescribir alcance |
| S1-07/08 originalmente Sprint 1 | Trasladadas a Sprint 2 con IDs intactos | No duplicar ni implementar en S1 |
| Newsletter/ItemNewsletter versus OfertaSemanal/DetalleOferta | Equivalencia conceptual; nombres no prueban cardinalidades | Aprobar modelo/semántica antes de SQL |
| Rol/Recepcion y detalles no enumerados en S | Plan histórico identifica diferencias con diseño académico | Validar modelo contra diagrama original antes de entidades |
| Rentabilidad/reportes E2 versus recorte del equipo | Recorte vigente pero conformidad docente no acreditada | Obtener validación docente o revisar recorte |
| S0 distribución sugerida versus asignación formal S1 | Histórica y sustituida para S1 | Usar autores/revisores del tablero formal |

## Decisiones a presentar antes de implementar lo dependiente

Modelo/historial y vigencias/correcciones; Usuario/Cliente/rol; oferta y precio de venta; contrato de sesión/CSRF/permisos y cookies públicas; reservas/concurrencia/transiciones; consolidación de faltantes y proveedor; recepción parcial/excedentes; factura/pagos/saldo; bases de totales y validación docente; carga manual y publicación newsletter mínima. No todos bloquean comenzar JDBC o diseñar contratos, pero sí la acción dependiente. Sin porcentajes de cobertura, SLA, OCR, correo automático o reglas monetarias nuevos atribuidos a fuentes.
