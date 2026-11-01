package ar.edu.uade.comunitarias.service;

import ar.edu.uade.comunitarias.domain.EstadoCompra;
import ar.edu.uade.comunitarias.domain.RecursoNoEncontradoException;
import ar.edu.uade.comunitarias.dto.HistorialPreciosResponse;
import ar.edu.uade.comunitarias.dto.PrecioCompraItem;
import ar.edu.uade.comunitarias.dto.PrecioVentaItem;
import ar.edu.uade.comunitarias.repository.HistorialPreciosRepository;
import ar.edu.uade.comunitarias.repository.ProductoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Layer del historial de precios: arma la evolución de precios de venta (ofertas publicadas)
 * y de compra (órdenes de compra) sin escribir en la base ni requerir migraciones.
 */
@Service @Profile("supabase") @Transactional(readOnly = true)
public class HistorialPreciosService {
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final ProductoRepository productos;
    private final HistorialPreciosRepository precios;
    private final Clock clock;

    @Autowired
    public HistorialPreciosService(ProductoRepository productos, ObjectProvider<HistorialPreciosRepository> precios, Clock clock) {
        this(productos, precios.getIfAvailable(), clock);
    }

    public HistorialPreciosService(ProductoRepository productos, HistorialPreciosRepository precios, Clock clock) {
        this.productos = productos; this.precios = precios; this.clock = clock;
    }

    public HistorialPreciosResponse historial(Long productoId) {
        var producto = productos.findById(productoId).orElseThrow(() -> new RecursoNoEncontradoException("Producto inexistente"));
        if (precios == null) throw new IllegalStateException("Historial de precios no disponible en este entorno");

        var ventas = conVariacion(precios.ventas(productoId), PrecioVentaItem::precio, PrecioVentaItem::conVariacion);
        var compras = conVariacion(precios.compras(productoId), PrecioCompraItem::precio, PrecioCompraItem::conVariacion);

        LocalDate hoy = LocalDate.now(clock.withZone(ZONA));
        BigDecimal vigente = ventas.stream()
                .filter(v -> v.desde() != null && v.hasta() != null && !hoy.isBefore(v.desde()) && !hoy.isAfter(v.hasta()))
                .map(PrecioVentaItem::precio).findFirst().orElse(null);
        BigDecimal ultimaCompra = compras.stream().filter(c -> c.estado() != EstadoCompra.CANCELADA)
                .map(PrecioCompraItem::precio).findFirst().orElse(null);

        return new HistorialPreciosResponse(producto.getId(), producto.getNombre(), producto.getUnidadMedida(),
                vigente, ultimaCompra, ventas, compras, precios.proveedores(productoId));
    }

    /** Las listas llegan de la más reciente a la más antigua; cada ítem se compara con el siguiente (anterior en el tiempo). */
    static <T> List<T> conVariacion(List<T> items, Function<T, BigDecimal> precio, BiFunction<T, BigDecimal, T> con) {
        List<T> resultado = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            BigDecimal actual = precio.apply(items.get(i));
            BigDecimal anterior = i + 1 < items.size() ? precio.apply(items.get(i + 1)) : null;
            resultado.add(con.apply(items.get(i), variacion(actual, anterior)));
        }
        return resultado;
    }

    static BigDecimal variacion(BigDecimal actual, BigDecimal anterior) {
        if (actual == null || anterior == null || anterior.signum() == 0) return null;
        return actual.subtract(anterior).multiply(CIEN).divide(anterior, 1, RoundingMode.HALF_UP);
    }
}
