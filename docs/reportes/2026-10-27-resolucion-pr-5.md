# Resolución de conflictos del PR #5

Fecha: 27/10/2026. Rama de trabajo: `andy`. Base incorporada: `origin/main` (`adbb0c1`, PR #4 de ofertas). PR de compras: `84f7d93`.

## Cambios

Se integran las incorporaciones paralelas de ofertas y compras en cinco archivos:

- `SecurityConfig.java`: se conservan ambas matrices de permisos; Cliente no accede a costos/compras y Gerencia no administra ofertas.
- `AuthFlowTest.java`: se conservan las pruebas HTTP de ofertas y compras, incluidos roles y CSRF.
- `OperacionesPage.tsx`: se conservan ambas pantallas y el recorrido oferta → pedido con la oferta seleccionada. Solo reportes conserva la marca de funcionalidad futura; Compras muestra su disponibilidad real.
- `DECISIONES_ARQUITECTURA.md`: se conservan las decisiones registradas para ambos incrementos.
- `ULTIMO.md`: distingue oferta funcional de compras con integración pendiente y enlaza este reporte.

Se mantiene el monolito y no se agregan dependencias. El selector de perfiles del superprompt ya fue sustituido por sesiones/roles según las decisiones aprobadas. El recorte de rentabilidad respecto de la entrega académica conserva su validación docente pendiente. Esta resolución no cambia esas decisiones.

## Verificación

- `docker compose build backend frontend`: compilación de ambos módulos aprobada.
- Backend: `mvn --batch-mode --no-transfer-progress clean verify` con Java 21 dentro de Docker; **111 pruebas, 0 fallos, 0 errores, 0 omitidas**. Incluye la matriz HTTP combinada de ofertas/compras, servicios y patrones. Advertencias existentes por `@MockBean` obsoleto, sin impedir la compilación.
- Frontend: `npm ci` y `npm run build` (TypeScript y Vite) aprobados; auditoría npm: 0 vulnerabilidades.
- `git diff --check` sin errores y fuentes sin marcadores de conflicto.
- No se ejecutaron pruebas `*SupabaseIT` ni validación visual de navegador; estas comprobaciones no acreditan el circuito persistido de compras.

## Límites

No se aplica la migración 002 ni se modifica Supabase. Compras sigue deshabilitado por defecto y el PR conserva su condición de borrador. La validación persistida de compras, concurrencia y recorrido real HTTP/UI sigue pendiente de la migración autorizada. No se fusiona el PR en main ni se despliega.
