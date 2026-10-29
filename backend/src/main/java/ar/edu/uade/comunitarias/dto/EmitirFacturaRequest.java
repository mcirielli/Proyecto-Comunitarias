package ar.edu.uade.comunitarias.dto;
import jakarta.validation.constraints.*;
public record EmitirFacturaRequest(@NotNull @Positive Long pedidoId) {}
