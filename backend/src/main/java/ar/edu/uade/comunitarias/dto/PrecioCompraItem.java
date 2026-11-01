package ar.edu.uade.comunitarias.dto;

import ar.edu.uade.comunitarias.domain.EstadoCompra;
import ar.edu.uade.comunitarias.domain.TipoCompra;
import java.math.BigDecimal;
import java.time.Instant;

/** Precio de compra pactado en una orden de compra. Variación en % respecto de la compra anterior del producto. */
public record PrecioCompraItem(Long ordenId, Instant fecha, String proveedor, EstadoCompra estado, TipoCompra tipo,
                               Integer cantidad, BigDecimal precio, BigDecimal variacion) {
    /** Usado por la consulta JPQL; la variación se calcula en HistorialPreciosService. */
    public PrecioCompraItem(Long ordenId, Instant fecha, String proveedor, EstadoCompra estado, TipoCompra tipo,
                            Integer cantidad, BigDecimal precio) {
        this(ordenId, fecha, proveedor, estado, tipo, cantidad, precio, null);
    }
    public PrecioCompraItem conVariacion(BigDecimal valor) {
        return new PrecioCompraItem(ordenId, fecha, proveedor, estado, tipo, cantidad, precio, valor);
    }
}
