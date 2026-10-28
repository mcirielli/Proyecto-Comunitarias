package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Lectura del vínculo y precio actual; no administra el historial de proveedores. */
@Entity @Table(name="producto_proveedor")
public class ProductoProveedor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="producto_id") private Producto producto;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="proveedor_id") private Proveedor proveedor;
    @Column(name="precio_compra") private BigDecimal precioCompra;
    protected ProductoProveedor() {}
    public Producto getProducto() { return producto; }
    public Proveedor getProveedor() { return proveedor; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
}
