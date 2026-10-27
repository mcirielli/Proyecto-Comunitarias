package ar.edu.uade.comunitarias.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="pedido_detalle")
public class DetallePedido {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pedido_id") private Pedido pedido;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="producto_id") private Producto producto;
    private int cantidad;
    @Column(name="precio_unitario") private BigDecimal precioUnitario;
    protected DetallePedido() {}
    public DetallePedido(Pedido pedido,Producto producto,int cantidad,BigDecimal precio) { this.pedido=pedido; this.producto=producto; this.cantidad=cantidad;precioUnitario=precio; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal subtotal() { return precioUnitario.multiply(BigDecimal.valueOf(cantidad)); }
}
