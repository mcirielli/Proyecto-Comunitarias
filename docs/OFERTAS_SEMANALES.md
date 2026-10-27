# Gestión y publicación de ofertas semanales

27/10/2026. Incremento C05, rama `tino`, sobre `main` con Pedido + State
(`c80373b`, integrado mediante `2ae7e11`). Reutiliza Oferta, DetalleOferta,
OfertaRepository y la creación de pedidos existente. Sin DDL ni dependencias nuevas.

## Reglas y decisión aprobada

El usuario aprobó expresamente en este chat: las ofertas publicadas no se editan
en este incremento; para cambios se crea otro borrador. Tampoco se implementan
edición de borradores, eliminación, despublicación ni emails automáticos.
Si un producto se desactiva después, se oculta al Cliente y PedidoService rechaza
nuevos pedidos de ese producto. Los detalles históricos del pedido se conservan.
Operaciones sigue viendo los productos inactivos de la oferta para diagnóstico.

Se puede crear un borrador completo con título, fechas, 1–100 productos activos
distintos y precio de venta BigDecimal. Se conserva precio cero permitido por
el contrato existente y el CHECK de Supabase. Esquema real inspeccionado por JDBC:
precio_venta numeric(12,2), máximo 9999999999.99 y dos decimales; se rechazan
valores que provocarían redondeo u overflow. No se calcula precio desde costos.

Publicar vuelve a validar fechas, detalles, productos activos/únicos y precios.
Cabecera y detalles se guardan en una transacción; publicación usa bloqueo
pesimista sobre la oferta, sin columna adicional. Repetir publicación es
idempotente. La desactivación posterior conserva la regla actual de pedidos;
no se incorpora coordinación concurrente entre maestros e inventario.

Estados visibles: BORRADOR; publicada FUTURA, VIGENTE o VENCIDA.
Inicio y fin son inclusivos en America/Argentina/Buenos_Aires; ambas fechas
pueden coincidir. Publicar no implica vigencia inmediata: las fechas deciden.
Varias ofertas pueden estar vigentes y el usuario elige explícitamente.
Una oferta vigente sin productos activos muestra un aviso sin habilitar pedido.

Cliente recibe solo ofertas publicadas vigentes y datos comerciales de venta.
El DTO contiene id, título, fechas, publicada, estado y productos con id,
nombre, unidad, precioVenta y activo. Nunca contiene proveedor ni precio de compra.
Gerencia no tiene acceso. Las escrituras exigen Operaciones, sesión y CSRF.

## API

| Método | Ruta | Contrato |
|---|---|---|
| GET | /api/ofertas | Operaciones: todas; Cliente: publicadas vigentes |
| POST | /api/ofertas | Operaciones: crea borrador completo, responde 201 |
| POST | /api/ofertas/{id}/publicacion | Operaciones: valida y publica, responde 200 |
| GET | /api/pedidos/contexto | Contrato existente, ofertas vigentes y clientes autorizados |
| POST | /api/pedidos | Contrato existente: copia precios del servidor y valida identidad |

Ejemplo de borrador (reemplazar productoId por un producto activo real):

```json
{"titulo":"Oferta semanal","fechaInicio":"2026-10-27","fechaFin":"2026-11-02","detalles":[{"productoId":1,"precioVenta":"17.25"}]}
```

Errores: 400 entrada inválida, 404 recurso inexistente, 409 regla o restricción
de datos, 401 sesión requerida y 403 permisos/CSRF. No hay otro controlador que
cree pedidos ni otra fuente de validación de sus precios.

## Capas y defensa

OfertaPage usa fetch mediante api/write existentes. OfertaController recibe y
devuelve DTOs. OfertaService coordina validación, repositorios y transacciones.
Oferta conserva fechas/publicación y DetalleOferta el precio de venta.
OfertaRepository extiende Spring Data JPA y bloquea al publicar. Esto amplía MVC,
Service Layer y Repository; no agrega patrones artificiales ni cambia State.
El constructor histórico de Oferta conserva publicación automática para los
fixtures anteriores; la API nueva usa explícitamente Oferta.borrador.

## Demo

1. Arrancar con los tres Compose indicados en DEMO_LOCAL.md.
2. Operaciones → Oferta semanal → Nueva oferta: título, fechas inclusivas,
   producto activo y precio. Guardar borrador y comprobar estado.
3. En otra sesión Cliente, comprobar que ese borrador no aparece.
4. Operaciones publica. Actualizar Cliente: aparece si está vigente.
5. Cliente elige Crear pedido con esta oferta: abre el formulario existente
   de Pedidos con esa selección. Ingresa cantidades y registra.
6. Verificar éxito real, total y precio histórico. Operaciones consulta el
   mismo pedido; el ciclo State continúa sin cambios.
7. Ofertas futuras/vencidas quedan visibles solo para Operaciones. Con varias
   vigentes, probar selección explícita; no hay elección automática de la primera.

Para el Cliente demo debe existir la asociación preparada por Operaciones en
Pedidos → Preparar datos de ejemplo, tal como documenta PEDIDO_STATE.md.
Esto prepara identidad demo; no reemplaza el alta/publicación real de ofertas.
Los errores de sesión vencida se muestran con el cliente HTTP existente;
cerrar sesión y volver a ingresar. Actualizar / Reintentar recupera el catálogo.

## Alcance académico y contradicciones revisadas

La Actividad 2 exige newsletter consultable por usuario y separación por capas:
este incremento implementa la publicación/consulta web. Excel/OCR, envío de mail,
costos y abastecimiento permanecen fuera de este incremento autorizado.
Prevalece el login por sesión aprobado sobre el selector de perfiles del
superprompt; el recorte gerencial de rentabilidad sigue pendiente de validación
docente según DECISIONES_ARQUITECTURA.md. No se afirma cierre completo del MVP.
El material UADE de SOLID/MVC se usa como referencia de separación de responsabilidades.

Pruebas, comandos, evidencia real y límites: reportes/2026-10-27-ofertas-semanales.md.
