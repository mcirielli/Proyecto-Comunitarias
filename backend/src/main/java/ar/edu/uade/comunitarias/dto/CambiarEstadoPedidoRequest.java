package ar.edu.uade.comunitarias.dto;
import jakarta.validation.constraints.NotNull;
import ar.edu.uade.comunitarias.state.EstadoPedido;
public record CambiarEstadoPedidoRequest(@NotNull EstadoPedido estado) {}
