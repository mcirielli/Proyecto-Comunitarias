# Migración 002 de compras aplicada en Supabase

Fecha: 27/10/2026, zona Argentina. Autorización expresa del usuario en este chat:
«autorizo», en respuesta a aplicar únicamente 002_compras_strategy.sql.
No autoriza merge, despliegue ni cambios adicionales de negocio.

## Versión y preparación

Se volvió a obtener la propuesta desde la rama andy y se verificó identidad exacta
mediante SHA-1 de blob Git: fa05b7621ab17f6fc110003f16a79468356288d7.
Las credenciales permanecen exclusivamente en .env/entorno del proceso, sin
imprimir URL, usuario ni contraseña. Conexión JDBC con SSL obligatorio.

Docker apagado y pg_dump no disponible. Se guardaron instantáneas locales JSON de
columnas/tipos/defaults/identidad, restricciones, índices, RLS, ACL, políticas y
conteos de las tablas afectadas, antes y después. Esto es respaldo de metadatos,
no un dump completo/restaurable de toda la base ni copia de datos comerciales.
Archivos ignorados: .cache/compras-schema-before-002.json y
.cache/compras-schema-after-002.json. No se publican sus ACL ni credenciales.

## Ejecución

Runner auxiliar JDBC local, JDK 23 y driver PostgreSQL ya disponible; no implica
pruebas de la aplicación Java 21. Precondiciones: ordenes_compra con cinco columnas,
estado varchar(20), nuevas tablas ausentes, órdenes/detalles vacíos. Bloqueo sobre
ordenes_compra durante la operación; lock_timeout 5 segundos y statement_timeout
30 segundos para evitar esperas indefinidas.

Se ejecutaron exclusivamente las sentencias de la propuesta, conservando su
transacción única: BEGIN/COMMIT exteriores gestionados mediante JDBC, contenido
DDL sin modificaciones. Verificación dentro de la transacción antes de commit
y nueva consulta después del commit.

Primer intento detenido antes de DDL por comparación del formato booleano
devuelto por JDBC. Se corrigió el runner para normalizar Boolean; rollback y
reintento. Segundo intento: COMMIT confirmado y verificación post-commit correcta.
No se reejecutó la migración después del éxito.

## Resultado verificado

- estado ampliado a varchar(30).
- Seis columnas nuevas de clave de solicitud y trazabilidad de autorización/emisión.
- Tres tablas nuevas de cobertura, con RLS habilitado.
- CHECKs de estados y autorización, índices y permisos JDBC SELECT/INSERT/UPDATE
  comprobados individualmente, además del uso de la secuencia nueva.
- Comparación de snapshots: PK/FK/UNIQUE/CHECK preexistentes conservados, salvo
  sustitución del CHECK de estado expresamente prevista.
- Órdenes, detalles y tablas nuevas siguen vacías. Sin alta de compras, fixtures,
  cambios de productos, pedidos, stock ni precios.

Log local ignorado: .cache/aplicacion-compras-002.log.
La verificación no acredita todavía persistencia funcional ni concurrencia de
Compras: no se ejecutaron CompraSupabaseIT ni HTTP/UI con el módulo habilitado.

## Estado siguiente

Dependencia de esquema resuelta en la base configurada localmente. Compras no fue
habilitado, no se cambió la configuración de la aplicación ni se fusionó PR #5.
Para dar por terminado el módulo: integrar base actual de main con andy en entorno
aislado, ejecutar pruebas Java 21 y regresión, CompraSupabaseIT, concurrencia y
recorridos demanda/Quinteros con autorización y emisión; luego revisión del equipo.
Los comentarios históricos «NO APLICADA» en la rama de Andy deben actualizarse
en el cierre de esa PR con referencia a esta evidencia, sin reejecutar el script.
