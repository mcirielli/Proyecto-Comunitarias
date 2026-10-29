package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity @Table(name="pagos")
public class Pago {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="factura_id") private Factura factura;
    @Column(name="clave_solicitud") private UUID claveSolicitud;
    @Column(name="fecha_pago") private Instant fechaPago;
    private BigDecimal importe;
    @Enumerated(EnumType.STRING) @Column(name="medio_pago") private MedioPago medioPago;
    private String referencia;
    protected Pago() {}
    public Pago(Factura factura,UUID clave,BigDecimal importe,MedioPago medio,String referencia,Instant ahora) {
        Factura.validarImporte(importe,true);
        if(referencia!=null && referencia.length()>100) throw new IllegalArgumentException("Referencia demasiado larga");
        this.factura=Objects.requireNonNull(factura);claveSolicitud=Objects.requireNonNull(clave);
        this.importe=importe;medioPago=Objects.requireNonNull(medio);this.referencia=normalizar(referencia);fechaPago=Objects.requireNonNull(ahora);
    }
    public static String normalizar(String value){return value==null || value.isBlank() ? null : value.trim();}
    public boolean coincide(Long facturaId,BigDecimal valor,MedioPago medio,String ref) {
        return factura.getId().equals(facturaId) && importe.compareTo(valor)==0 && medioPago==medio && Objects.equals(referencia,normalizar(ref));
    }
    public Long getId(){return id;}
    public Factura getFactura(){return factura;}
    public UUID getClaveSolicitud(){return claveSolicitud;}
    public Instant getFechaPago(){return fechaPago;}
    public BigDecimal getImporte(){return importe;}
    public MedioPago getMedioPago(){return medioPago;}
    public String getReferencia(){return referencia;}
}
