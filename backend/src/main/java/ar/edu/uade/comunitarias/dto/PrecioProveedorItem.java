package ar.edu.uade.comunitarias.dto;

import java.math.BigDecimal;

/** Precio de compra actual registrado en el vínculo producto-proveedor. */
public record PrecioProveedorItem(Long proveedorId, String proveedor, BigDecimal precioCompra) {}
