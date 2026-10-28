package ar.edu.uade.comunitarias.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="inventario_recepciones")
public class RecepcionCompra {
    @Id @Column(name="orden_compra_id") private Long ordenId;
    private String actor;
    private Instant fecha;
    protected RecepcionCompra(){}
    public RecepcionCompra(Long ordenId,String actor,Instant fecha){this.ordenId=java.util.Objects.requireNonNull(ordenId);this.actor=java.util.Objects.requireNonNull(actor);this.fecha=java.util.Objects.requireNonNull(fecha);}
    public Long getOrdenId(){return ordenId;}
    public String getActor(){return actor;}
    public Instant getFecha(){return fecha;}
}
