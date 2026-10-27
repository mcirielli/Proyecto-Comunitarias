package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;

@Entity
@Table(name="proveedores")
public class Proveedor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="razon_social")
    private String razonSocial;
    @Column(name="cuit")
    private String cuit;
    @Column(name="email")
    private String email;
    @Column(name="telefono")
    private String telefono;
    @Column(name="activo")
    private boolean activo;
    protected Proveedor() {}
    public Proveedor(String razonSocial, String cuit, String email, String telefono) {
        this.razonSocial = razonSocial;
        this.cuit = cuit;
        this.email = email;
        this.telefono = telefono;
        this.activo = true;
    }
    public Long getId() { return id; }
    public String getRazonSocial() { return razonSocial; }
    public String getCuit() { return cuit; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public boolean getActivo() { return activo; }
    public void cambiarActivo(boolean activo) { this.activo = activo; }
}
