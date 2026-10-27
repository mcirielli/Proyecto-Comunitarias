package ar.edu.uade.comunitarias.service;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import java.time.*;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Profile("supabase") @Transactional(readOnly=true)
public class OfertaService {
    private final OfertaRepository ofertas;
    private final ProductoRepository productos;
    private final Clock clock;

    public OfertaService(OfertaRepository ofertas, ProductoRepository productos, Clock clock) {
        this.ofertas = ofertas;
        this.productos = productos;
        this.clock = clock;
    }

    private LocalDate hoy() { return LocalDate.now(clock.withZone(ZoneId.of("America/Argentina/Buenos_Aires"))); }

    public List<OfertaResponse> listar(boolean operaciones) {
        LocalDate fecha = hoy();
        return ofertas.findAll().stream()
                .filter(o -> operaciones || o.vigente(fecha))
                .sorted(Comparator.comparing(Oferta::getId).reversed())
                .map(o -> respuesta(o, operaciones, fecha)).toList();
    }

    @Transactional
    public OfertaResponse crear(CrearOfertaRequest request) {
        validarCabecera(request.titulo(), request.fechaInicio(), request.fechaFin());
        if (request.detalles() == null || request.detalles().isEmpty() || request.detalles().size() > 100)
            throw new IllegalArgumentException("La oferta requiere entre 1 y 100 productos");
        var oferta = Oferta.borrador(request.titulo().trim(), request.fechaInicio(), request.fechaFin());
        var ids = new HashSet<Long>();
        for (var item : request.detalles()) {
            if (item == null || item.productoId() == null || !ids.add(item.productoId()))
                throw new IllegalArgumentException("Productos vacíos o repetidos");
            validarPrecio(item.precioVenta());
            var producto = productos.findById(item.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto inexistente"));
            if (!producto.getActivo()) throw new ReglaNegocioException("Solo se pueden ofrecer productos activos");
            oferta.agregar(producto, item.precioVenta());
        }
        return respuesta(ofertas.saveAndFlush(oferta), true, hoy());
    }

    @Transactional
    public OfertaResponse publicar(Long id) {
        var oferta = ofertas.buscarParaPublicar(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Oferta inexistente"));
        if (oferta.isPublicada()) return respuesta(oferta, true, hoy());
        validarCabecera(oferta.getTitulo(), oferta.getFechaInicio(), oferta.getFechaFin());
        if (oferta.getDetalles().isEmpty()) throw new ReglaNegocioException("No se puede publicar una oferta sin productos");
        var ids = new HashSet<Long>();
        for (var detalle : oferta.getDetalles()) {
            if (!ids.add(detalle.getProducto().getId())) throw new ReglaNegocioException("Productos repetidos");
            if (!detalle.getProducto().getActivo()) throw new ReglaNegocioException("La oferta contiene productos inactivos");
            validarPrecio(detalle.getPrecioVenta());
        }
        oferta.publicar();
        return respuesta(ofertas.saveAndFlush(oferta), true, hoy());
    }

    private void validarCabecera(String titulo, LocalDate inicio, LocalDate fin) {
        if (titulo == null || titulo.isBlank() || titulo.trim().length() > 255)
            throw new IllegalArgumentException("Indique un título de hasta 255 caracteres");
        if (inicio == null || fin == null || fin.isBefore(inicio))
            throw new IllegalArgumentException("La fecha fin debe ser igual o posterior al inicio");
    }

    private void validarPrecio(java.math.BigDecimal precio) {
        // Se conserva precio >= 0 del contrato de pedidos y CHECK del esquema existente.
        if (precio == null || precio.signum() < 0) throw new IllegalArgumentException("El precio de venta debe ser mayor o igual a cero");
        if (precio.stripTrailingZeros().scale() > 2 || precio.compareTo(new java.math.BigDecimal("9999999999.99")) > 0)
            throw new IllegalArgumentException("El precio admite hasta 10 enteros y 2 decimales, según el esquema existente");
    }

    private OfertaResponse respuesta(Oferta oferta, boolean operaciones, LocalDate fecha) {
        String estado = !oferta.isPublicada() ? "BORRADOR" : fecha.isBefore(oferta.getFechaInicio()) ? "FUTURA"
                : fecha.isAfter(oferta.getFechaFin()) ? "VENCIDA" : "VIGENTE";
        return new OfertaResponse(oferta.getId(), oferta.getTitulo(), oferta.getFechaInicio(), oferta.getFechaFin(),
                oferta.isPublicada(), estado, oferta.getDetalles().stream()
                .filter(d -> operaciones || d.getProducto().getActivo())
                .map(d -> new OfertaResponse.Item(d.getProducto().getId(), d.getProducto().getNombre(),
                        d.getProducto().getUnidadMedida(), d.getPrecioVenta(), d.getProducto().getActivo())).toList());
    }
}
