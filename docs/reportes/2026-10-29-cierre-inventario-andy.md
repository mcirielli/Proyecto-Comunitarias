# Cierre técnico de inventario Andy

Fecha: 29/10/2026, America/Buenos_Aires. Estado: CERRADO TÉCNICAMENTE.
Alcance: stock, reservas, recepción, State/Strategy, coberturas, UI, pruebas y
compatibilidad con la rama disponible de Tino. No se declara completo todo el MVP.

## Autorizaciones y base

El usuario aprobó políticas/modelo y luego «Autorizar 004 y validación real».
El 29/10 solicitó finalizar los pendientes y después autorizó expresamente push
a Andy. No autorizó merge a main ni despliegue público. Se conservaron las
aprobaciones históricas y los archivos de Factura/Pago de Tino.

Andy parte de main da976fc y conserva los commits publicados hasta a7fac07.
Esta ventana agrega 2765f90 (matriz persistente ampliada) y eb0af82 (UTF-8 y
fechas explícitas de Buenos Aires), más este cierre documental. El push autorizado
se realiza después de revisar diff, secretos y resultados.

La copia .cache/inventario-integracion-tino usa la rama local
codex/validacion-inventario-tino: combina Andy con origin/tino cdd6880 en 23e3b29
y conserva el estado validado final en 4d310cd. No se publica esa rama ni se
incorporan los commits de Tino a Andy. Main remoto sigue en da976fc.

## Cambios y pendientes cerrados

| Pendiente anterior | Resultado verificable |
|---|---|
| Pedido #51 en preparación con cuatro reservadas | Navegador completó PREPARADO → DESPACHADO → ENTREGADO; un egreso de cuatro, reserva consumida |
| Compatibilidad con Factura/Pago no disponible en main | Rama Tino cdd6880 disponible; validación aislada de ambos frentes. Factura #12 INT-PED-51 emitida por ARS 4000, reintento conserva ID |
| Legado y productos inactivos sin cobertura completa de aceptación | Quince InventarioSupabaseIT con datos propios: estados avanzados sin reserva, PENDIENTE legado, inactivo histórico, overflow, recepción multilínea y rollback de egreso |
| Trazabilidad y concurrencia | Producto/pedido/orden/cantidad/fecha/actor comprobados; recepción y reservas concurrentes con conexiones distintas |
| Documentos/índices desactualizados | Políticas, API/estado, matriz, backlog, demo y último reporte actualizados, preservando antecedentes |
| Hora dependiente del navegador y errores JSON con acentos | Pedidos/Compras declaran zona Buenos Aires; SecurityConfig declara UTF-8 en éxito/error. Prueba HTTP nueva sobre JSON 401 |

Participantes y casos por regla: [matriz](../INVENTARIO_ACEPTACION.md).
State y Strategy siguen siendo implementaciones polimórficas reales. Sin nuevas
dependencias, cambios de precio/productos existentes ni nuevos patrones artificiales.

## Pruebas ejecutadas en esta ventana

| Entorno/comando | Resultado | Log local ignorado |
|---|---|---|
| Andy, Maven Java 21 offline verify | 128 aprobadas, cero fallos/errores/omitidas | .cache/inventario-final-verify.log |
| Andy, npm --prefix frontend run build | TypeScript/Vite aprobado | Salida observada de herramienta |
| Copia Andy + Tino, Docker build backend/frontend | 153 estándar aprobadas y frontend compilado; ambos contenedores healthy | .cache/inventario-final-docker-integrado.log |
| Copia combinada, Maven Supabase con tres flags true | 35 aprobadas: 15 inventario + 13 regresión previa + 7 facturación; cero fallos/errores/omitidas | .cache/inventario-final-supabase-integrado.log |
| HTTP real después de reconstrucción | Pedido entregado, factura idempotente, stock/movimientos invariantes, roles/CSRF/reintentos/sesión y JSON UTF-8 estricto | .cache/inventario-cierre-http.json |
| Navegador real | Pedido #51 entregado y facturado; Cliente consulta sin emitir/pagar ni ver inventario; Gerencia consulta sin recibir | Evidencia de CUA; capturas locales |
| Git diff --exit-code origin/tino sobre sus archivos propios | Idénticos: no se editaron Factura/Pago, sus servicios/repositorios/API/UI/tests | Salida observada de herramienta |

