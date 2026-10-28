package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;

/** Única autoridad de escritura del saldo. Disponible ya excluye reservado. */
@Entity @Table(name="stock")
public class Stock {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="producto_id",nullable=false,unique=true) private Long productoId;
    @Column(name="cantidad_disponible") private int disponible;
    @Column(name="cantidad_reservada") private int reservado;
    protected Stock() {}
    public Stock(Long productoId) { this.productoId=java.util.Objects.requireNonNull(productoId); }
    public void ingresar(int cantidad) {
        positiva(cantidad);
        int nuevo=Math.addExact(disponible,cantidad);
        Math.addExact(nuevo,reservado);
        disponible=nuevo;
    }
    public void reservar(int cantidad) {
        positiva(cantidad);
        if(disponible<cantidad) throw new ReglaNegocioException("Stock libre insuficiente");
        int nuevo=Math.addExact(reservado,cantidad);
        disponible-=cantidad; reservado=nuevo;
    }
    public void consumir(int cantidad) {
        positiva(cantidad);
        if(reservado<cantidad) throw new ReglaNegocioException("Reserva insuficiente");
        reservado-=cantidad;
    }
    private static void positiva(int cantidad) { if(cantidad<=0) throw new IllegalArgumentException("Cantidad positiva requerida"); }
    public Long getProductoId() { return productoId; }
    public int getDisponible() { return disponible; }
    public int getReservado() { return reservado; }
    public int existencia() { return Math.addExact(disponible,reservado); }
}
