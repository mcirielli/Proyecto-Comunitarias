package ar.edu.uade.comunitarias.domain;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;
@Entity @Table(name="ofertas")
public class Oferta {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String titulo;
    @Column(name="fecha_inicio") private LocalDate fechaInicio;
    @Column(name="fecha_fin") private LocalDate fechaFin;
    private boolean publicada;
    @OneToMany(mappedBy="oferta", cascade=CascadeType.ALL) private List<DetalleOferta> detalles=new ArrayList<>();
    protected Oferta() {}
    public Oferta(String titulo, LocalDate inicio, LocalDate fin) { this.titulo=titulo; fechaInicio=inicio; fechaFin=fin; publicada=true; }
    public void agregar(Producto producto, java.math.BigDecimal precio) { detalles.add(new DetalleOferta(this,producto,precio)); }
    public boolean vigente(LocalDate fecha) { return publicada && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin); }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public List<DetalleOferta> getDetalles() { return Collections.unmodifiableList(detalles); }
}
