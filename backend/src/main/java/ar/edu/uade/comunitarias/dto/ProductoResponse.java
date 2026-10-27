package ar.edu.uade.comunitarias.dto;
import ar.edu.uade.comunitarias.domain.Producto;
public record ProductoResponse(Long id, String nombre, String descripcion, String categoria, String unidadMedida, boolean activo) {
    public static ProductoResponse from(Producto entity) {
        return new ProductoResponse(entity.getId(), entity.getNombre(), entity.getDescripcion(), entity.getCategoria(), entity.getUnidadMedida(), entity.getActivo());
    }
}
