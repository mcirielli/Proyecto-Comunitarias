package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.HistorialPreciosService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistorialPreciosServiceTest {
    // 2026-10-27T15:00Z = 27/10 12:00 en Argentina.
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-27T15:00:00Z"), ZoneOffset.UTC);
    final ProductoRepository productos = mock(ProductoRepository.class);
    final HistorialPreciosRepository precios = mock(HistorialPreciosRepository.class);
    final HistorialPreciosService service = new HistorialPreciosService(productos, precios, CLOCK);

    @BeforeEach void setup() {
        var arroz = new Producto("Arroz", null, null, "kg");
        ReflectionTestUtils.setField(arroz, "id", 2L);
        when(productos.findById(2L)).thenReturn(Optional.of(arroz));
    }

    static PrecioVentaItem venta(long id, String desde, String hasta, String precio) {
        return new PrecioVentaItem(id, "Oferta " + id, LocalDate.parse(desde), LocalDate.parse(hasta), new BigDecimal(precio));
    }
    static PrecioCompraItem compra(long id, String fecha, EstadoCompra estado, String precio) {
        return new PrecioCompraItem(id, Instant.parse(fecha), "Proveedor", estado, TipoCompra.DEMANDA_CLIENTES, 5, new BigDecimal(precio));
    }

    @Test void calculaVariacionRespectoDelPrecioAnteriorYPrecioVigente() {
        when(precios.ventas(2L)).thenReturn(List.of(
                venta(3, "2026-11-02", "2026-11-08", "1320"),
                venta(2, "2026-10-26", "2026-11-01", "1200"),
                venta(1, "2026-10-19", "2026-10-25", "1000")));
        var r = service.historial(2L);
        assertEquals("Arroz", r.producto());
        assertEquals(new BigDecimal("10.0"), r.ventas().get(0).variacion());
        assertEquals(new BigDecimal("20.0"), r.ventas().get(1).variacion());
        assertNull(r.ventas().get(2).variacion());
        assertEquals(0, new BigDecimal("1200").compareTo(r.precioVentaVigente()));
    }

    @Test void sinOfertaVigenteNoInformaPrecioActual() {
        when(precios.ventas(2L)).thenReturn(List.of(venta(1, "2026-10-19", "2026-10-25", "1000")));
        assertNull(service.historial(2L).precioVentaVigente());
    }

    @Test void ultimaCompraIgnoraOrdenesCanceladas() {
        when(precios.compras(2L)).thenReturn(List.of(
                compra(9, "2026-10-26T12:00:00Z", EstadoCompra.CANCELADA, "900"),
                compra(8, "2026-10-20T12:00:00Z", EstadoCompra.RECIBIDA, "750"),
                compra(7, "2026-10-10T12:00:00Z", EstadoCompra.RECIBIDA, "600")));
        var r = service.historial(2L);
        assertEquals(0, new BigDecimal("750").compareTo(r.ultimoPrecioCompra()));
        assertEquals(new BigDecimal("25.0"), r.compras().get(1).variacion());
        assertEquals(3, r.compras().size());
    }

    @Test void productoInexistenteEs404SinConsultarPrecios() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.historial(99L));
        verifyNoInteractions(precios);
    }

    @Test void sinRegistrosDevuelveListasVacias() {
        var r = service.historial(2L);
        assertTrue(r.ventas().isEmpty());
        assertTrue(r.compras().isEmpty());
        assertNull(r.precioVentaVigente());
        assertNull(r.ultimoPrecioCompra());
    }
}
