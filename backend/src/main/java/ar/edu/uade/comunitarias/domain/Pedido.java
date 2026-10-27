package ar.edu.uade.comunitarias.domain;
import ar.edu.uade.comunitarias.state.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import java.math.BigDecimal;
@Entity @Table(name="pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="cliente_id") private Cliente cliente;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="oferta_id") private Oferta oferta;
    private Instant fecha;
    @Enumerated(EnumType.STRING) private EstadoPedido estado;
    @Column(name="fecha_despacho") private Instant fechaDespacho;
    @Column(name="fecha_entrega") private Instant fechaEntrega;
    @OneToMany(mappedBy="pedido", cascade=CascadeType.ALL) private List<DetallePedido> detalles=new ArrayList<>();
    protected Pedido() {}
    public Pedido(Cliente cliente, Oferta oferta, Instant fecha) {
        this.cliente=Objects.requireNonNull(cliente); this.oferta=Objects.requireNonNull(oferta);
        this.fecha=Objects.requireNonNull(fecha); this.estado=EstadoPedido.RECIBIDO;
    }
    public void agregar(Producto producto,int cantidad,BigDecimal precio) {
        if(estado!=EstadoPedido.RECIBIDO) throw new IllegalStateException("El pedido ya fue confirmado");
        if(cantidad<=0 || precio==null || precio.signum()<0) throw new IllegalArgumentException("Detalle inválido");
        if(detalles.stream().anyMatch(d -> Objects.equals(d.getProducto().getId(),producto.getId()))) throw new IllegalArgumentException("Producto repetido");
        detalles.add(new DetallePedido(this,producto,cantidad,precio));
    }
    public void cambiarEstado(EstadoPedido destino,Instant ahora) {
        Objects.requireNonNull(ahora);
        PedidoState nuevo=state().cambiarA(destino);
        estado=nuevo.codigo();
        if(estado==EstadoPedido.DESPACHADO) fechaDespacho=ahora;
        if(estado==EstadoPedido.ENTREGADO) fechaEntrega=ahora;
    }
    public PedidoState state() { return PedidoStates.reconstruir(estado); }
    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Oferta getOferta() { return oferta; }
    public Instant getFecha() { return fecha; }
    public EstadoPedido getEstado() { return estado; }
    public Instant getFechaDespacho() { return fechaDespacho; }
    public Instant getFechaEntrega() { return fechaEntrega; }
    public List<DetallePedido> getDetalles() { return Collections.unmodifiableList(detalles); }
    public BigDecimal total() { return detalles.stream().map(DetallePedido::subtotal).reduce(BigDecimal.ZERO,BigDecimal::add); }
}
