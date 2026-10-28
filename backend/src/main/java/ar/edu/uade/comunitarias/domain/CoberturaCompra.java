package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

/** Lote inmutable, incluso cuando no hay faltantes ni orden nueva. */
@Entity @Table(name="compras_coberturas")
public class CoberturaCompra {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="clave_solicitud") private UUID claveSolicitud;
    @Column(name="solicitada_por") private String solicitadaPor;
    private Instant fecha;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="orden_compra_id") private OrdenCompra orden;
    @ElementCollection @CollectionTable(name="compras_cobertura_pedidos", joinColumns=@JoinColumn(name="cobertura_id"))
    @Column(name="pedido_id") private Set<Long> pedidos = new HashSet<>();
    @ElementCollection @CollectionTable(name="compras_cobertura_detalles", joinColumns=@JoinColumn(name="cobertura_id"))
    private List<Asignacion> asignaciones = new ArrayList<>();
    protected CoberturaCompra() {}
    public CoberturaCompra(UUID clave, String actor, Instant fecha, Set<Long> pedidos, OrdenCompra orden, List<Asignacion> asignaciones) {
        this.claveSolicitud=clave; this.solicitadaPor=actor; this.fecha=fecha;
        this.pedidos.addAll(pedidos); this.orden=orden; this.asignaciones.addAll(asignaciones);
    }
    public Long getId() { return id; }
    public OrdenCompra getOrden() { return orden; }
    public List<Asignacion> getAsignaciones() { return List.copyOf(asignaciones); }
    @Embeddable
    public static class Asignacion {
        @Column(name="producto_id") private Long productoId;
        @Column(name="cantidad_stock") private int stock;
        @Column(name="cantidad_abierta") private int abierta;
        protected Asignacion() {}
        public Asignacion(Long productoId, int stock, int abierta) { this.productoId=productoId; this.stock=stock; this.abierta=abierta; }
        public Long getProductoId() { return productoId; }
        public int getStock() { return stock; }
        public int getAbierta() { return abierta; }
    }
}
