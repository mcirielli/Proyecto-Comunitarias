package ar.edu.uade.comunitarias.dto;
import ar.edu.uade.comunitarias.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
public record CompraResponse(Long id, Long proveedorId, String proveedor, TipoCompra tipo,
        EstadoCompra estado, Instant fecha, BigDecimal total, String solicitadaPor,
        String autorizadaPor, Instant fechaAutorizacion, String emitidaPor, Instant fechaEmision,
        List<Detalle> detalles) {
    public record Detalle(Long productoId, String producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}
    public static CompraResponse de(OrdenCompra o) {
        return new CompraResponse(o.getId(),o.getProveedor().getId(),o.getProveedor().getRazonSocial(),
            o.getTipo(),o.getEstado(),o.getFecha(),o.total(),o.getSolicitadaPor(),o.getAutorizadaPor(),
            o.getFechaAutorizacion(),o.getEmitidaPor(),o.getFechaEmision(),o.getDetalles().stream().map(d ->
                new Detalle(d.getProducto().getId(),d.getProducto().getNombre(),d.getCantidad(),d.getPrecioUnitario(),d.subtotal())).toList());
    }
}
