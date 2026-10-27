package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.OfertaService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OfertaServiceTest {
    final OfertaRepository ofertas = mock(OfertaRepository.class);
    final ProductoRepository productos = mock(ProductoRepository.class);
    // UTC ya es día 28, pero Buenos Aires sigue en el 27.
    final Clock clock = Clock.fixed(Instant.parse("2026-10-28T02:59:59Z"), ZoneOffset.UTC);
    final OfertaService service = new OfertaService(ofertas, productos, clock);
    final LocalDate hoy = LocalDate.of(2026,10,27);
    Producto producto;
    @BeforeEach void setup() {
        producto = new Producto("Arroz",null,null,"kg");
        ReflectionTestUtils.setField(producto,"id",1L);
        when(productos.findById(1L)).thenReturn(Optional.of(producto));
        when(ofertas.saveAndFlush(any())).thenAnswer(call -> { Oferta o = call.getArgument(0); ReflectionTestUtils.setField(o,"id",2L); return o; });
    }
    CrearOfertaRequest request(LocalDate fin, List<CrearOfertaRequest.Item> detalles) {
        return new CrearOfertaRequest("Oferta",hoy,fin,detalles);
    }
    CrearOfertaRequest.Item item(String precio) { return new CrearOfertaRequest.Item(1L,precio == null ? null : new BigDecimal(precio)); }
    Oferta oferta(boolean publicada, LocalDate inicio, LocalDate fin) {
        var o = Oferta.borrador("Oferta",inicio,fin); o.agregar(producto,new BigDecimal("10.25"));
        ReflectionTestUtils.setField(o,"id",2L); if(publicada) o.publicar(); return o;
    }
    @Test void creaBorradorConPrecioDecimalSinPublicar() {
        var response = service.crear(request(hoy,List.of(item("10.25"))));
        assertFalse(response.publicada()); assertEquals("BORRADOR",response.estado());
        assertEquals(new BigDecimal("10.25"),response.productos().get(0).precioVenta());
    }
    @Test void rechazaFechasInvertidasSinEscritura() {
        assertThrows(IllegalArgumentException.class,() -> service.crear(request(hoy.minusDays(1),List.of(item("1")))));
        verify(ofertas,never()).saveAndFlush(any());
    }
    @Test void rechazaVaciosDuplicadosYPreciosInvalidosSinEscritura() {
        for(var items : List.of(List.<CrearOfertaRequest.Item>of(),List.of(item("1"),item("2")),List.of(item("-0.01")),List.of(item(null)),List.of(item("1.001")),List.of(item("10000000000"))))
            assertThrows(IllegalArgumentException.class,() -> service.crear(request(hoy,items)));
        verify(ofertas,never()).saveAndFlush(any());
    }
    @Test void mantienePrecioCeroPermitidoPorPedidoYEsquema() {
        assertEquals(0,service.crear(request(hoy,List.of(item("0")))).productos().get(0).precioVenta().signum());
    }
    @Test void rechazaInactivosEInexistentes() {
        producto.cambiarActivo(false);
        assertThrows(ReglaNegocioException.class,() -> service.crear(request(hoy,List.of(item("1")))));
        assertThrows(RecursoNoEncontradoException.class,() -> service.crear(request(hoy,List.of(new CrearOfertaRequest.Item(99L,BigDecimal.ONE)))));
        verify(ofertas,never()).saveAndFlush(any());
    }
    @Test void clienteSoloVeVigentesConLimitesInclusivosYZonaArgentina() {
        var inicio = oferta(true,hoy,hoy.plusDays(1));
        var fin = oferta(true,hoy.minusDays(1),hoy);
        when(ofertas.findAll()).thenReturn(List.of(inicio,fin,oferta(false,hoy,hoy),oferta(true,hoy.plusDays(1),hoy.plusDays(2)),oferta(true,hoy.minusDays(2),hoy.minusDays(1))));
        assertEquals(2,service.listar(false).size()); assertEquals(5,service.listar(true).size());
        assertEquals(Set.of("VIGENTE","BORRADOR","FUTURA","VENCIDA"),new HashSet<>(service.listar(true).stream().map(OfertaResponse::estado).toList()));
    }
    @Test void desactivacionPosteriorSeOcultaSoloAlCliente() {
        when(ofertas.findAll()).thenReturn(List.of(oferta(true,hoy,hoy))); producto.cambiarActivo(false);
        assertTrue(service.listar(false).get(0).productos().isEmpty()); assertFalse(service.listar(true).get(0).productos().get(0).activo());
    }
    @Test void publicaRevalidandoYRepetirEsIdempotente() {
        var o=oferta(false,hoy,hoy); when(ofertas.buscarParaPublicar(2L)).thenReturn(Optional.of(o));
        assertTrue(service.publicar(2L).publicada()); service.publicar(2L);
        verify(ofertas,times(1)).saveAndFlush(o);
    }
    @Test void publicarRechazaDesactivacionSinCambiarBorrador() {
        var o=oferta(false,hoy,hoy); producto.cambiarActivo(false); when(ofertas.buscarParaPublicar(2L)).thenReturn(Optional.of(o));
        assertThrows(ReglaNegocioException.class,() -> service.publicar(2L)); assertFalse(o.isPublicada()); verify(ofertas,never()).saveAndFlush(any());
    }
    @Test void publicarRechazaOfertaVaciaYNoEncontrada() {
        var o=Oferta.borrador("Vacía",hoy,hoy); when(ofertas.buscarParaPublicar(2L)).thenReturn(Optional.of(o));
        assertThrows(ReglaNegocioException.class,() -> service.publicar(2L)); assertFalse(o.isPublicada());
        assertThrows(RecursoNoEncontradoException.class,() -> service.publicar(99L));
    }
}
