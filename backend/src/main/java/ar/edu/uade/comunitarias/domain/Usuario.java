package ar.edu.uade.comunitarias.domain;
import jakarta.persistence.*;
@Entity
@Table(name="usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String nombre;
    private String email;
    private String rol;
    private boolean activo;
    @Column(name="password_hash") private String passwordHash;
    protected Usuario() {}
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public String getPasswordHash() { return passwordHash; }
}
