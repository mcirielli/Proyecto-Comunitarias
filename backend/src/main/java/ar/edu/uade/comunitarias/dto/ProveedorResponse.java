package ar.edu.uade.comunitarias.dto;
import ar.edu.uade.comunitarias.domain.Proveedor;
public record ProveedorResponse(Long id, String razonSocial, String cuit, String email, String telefono, boolean activo) {
    public static ProveedorResponse from(Proveedor entity) {
        return new ProveedorResponse(entity.getId(), entity.getRazonSocial(), entity.getCuit(), entity.getEmail(), entity.getTelefono(), entity.getActivo());
    }
}
