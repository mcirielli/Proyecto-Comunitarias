package ar.edu.uade.comunitarias.dto;
import ar.edu.uade.comunitarias.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record FacturaResponse(Long id,Long pedidoId,String numero,String cliente,Instant fechaEmision,
        BigDecimal importeTotal,EstadoFactura estado,BigDecimal pagado,BigDecimal saldo,List<Detalle> detalles,List<PagoResponse> pagos) {
    public record Detalle(Long productoId,String producto,int cantidad,BigDecimal precioUnitario,BigDecimal subtotal) {}
    public static FacturaResponse de(Factura f,List<Pago> pagos) {
        var pagado=pagos.stream().map(Pago::getImporte).reduce(BigDecimal.ZERO,BigDecimal::add);
        return new FacturaResponse(f.getId(),f.getPedido().getId(),f.getNumero(),f.getPedido().getCliente().getNombreComercio(),f.getFechaEmision(),f.getImporteTotal(),f.getEstado(),pagado,f.getImporteTotal().subtract(pagado),
            f.getPedido().getDetalles().stream().map(d -> new Detalle(d.getProducto().getId(),d.getProducto().getNombre(),d.getCantidad(),d.getPrecioUnitario(),d.subtotal())).toList(),pagos.stream().map(PagoResponse::de).toList());
    }
}
