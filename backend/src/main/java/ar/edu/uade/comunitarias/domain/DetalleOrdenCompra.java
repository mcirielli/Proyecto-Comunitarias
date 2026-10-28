package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="orden_compra_detalle")
public class DetalleOrdenCompra {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="orden_compra_id") private OrdenCompra orden;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="producto_id") private Producto producto;
    private int cantidad;
    @Column(name="precio_unitario") private BigDecimal precioUnitario;


    protected DetalleOrdenCompra() {}

    //Constructor
    DetalleOrdenCompra(OrdenCompra orden,
                       Producto producto,
                       int cantidad,
                       BigDecimal precio)

    {
        this.orden=orden;
        this.producto=producto;
        this.cantidad=cantidad;
        this.precioUnitario=precio;
    }

    //Getters
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal subtotal() { return precioUnitario.multiply(BigDecimal.valueOf(cantidad)); }
}
