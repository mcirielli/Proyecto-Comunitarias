package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import ar.edu.uade.comunitarias.state.EstadoPedido;

@Entity @Table(name="facturas")
public class Factura {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="pedido_id") private Pedido pedido;
    private String numero;
    @Column(name="fecha_emision") private Instant fechaEmision;
    @Column(name="importe_total") private BigDecimal importeTotal;
    @Enumerated(EnumType.STRING) private EstadoFactura estado;
    protected Factura() {}
    public Factura(Pedido pedido, Instant ahora) {
        this.pedido=Objects.requireNonNull(pedido);
        if(pedido.getEstado()!=EstadoPedido.ENTREGADO) throw new ReglaNegocioException("Solo se facturan pedidos entregados");
        if(pedido.getId()==null || pedido.getDetalles().isEmpty()) throw new ReglaNegocioException("Pedido sin detalles persistidos");
        importeTotal=pedido.total(); validarImporte(importeTotal,false);
        numero="INT-PED-"+pedido.getId(); fechaEmision=Objects.requireNonNull(ahora);
        estado=importeTotal.signum()==0 ? EstadoFactura.PAGADA : EstadoFactura.PENDIENTE;
    }
    public static void validarImporte(BigDecimal importe,boolean positivo) {
        if(importe==null || (positivo ? importe.signum()<=0 : importe.signum()<0)
                || importe.stripTrailingZeros().scale()>2 || importe.compareTo(new BigDecimal("9999999999.99"))>0)
            throw new IllegalArgumentException("Importe inválido para numeric(12,2)");
    }
    public void registrarAcumulado(BigDecimal pagado) {
        if(estado==EstadoFactura.ANULADA) throw new ReglaNegocioException("Factura anulada");
        if(pagado.signum()<0 || pagado.compareTo(importeTotal)>0) throw new ReglaNegocioException("El pago supera el saldo de la factura");
        estado=pagado.compareTo(importeTotal)==0 ? EstadoFactura.PAGADA : EstadoFactura.PENDIENTE;
    }
    public Long getId(){return id;}
    public Pedido getPedido(){return pedido;}
    public String getNumero(){return numero;}
    public Instant getFechaEmision(){return fechaEmision;}
    public BigDecimal getImporteTotal(){return importeTotal;}
    public EstadoFactura getEstado(){return estado;}
}
