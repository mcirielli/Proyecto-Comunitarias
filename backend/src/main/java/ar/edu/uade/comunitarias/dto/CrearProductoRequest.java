package ar.edu.uade.comunitarias.dto;
import jakarta.validation.constraints.NotBlank;
public record CrearProductoRequest(@NotBlank String nombre, String descripcion,
                                   String categoria, @NotBlank String unidadMedida) {}
