package ar.edu.uade.comunitarias.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CrearOfertaRequest(
        @NotBlank @Size(max=255) String titulo,
        @NotNull LocalDate fechaInicio,
        @NotNull LocalDate fechaFin,
        @NotEmpty @Size(max=100) List<@NotNull @Valid Item> detalles) {
    public record Item(@NotNull @Positive Long productoId, @NotNull @DecimalMin("0") @Digits(integer=10, fraction=2) BigDecimal precioVenta) {}
}
