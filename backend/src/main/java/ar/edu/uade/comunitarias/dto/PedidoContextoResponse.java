package ar.edu.uade.comunitarias.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record PedidoContextoResponse(List<ClienteItem> clientes,List<OfertaItem> ofertas) {
    public record ClienteItem(Long id,String nombre) {}
    public record OfertaItem(Long id,String titulo,LocalDate fechaInicio,LocalDate fechaFin,List<ProductoItem> productos) {}
    public record ProductoItem(Long id,String nombre,BigDecimal precio) {}
}
