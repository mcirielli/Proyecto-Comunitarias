package ar.edu.uade.comunitarias.dto;

import java.math.BigDecimal;
import java.util.List;

/** Historial de precios de un producto, armado con los registros existentes de ofertas y órdenes de compra. */
public record HistorialPreciosResponse(Long productoId, String producto, String unidadMedida,
                                       BigDecimal precioVentaVigente, BigDecimal ultimoPrecioCompra,
                                       List<PrecioVentaItem> ventas, List<PrecioCompraItem> compras,
                                       List<PrecioProveedorItem> proveedores) {}
