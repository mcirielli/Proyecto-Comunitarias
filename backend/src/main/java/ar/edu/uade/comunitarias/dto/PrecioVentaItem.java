package ar.edu.uade.comunitarias.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Precio de venta de un producto en una oferta semanal publicada. Variación en % respecto de la oferta anterior. */
public record PrecioVentaItem(Long ofertaId, String oferta, LocalDate desde, LocalDate hasta, BigDecimal precio, BigDecimal variacion) {
    /** Usado por la consulta JPQL; la variación se calcula en HistorialPreciosService. */
    public PrecioVentaItem(Long ofertaId, String oferta, LocalDate desde, LocalDate hasta, BigDecimal precio) {
        this(ofertaId, oferta, desde, hasta, precio, null);
    }
    public PrecioVentaItem conVariacion(BigDecimal valor) { return new PrecioVentaItem(ofertaId, oferta, desde, hasta, precio, valor); }
}
