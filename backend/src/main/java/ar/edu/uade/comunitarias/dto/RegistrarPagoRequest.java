package ar.edu.uade.comunitarias.dto;
import ar.edu.uade.comunitarias.domain.MedioPago;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
public record RegistrarPagoRequest(@NotNull @Positive Long facturaId,@NotNull UUID claveSolicitud,
        @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal importe,
        @NotNull MedioPago medioPago,@Size(max=100) String referencia) {}
