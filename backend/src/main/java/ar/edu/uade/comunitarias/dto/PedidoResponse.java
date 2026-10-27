package ar.edu.uade.comunitarias.dto;
import ar.edu.uade.comunitarias.domain.Pedido;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record PedidoResponse(Long id,Long clienteId,String cliente,Long ofertaId,Instant fecha,
        EstadoPedido estado,EstadoPedido siguienteEstado,Instant fechaDespacho,Instant fechaEntrega,
        BigDecimal total,List<Item> detalles) {
    public record Item(Long productoId,String producto,int cantidad,BigDecimal precioUnitario,BigDecimal subtotal) {}
    public static PedidoResponse from(Pedido p) {
        var siguiente=p.state().siguiente();
        return new PedidoResponse(p.getId(),p.getCliente().getId(),p.getCliente().getNombreComercio(),p.getOferta()==null?null:p.getOferta().getId(),p.getFecha(),p.getEstado(),siguiente==null?null:siguiente.codigo(),p.getFechaDespacho(),p.getFechaEntrega(),p.total(),p.getDetalles().stream().map(d -> new Item(d.getProducto().getId(),d.getProducto().getNombre(),d.getCantidad(),d.getPrecioUnitario(),d.subtotal())).toList());
    }
}
