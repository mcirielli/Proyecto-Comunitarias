package ar.edu.uade.comunitarias.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record CrearProveedorRequest(@NotBlank String razonSocial, String cuit,
                                    @Email String email, String telefono) {}