Verificación Supabase explícita, sin desactivar inventario:

```powershell
mvn -o -f backend/pom.xml -Dapp.inventario.enabled=true -Dapp.compras.enabled=true -Dapp.facturacion.enabled=true -Dtest=InventarioSupabaseIT,CompraSupabaseIT,OfertaSupabaseIT,PedidoSupabaseIT,SupabaseJdbcIT,MaestrosSupabaseIT,FacturacionSupabaseIT test
```

Los casos de facturación mantienen sus fixtures específicos; la integración real
con inventario se acredita adicionalmente con el pedido #51, sin saltar estados
ni apagar el módulo. Las pruebas concurrentes confirman únicamente fixtures propios
para compartirlos entre conexiones y los eliminan por IDs en AfterEach. No son
pruebas de carga ni certificación de producción.

## Demo persistida y balance final

- Pedido #50: ENTREGADO; órdenes #17 demanda y #18 Quinteros: RECIBIDA.
- Pedido #51: ENTREGADO; orden #19: RECIBIDA. Se usaron tres unidades libres y
  se compró una, se recibieron cuatro disponibles en total, se reservaron cuatro
  y se consumieron una vez al despachar. Entregar no descontó otra vez.
- Producto #2: libre 0, reservado 0. Su historial conserva siete movimientos;
  el pedido #51 tiene una RESERVA de cuatro y un EGRESO de cuatro. Orden #19
  registra un solo INGRESO de una unidad.
- Factura #12, INT-PED-51: importe histórico ARS 4000, PENDIENTE, sin pagos nuevos.
  Reemisión devuelve #12 y conserva saldos/movimientos. No se procesa dinero.
- Las órdenes antiguas #3/#4 y los pedidos históricos ajenos a esta demo no se
  recibieron ni se les inventaron movimientos. No se editaron sus datos.

## Migraciones, fallos y recuperación

004 ya estaba aplicada con autorización y hash
54d2eba3baf035bdfabbb9f33df0f20c154ebc65fa5881ac9b1d4ec6381575dc.
Se verificó la presencia de la columna de 003 de Tino. No se ejecutó DDL en esta
ventana ni se reejecutaron 001/002/003/004. RLS y el modo ddl-auto=none/
sql.init.mode=never se conservan.

La primera matriz ampliada dio 27/28: una aserción nueva esperaba dos unidades
en un fixture de cinco. Se corrigió la expectativa, sin cambiar negocio, y la
pasada final combinada terminó 35/35. Los mensajes SQL de clave duplicada en la
prueba negativa de maestros son esperados; no equivalen a un fallo del resultado.

Un auxiliar JDBC de inspección encontró SQLSTATE 42P05 por prepared statements
del pooler; se ajustó prepareThreshold=0 igual que la configuración de la app.
Las llamadas HTTP revelaron errores 401 con codificación incorrecta; se corrigió
UTF-8 y se verificó con parsing estricto. No se cambiaron permisos de negocio.

## Entrega y límites

La aceptación técnica del incremento está cerrada. Quedan la revisión humana y
los merges de PRs del equipo a main: no se realizan desde esta tarea. La rama de
Tino no se publica ni modifica. PR #6 se actualiza con el push de Andy y conserva
su condición de borrador hasta la revisión del usuario.

Entorno conjunto local: frontend http://127.0.0.1:8085, backend loopback 8084.
Usa configuración demo y Supabase existente, sin publicación externa. Las
credenciales JDBC no se copiaron al worktree ni a los artefactos públicos.
No se añadieron cancelaciones, ajustes manuales, parciales o reversiones fuera
del alcance aprobado; rentabilidad y otros frentes siguen según su backlog.

## Contexto para continuar

Usar .cache/andy-inventario, rama andy, leer este cierre y la matriz de aceptación.
No reejecutar migraciones. Para combinar formalmente Tino, preservar las rutas
/api/stock, /api/compras y /api/facturas, /api/pagos, ambos flags y vistas, más
los enlaces de ambos frentes en ULTIMO. La resolución local de referencia está
en 23e3b29/4d310cd. No habilitar módulos en otra base sin inspección/autorización.
