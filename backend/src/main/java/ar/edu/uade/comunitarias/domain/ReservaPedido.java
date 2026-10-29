package ar.edu.uade.comunitarias.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="reservas_pedido",uniqueConstraints=@UniqueConstraint(columnNames={"pedido_id","producto_id"}))
public class ReservaPedido {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="pedido_id") private Long pedidoId;
    @Column(name="producto_id") private Long productoId;
    private int cantidad;
    private boolean consumida;
    private Instant fecha;
    private String actor;
    @Column(name="fecha_consumo") private Instant fechaConsumo;
    protected ReservaPedido() {}
    public ReservaPedido(Long pedidoId,Long productoId,int cantidad,Instant fecha,String actor) {
        if(cantidad<=0)throw new IllegalArgumentException("Cantidad inválida");
        this.pedidoId=java.util.Objects.requireNonNull(pedidoId);this.productoId=java.util.Objects.requireNonNull(productoId);
        this.cantidad=cantidad;this.fecha=java.util.Objects.requireNonNull(fecha);this.actor=java.util.Objects.requireNonNull(actor);
    }
    public void consumir(Instant ahora) {
        if(consumida)throw new ReglaNegocioException("Reserva ya consumida");
        consumida=true;fechaConsumo=java.util.Objects.requireNonNull(ahora);
    }
    public Long getProductoId(){return productoId;}
    public int getCantidad(){return cantidad;}
    public boolean isConsumida(){return consumida;}
}
