package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity @Table(name="movimientos_stock")
public class MovimientoStock {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="producto_id") private Long productoId;
    @Column(name="orden_compra_id") private Long ordenId;
    @Column(name="pedido_id") private Long pedidoId;
    private String tipo;
    private int cantidad;
    private Instant fecha;
    private String observacion;
    protected MovimientoStock() {}
    public MovimientoStock(Long productoId,Long ordenId,Long pedidoId,String tipo,int cantidad,Instant fecha,String actor) {
        if(cantidad<=0 || (ordenId==null)==(pedidoId==null)) throw new IllegalArgumentException("Movimiento inválido");
        if(!java.util.Set.of("INGRESO","RESERVA","EGRESO").contains(tipo)) throw new IllegalArgumentException("Tipo inválido");
        this.productoId=Objects.requireNonNull(productoId);this.ordenId=ordenId;this.pedidoId=pedidoId;
        this.tipo=tipo;this.cantidad=cantidad;this.fecha=Objects.requireNonNull(fecha);this.observacion=Objects.requireNonNull(actor);
    }
    public Long getId(){return id;}
    public Long getProductoId(){return productoId;}
    public Long getOrdenId(){return ordenId;}
    public Long getPedidoId(){return pedidoId;}
    public String getTipo(){return tipo;}
    public int getCantidad(){return cantidad;}
    public Instant getFecha(){return fecha;}
}
