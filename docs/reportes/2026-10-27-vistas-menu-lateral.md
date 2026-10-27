# Vistas separadas y menú lateral

Estado: CERRADO. Fecha: 27/10/2026 America/Buenos_Aires.

## Alcance autorizado

Usuario pidió separar vistas siguiendo referencia visual de dashboard con menú
lateral. Solo frontend, conservando paleta clara/oscura y lógica/persistencia
existentes. No se implementaron módulos de negocio nuevos ni importación de archivos.

## Cambios

- LoginPage independiente de espacio autenticado.
- OperacionesPage coordina sesión, logout, layout y navegación por hash con enlaces
  reales y soporte de recarga/atrás/adelante, sin nueva librería de routing.
- Sidebar con marca, vista activa, menú por rol y usuario/logout en parte inferior.
- Cabecera con fecha real Argentina, título de vista y selector de tema.
- ResumenPage consulta productos/proveedores reales para totales y activos; accesos
  a módulos funcionales. Sin ventas/pedidos/cifras inventadas.
- ProductosPage y ProveedoresPage separadas; MaestrosPage reutiliza tabla, buscador,
  formulario de alta desplegable y activación/desactivación. Mantiene Fetch/CSRF.
- PendientePage identifica oferta/pedidos/compras/reportes aún no implementados.
- Extracción de services/http.ts, types/session.ts e iconos SVG NavIcon.
- CSS responsive: sidebar vertical escritorio, navegación horizontal móvil,
  tablas desplazables y formularios adaptados. Sin nueva dependencia.

## Evidencia

Primer build detectó paréntesis extra en media query CSS; corregido.
`npm --prefix frontend run build`: TypeScript/Vite exitosos, 38 módulos.
`docker compose -f compose.yaml -f compose.supabase.yaml -f compose.demo.yaml
up -d --build --no-deps frontend`: build/recreación frontend exitosos.
Backend y BD conservados; sin escrituras de negocio durante verificación.

Navegador: login Operaciones real, sidebar y resumen inspeccionados visualmente;
consultas reales mostraron 5 productos/5 activos y 3 proveedores/3 activos en ese
momento. Navegación a Productos cambia URL/hash, título y contenido de vista.
El navegador desconectó la pestaña durante la siguiente comprobación del formulario;
se dejó nueva pestaña de acceso. No se declara probado visualmente todo el circuito
de alta ni layout móvil. No se agregaron tests para la reorganización reversible.
Build y comprobaciones de interfaz no sustituyen tests backend previos.

## Límites y próximo paso

La referencia incluye importación de archivos y métricas de ventas/pedidos sin
backend disponible en este proyecto. No se copiaron como operaciones funcionales.
Gerencia/Cliente tienen navegación de su perfil y pantallas pendientes claras;
solo Operaciones consulta maestros. No cambios de permisos API ni SQL.
Sin commit/push, publicación o nueva dependencia. Revisión principal solamente.
Siguiente incremento: oferta semanal y luego pedido/State según reglas acordadas.
