# Oferta semanal: gestión, publicación y pedido real

Fecha: 27/10/2026, America/Argentina/Buenos_Aires. Incremento C05.
Estado funcional: implementado y verificado contra Supabase; revisión del equipo
mediante PR pendiente. No implica cierre completo del MVP.

## Base y autorización

Se revisaron AGENTS.md, SUPERPROMPT, ambas entregas académicas, referencia UADE
SOLID/MVC, decisiones, último reporte Pedido + State, pendientes del equipo,
PEDIDO_STATE y estructura compartida. Se conserva prioridad académica, capas y
esquema. Contradicciones vigentes: sesión aprobada sustituye selector de perfiles;
exclusión de rentabilidad mantiene pendiente validación docente. Publicación web
no incluye automatización de correo ni importación Excel/OCR.

`git status --short` inicial limpio; `git fetch origin` actualizó referencias.
Pedido + State c80373b es ancestro de origin/main, merge 2ae7e11. No existía tino
local/remota; se creó desde origin/main. Sin reset, cherry-pick ni duplicación
de commits; no se sobrescribieron cambios ajenos ni se usó force push.
Usuario autorizó implementación, pruebas, documentación, commit, push y PR a main,
sin merge. Aprobó expresamente publicaciones sin edición y exclusión de productos
desactivados de futuras compras, preservando históricos.

## Resultado

- Entidades/repositorio existentes ampliados de forma compatible. Constructor
  anterior conserva contrato demo; nueva fábrica explícita de borradores.
- OfertaService, DTOs y controlador propios: crear, consultar y publicar con
  transacciones; bloqueo pesimista al publicar y operación idempotente.
- Esquema real inspeccionado por JDBC: nueve columnas en ofertas/oferta_detalle,
  numeric(12,2), CHECK precio >=0 y fechas ordenadas, FK y producto único por oferta.
  Validación evita redondeos/overflow sin cambiar DDL.
- OfertaPage usa API real, carga/vacío/error/reintento, formulario y catálogo.
  Estados borrador/publicada futura/vigente/vencida; datos de venta sin costos.
- Cliente continúa a Pedidos con su selección; alta/precios/identidad permanecen
  en PedidoService. Se eliminó selección automática arbitraria de primera oferta.
- SecurityConfig limita consulta a Cliente/Operaciones y escrituras a Operaciones;
  sesión y CSRF activos. Temas y shell conservados con integración acotada.
- Documentación OFERTAS_SEMANALES, decisiones, README, demo y backlog actualizados.

No se modificaron PedidoService, PedidoState, strategy, compras, producto_proveedor
ni stock. Sin dependencias nuevas, servicios públicos, cambios de producción o DDL.
ddl-auto=none y sql.init.mode=never permanecen.

## Comandos y resultados

1. `docker compose build backend frontend`: build frontend TypeScript/Vite y
   Maven `clean verify` dentro de Docker con Java 21. Primera ejecución: 82 tests;
   ejecución final después de ampliar pruebas/precisión: **85**, cero fallos,
   errores u omitidas; BUILD SUCCESS. Logs locales ignorados:
   `.cache/ofertas-final-build.log`.
2. `docker build --target build -t comunitarias-ofertas-check backend`: prepara
   entorno Maven/Java 21 con dependencias, sin incluir credenciales.
3. Integración explícita con ese entorno, fuentes backend montadas y únicamente
   variables JDBC del .env local transmitidas por entorno:
   `mvn -o --batch-mode --no-transfer-progress
   -Dtest=OfertaSupabaseIT,PedidoSupabaseIT,SupabaseJdbcIT,MaestrosSupabaseIT test`.
   **10 aprobadas**, cero fallos/errores/omitidas, incluida repetición final tras
   ajuste de precisión. Logs `.cache/ofertas-it.log`. Cada prueba revierte datos;
   prueba específica fuerza fallo después de flush/publicación y comprueba que
   no sobreviven cabecera ni detalles. Otra conserva precio histórico tras cambio
   externo de oferta y reconstrucción del pedido. Error de CUIT duplicado en log
   es el rechazo esperado de MaestrosSupabaseIT, no una falla de suite.
4. `docker compose -f compose.yaml -f compose.supabase.yaml -f compose.demo.yaml
   up -d --no-build`: imágenes finales iniciadas localmente. Healthchecks no se
   presentan como evidencia JDBC; esa evidencia está en integración/smoke.
5. Smoke Python HTTP real Nginx → Spring → Supabase con cookies/CSRF por sesión:
   Operaciones creó oferta **#4**, borrador oculto al Cliente, luego publicación;
   Cliente consultó y creó pedido **#4**, producto #1, ARS 17,25 × 2 = **34,50**.
   Cliente no recibe campos de costo; rechazo 403 a escrituras de Cliente,
   Gerencia y publicación sin CSRF; suplantación de cliente rechazada 409.
   No se prepararon fixtures ni modificaron productos comerciales existentes.
   Se reutilizó asociación demo ya disponible. Oferta de prueba vigente 27/10
   inclusive; pedido queda RECIBIDO para demo. Son los únicos datos persistentes
   agregados por ese smoke; IT revierte sus propios datos.
6. Navegador Chromium mediante Playwright local: revisión de formulario Operaciones
   y catálogo Cliente con capturas oscuro/claro. Selección explícita hacia Pedidos
   y vista móvil verificadas en el cierre. Capturas y scripts en `.cache/`, fuera
   del commit. Los primeros intentos del script fallaron por selector de option
   oculto y codificación de labels; se corrigió el harness, sin simular operaciones.
7. Intento alternativo Maven offline con caché .m2 del host falló por artefacto
   surefire-api ausente. Se resolvió usando el entorno Docker construido completo;
   no se agregaron dependencias al proyecto ni se omitieron tests.
8. Conector GitHub devolvió 422 al buscar repositorio; fallback con API GitHub y
   autenticación local de Git sin imprimir secretos. No existía PR abierta de tino.

## Límites y coordinación

Publicado no tiene edición, borrado o despublicación en esta API. Tampoco se edita
borrador: el alcance autorizado permite crear uno completo y publicarlo.
Productos desactivados posteriormente se ocultan y no pueden pedirse; pedidos
previos mantienen precios/detalles. No se agrega bloqueo concurrente entre cambios
de maestros y publicación: se conserva validación de productos de PedidoService.
OfertaRepository bloquea publicación; atomicidad no equivale a pruebas de carga.

Pendientes externos: Strategy/compras de Andy, inventario/reservas, facturación,
pagos y reportes en sus frentes; revisión académica de rentabilidad. No bloquean
el circuito de oferta/pedido verificado aquí. No se acredita logística completa.
Merge/auto-merge no autorizados y no ejecutados. GitHub queda para revisión del equipo.
