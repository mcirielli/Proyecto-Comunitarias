# Temas claro/oscuro y revisión de negocio

Estado: CERRADO. Fecha: 27/10/2026 America/Buenos_Aires.

## Alcance

Usuario pidió comenzar a organizar frontend, paleta más oscura y selector de
modo claro/oscuro; preguntó qué falta en la lógica base del proyecto.
Se conservó la estructura de negocio y el esquema de Supabase.

## Cambios

index.css define tokens semánticos compartidos para canvas, superficies, texto,
bordes, acciones y estados. Oscuro: azul grafito y turquesa; claro: superficies
blancas y gris azulado con verde petróleo. ThemeToggle y main inicializan tema
antes de renderizar, oscuro por defecto; preferencia en localStorage con manejo
de almacenamiento bloqueado. Color-scheme aplica a controles nativos.
HomePage incorpora cabecera con marca/selector y diagnóstico HTTP plegable.
OperacionesPage/ApiStatus usan colores compartidos; formularios/listas/botones
adaptados a ambos temas. Sin librerías nuevas ni cambios en backend/API/BD.
Se anotó en MVP_PENDIENTES.md que sus conteos históricos quedaron desactualizados.

## Evidencia

- npm --prefix frontend run build: TypeScript/Vite exitosos, 33 módulos.
- Docker Compose con los tres archivos, up -d --build --no-deps frontend:
  frontend reconstruido y recreado; backend existente conservado.
- Navegador real: oscuro inicial; selector a claro; recarga conserva modo claro;
  vuelta a oscuro y login Operaciones real exitoso. Inspección visual de ambos
  temas en pantalla de acceso; sin escribir datos de negocio en esta ventana.
- git diff --check sin errores (avisos habituales LF/CRLF).
No se repitió suite Java por no modificar backend. Sin commit/push ni publicación.

## Negocio revisado

Código principal contiene maestros, autenticación y DTOs/repositorios asociados.
Maestros funcionales y permisos no equivalen a venta semanal completa. Faltan:
vínculo producto/proveedor e historial; oferta vigente/publicación/precio de venta;
pedidos y precios históricos con State real; compras por demanda y Quinteros con
Strategy; recepción, stock/reservas; facturas/pagos; totales gerenciales.
Modelo compartido no resuelve historial de proveedor; identidades demo negativas
requieren vínculo explícito a Cliente persistido antes de crear pedidos.
No se implementaron ni acordaron esas reglas en esta ventana.

Siguiente incremento recomendado: oferta real de Cliente y navegación por rol,
luego pedido/State. Antes de pedido, concretar cliente demo, validación de oferta,
precios históricos y momento de reserva. Mantener login demo y datos en Supabase.
