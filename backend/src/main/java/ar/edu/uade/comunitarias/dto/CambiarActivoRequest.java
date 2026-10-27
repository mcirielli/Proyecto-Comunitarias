package ar.edu.uade.comunitarias.dto;
import jakarta.validation.constraints.NotNull;
public record CambiarActivoRequest(@NotNull Boolean activo) {}
