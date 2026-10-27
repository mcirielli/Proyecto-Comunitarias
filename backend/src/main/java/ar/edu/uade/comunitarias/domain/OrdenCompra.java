package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity @Table(name="ordenes_compra")
public class OrdenCompra {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="proveedor_id") private Proveedor proveedor;
    @Enumerated(EnumType.STRING) private TipoCompra tipo;
    private Instant fecha;
    @Enumerated(EnumType.STRING) private EstadoCompra estado;
    @Column(name="clave_solicitud") private UUID claveSolicitud;
    // Texto del principal autenticado: también admite cuentas demo sin FK ficticia a usuarios.
    @Column(name="solicitada_por") private String solicitadaPor;
    @Column(name="autorizada_por") private String autorizadaPor;
    @Column(name="fecha_autorizacion") private Instant fechaAutorizacion;
    @Column(name="emitida_por") private String emitidaPor;
    @Column(name="fecha_emision") private Instant fechaEmision;
    @OneToMany(mappedBy="orden", cascade=CascadeType.ALL) private List<DetalleOrdenCompra> detalles = new ArrayList<>();
    protected OrdenCompra() {}
    public OrdenCompra(Proveedor proveedor, TipoCompra tipo, UUID clave, String actor, Instant ahora) {
        this.proveedor=Objects.requireNonNull(proveedor); this.tipo=Objects.requireNonNull(tipo);
        this.claveSolicitud=Objects.requireNonNull(clave); this.solicitadaPor=Objects.requireNonNull(actor);
        fecha=ahora;
        estado=tipo==TipoCompra.STOCK_QUINTEROS ? EstadoCompra.PENDIENTE_AUTORIZACION : EstadoCompra.EMITIDA;
        if (estado==EstadoCompra.EMITIDA) { emitidaPor=actor; fechaEmision=ahora; }
    }
    public void agregar(Producto producto, int cantidad, BigDecimal precio) {
        if (cantidad<=0 || precio==null || precio.signum()<0) throw new IllegalArgumentException("Detalle inválido");
        if(detalles.stream().anyMatch(d -> Objects.equals(d.getProducto().getId(),producto.getId())))
            throw new IllegalArgumentException("Producto repetido");
        detalles.add(new DetalleOrdenCompra(this,producto,cantidad,precio));
    }
    public void autorizar(String actor, Instant ahora) {
        if (tipo!=TipoCompra.STOCK_QUINTEROS || estado!=EstadoCompra.PENDIENTE_AUTORIZACION)
            throw new ReglaNegocioException("La compra no está pendiente de autorización");
        estado=EstadoCompra.AUTORIZADA; autorizadaPor=actor; fechaAutorizacion=ahora;
    }
    public void emitir(String actor, Instant ahora) {
        if (tipo!=TipoCompra.STOCK_QUINTEROS || estado!=EstadoCompra.AUTORIZADA)
            throw new ReglaNegocioException("La compra requiere autorización previa de Gerencia");
        estado=EstadoCompra.EMITIDA; emitidaPor=actor; fechaEmision=ahora;
    }
    public Long getId() { return id; }
    public Proveedor getProveedor() { return proveedor; }
    public TipoCompra getTipo() { return tipo; }
    public Instant getFecha() { return fecha; }
    public EstadoCompra getEstado() { return estado; }
    public String getSolicitadaPor() { return solicitadaPor; }
    public String getAutorizadaPor() { return autorizadaPor; }
    public Instant getFechaAutorizacion() { return fechaAutorizacion; }
    public String getEmitidaPor() { return emitidaPor; }
    public Instant getFechaEmision() { return fechaEmision; }
    public List<DetalleOrdenCompra> getDetalles() { return Collections.unmodifiableList(detalles); }
    public BigDecimal total() { return detalles.stream().map(DetalleOrdenCompra::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
}
