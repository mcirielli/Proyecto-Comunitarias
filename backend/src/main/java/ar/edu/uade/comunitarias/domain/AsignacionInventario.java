package ar.edu.uade.comunitarias.domain;
import jakarta.persistence.*;
import java.time.Instant;
/** Conserva origen y cantidades; las fechas de cada transferencia preservan auditoría. */
@Entity @Table(name="inventario_asignaciones")
public class AsignacionInventario {
    public enum Fase { ABIERTA, FISICA, RESERVADA, CONSUMIDA }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="cobertura_id") private Long coberturaId;
    @Column(name="pedido_id") private Long pedidoId;
    @Column(name="producto_id") private Long productoId;
    @Column(name="orden_compra_id") private Long ordenId;
    private int cantidad;
    @Enumerated(EnumType.STRING) private Fase fase;
    @Column(name="fecha_asignacion") private Instant fechaAsignacion;
    @Column(name="fecha_recepcion") private Instant fechaRecepcion;
    @Column(name="fecha_reserva") private Instant fechaReserva;
    @Column(name="fecha_consumo") private Instant fechaConsumo;
    protected AsignacionInventario() {}
    public AsignacionInventario(Long cobertura,Long pedido,Long producto,Long orden,int cantidad,Instant fecha) {
        if(cantidad<=0)throw new IllegalArgumentException("Cantidad inválida");
        coberturaId=java.util.Objects.requireNonNull(cobertura);pedidoId=java.util.Objects.requireNonNull(pedido);
        productoId=java.util.Objects.requireNonNull(producto);ordenId=orden;this.cantidad=cantidad;
        fechaAsignacion=java.util.Objects.requireNonNull(fecha);fase=orden==null?Fase.FISICA:Fase.ABIERTA;
    }
    public void recibir(Instant fecha){exigir(Fase.ABIERTA);fase=Fase.FISICA;fechaRecepcion=fecha;}
    public void reservar(Instant fecha){exigir(Fase.FISICA);fase=Fase.RESERVADA;fechaReserva=fecha;}
    public void consumir(Instant fecha){exigir(Fase.RESERVADA);fase=Fase.CONSUMIDA;fechaConsumo=fecha;}
    private void exigir(Fase esperada){if(fase!=esperada)throw new ReglaNegocioException("Fase de cobertura incompatible");}
    public Long getPedidoId(){return pedidoId;}
    public Long getProductoId(){return productoId;}
    public Long getOrdenId(){return ordenId;}
    public int getCantidad(){return cantidad;}
    public Fase getFase(){return fase;}
}
