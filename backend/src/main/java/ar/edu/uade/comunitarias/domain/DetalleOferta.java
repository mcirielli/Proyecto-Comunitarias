package ar.edu.uade.comunitarias.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="oferta_detalle")

public class DetalleOferta {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="oferta_id") private Oferta oferta;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="producto_id") private Producto producto;
    @Column(name="precio_venta") private BigDecimal precioVenta;

    protected DetalleOferta() {}


    //Constructor
    public DetalleOferta(Oferta oferta,
                         Producto producto,
                         BigDecimal precio)

    {
        this.oferta=oferta;
        this.producto=producto;
        precioVenta=precio;
    }

    //getters
    public Producto getProducto() { return producto; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
}
