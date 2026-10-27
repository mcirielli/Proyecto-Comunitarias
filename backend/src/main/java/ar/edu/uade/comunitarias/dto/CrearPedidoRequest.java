package ar.edu.uade.comunitarias.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record CrearPedidoRequest(@Positive Long clienteId, @NotNull @Positive Long ofertaId,
        @NotEmpty @Size(max=100) List<@NotNull @Valid Item> detalles) {
    public record Item(@NotNull @Positive Long productoId,@Min(1) int cantidad) {}
}
