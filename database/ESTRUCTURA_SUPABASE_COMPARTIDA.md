## Table `usuarios`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `nombre` | `varchar` |  |
| `email` | `varchar` |  Unique |
| `rol` | `varchar` |  |
| `activo` | `bool` |  |

## Table `clientes`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `usuario_id` | `int8` |  Nullable Unique |
| `nombre_comercio` | `varchar` |  |
| `cuit` | `varchar` |  Nullable Unique |
| `direccion` | `varchar` |  Nullable |
| `telefono` | `varchar` |  Nullable |
| `activo` | `bool` |  |

## Table `productos`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `nombre` | `varchar` |  |
| `descripcion` | `text` |  Nullable |
| `categoria` | `varchar` |  Nullable |
| `unidad_medida` | `varchar` |  |
| `activo` | `bool` |  |

## Table `proveedores`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `razon_social` | `varchar` |  |
| `cuit` | `varchar` |  Nullable Unique |
| `email` | `varchar` |  Nullable |
| `telefono` | `varchar` |  Nullable |
| `activo` | `bool` |  |

## Table `producto_proveedor`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `producto_id` | `int8` |  |
| `proveedor_id` | `int8` |  |
| `codigo_proveedor` | `varchar` |  Nullable |
| `precio_compra` | `numeric` |  |
| `condiciones_comerciales` | `text` |  Nullable |

## Table `ofertas`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `titulo` | `varchar` |  |
| `fecha_inicio` | `date` |  |
| `fecha_fin` | `date` |  |
| `publicada` | `bool` |  |

## Table `oferta_detalle`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `oferta_id` | `int8` |  |
| `producto_id` | `int8` |  |
| `precio_venta` | `numeric` |  |

## Table `pedidos`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `cliente_id` | `int8` |  |
| `oferta_id` | `int8` |  Nullable |
| `fecha` | `timestamptz` |  |
| `estado` | `varchar` |  |
| `fecha_despacho` | `timestamptz` |  Nullable |
| `fecha_entrega` | `timestamptz` |  Nullable |

## Table `pedido_detalle`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `pedido_id` | `int8` |  |
| `producto_id` | `int8` |  |
| `cantidad` | `int4` |  |
| `precio_unitario` | `numeric` |  |

## Table `ordenes_compra`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `proveedor_id` | `int8` |  |
| `tipo` | `varchar` |  |
| `fecha` | `timestamptz` |  |
| `estado` | `varchar` |  |

## Table `orden_compra_detalle`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `orden_compra_id` | `int8` |  |
| `producto_id` | `int8` |  |
| `cantidad` | `int4` |  |
| `precio_unitario` | `numeric` |  |

## Table `stock`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `producto_id` | `int8` |  Unique |
| `cantidad_disponible` | `int4` |  |
| `cantidad_reservada` | `int4` |  |

## Table `movimientos_stock`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `producto_id` | `int8` |  |
| `orden_compra_id` | `int8` |  Nullable |
| `pedido_id` | `int8` |  Nullable |
| `tipo` | `varchar` |  |
| `cantidad` | `int4` |  |
| `fecha` | `timestamptz` |  |
| `observacion` | `text` |  Nullable |

## Table `facturas`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `pedido_id` | `int8` |  Unique |
| `numero` | `varchar` |  Unique |
| `fecha_emision` | `timestamptz` |  |
| `importe_total` | `numeric` |  |
| `estado` | `varchar` |  |

## Table `pagos`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `factura_id` | `int8` |  |
| `fecha_pago` | `timestamptz` |  |
| `importe` | `numeric` |  |
| `medio_pago` | `varchar` |  |
| `referencia` | `varchar` |  Nullable |
