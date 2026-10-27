package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;

@Entity
@Table(name="productos")
public class Producto {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="nombre")
    private String nombre;
    @Column(name="descripcion")
    private String descripcion;
    @Column(name="categoria")
    private String categoria;
    @Column(name="unidad_medida")
    private String unidadMedida;
    @Column(name="activo")
    private boolean activo;
    protected Producto() {}
    public Producto(String nombre, String descripcion, String categoria, String unidadMedida) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.unidadMedida = unidadMedida;
        this.activo = true;
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public String getUnidadMedida() { return unidadMedida; }
    public boolean getActivo() { return activo; }
    public void cambiarActivo(boolean activo) { this.activo = activo; }
}
