package ar.edu.uade.comunitarias.domain;

import jakarta.persistence.*;

/** Proyección de solo lectura para compras; no reserva ni mueve inventario. */
@Entity @Table(name="stock")
public class StockAbastecimiento {
    @Id private Long id;
    @Column(name="producto_id") private Long productoId;
    @Column(name="cantidad_disponible") private int cantidadDisponible;
    public Long getProductoId() { return productoId; }
    public int getCantidadDisponible() { return cantidadDisponible; }
}
