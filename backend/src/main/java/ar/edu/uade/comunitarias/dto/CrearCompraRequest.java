package ar.edu.uade.comunitarias.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
public record CrearCompraRequest(@NotNull UUID claveSolicitud, @NotNull @Positive Long proveedorId,
        @NotNull @Size(max=100) List<@NotNull @Positive Long> pedidos,
        @NotNull @Size(max=100) List<@NotNull @Valid Linea> productos) {
    public record Linea(@NotNull @Positive Long productoId, @Min(1) int cantidad) {}
}
