package ar.edu.uade.comunitarias.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Solo datos de venta; nunca entidades ni costos de abastecimiento. */
public record OfertaResponse(Long id, String titulo, LocalDate fechaInicio, LocalDate fechaFin,
        boolean publicada, String estado, List<Item> productos) {
    public record Item(Long id, String nombre, String unidadMedida, BigDecimal precioVenta, boolean activo) {}
}
