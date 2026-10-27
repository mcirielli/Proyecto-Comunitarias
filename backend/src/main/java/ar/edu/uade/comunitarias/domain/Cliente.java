package ar.edu.uade.comunitarias.domain;
import jakarta.persistence.*;
@Entity @Table(name="clientes")
public class Cliente {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="usuario_id") private Long usuarioId;
    @Column(name="nombre_comercio") private String nombreComercio;
    private boolean activo;
    protected Cliente() {}
    public Cliente(String nombreComercio) { this.nombreComercio=nombreComercio; activo=true; }
    public Long getId() { return id; }
    public String getNombreComercio() { return nombreComercio; }
    public boolean isActivo() { return activo; }
}
