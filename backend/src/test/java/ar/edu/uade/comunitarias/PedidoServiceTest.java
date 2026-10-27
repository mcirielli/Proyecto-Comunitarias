package ar.edu.uade.comunitarias;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.PedidoService;
import ar.edu.uade.comunitarias.state.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PedidoServiceTest {
    final PedidoRepository pedidos=mock(PedidoRepository.class);
    final ClienteRepository clientes=mock(ClienteRepository.class);
    final OfertaRepository ofertas=mock(OfertaRepository.class);
    final Clock clock=Clock.fixed(Instant.parse("2026-10-27T15:00:00Z"),ZoneOffset.UTC);
    final PedidoService service=new PedidoService(pedidos,clientes,ofertas,clock,true);
    final SessionResponse ops=new SessionResponse(-2L,"Demo","operaciones@demo.local","OPERACIONES");
    final SessionResponse actor=new SessionResponse(-1L,"Cliente","cliente@demo.local","CLIENTE");
    Cliente cliente; Oferta oferta; Producto producto;
    @BeforeEach void setup() {
        cliente=new Cliente(PedidoService.CLIENTE_DEMO); ReflectionTestUtils.setField(cliente,"id",1L);
        producto=new Producto("Arroz",null,null,"kg");ReflectionTestUtils.setField(producto,"id",2L);
        oferta=new Oferta("Oferta",LocalDate.of(2026,10,27),LocalDate.of(2026,11,1));ReflectionTestUtils.setField(oferta,"id",3L);
        oferta.agregar(producto,new BigDecimal("19.90"));
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(clientes.findByNombreComercio(PedidoService.CLIENTE_DEMO)).thenReturn(List.of(cliente));
        when(ofertas.findById(3L)).thenReturn(Optional.of(oferta));
        when(pedidos.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
    }
    CrearPedidoRequest request(Long clienteId,int cantidad) { return new CrearPedidoRequest(clienteId,3L,List.of(new CrearPedidoRequest.Item(2L,cantidad))); }
    @Test void precioEsDelServidorYSeGuardaEnDetalle() {
        var response=service.crear(request(1L,3),ops);
        assertEquals(new BigDecimal("59.70"),response.total());
        assertEquals(new BigDecimal("19.90"),response.detalles().get(0).precioUnitario());
        assertEquals(EstadoPedido.RECIBIDO,response.estado());
    }
    @Test void clienteNoPuedeSuplantarOtraIdentidad() {
        assertThrows(ReglaNegocioException.class,() -> service.crear(request(999L,1),actor));verifyNoInteractions(pedidos);
    }
    @Test void ceroUnidadesNoPersiste() {
        assertThrows(IllegalArgumentException.class,() -> service.crear(request(1L,0),ops));verifyNoInteractions(pedidos);
    }
    @Test void productoRepetidoNoPersisteParcialmente() {
        var item=new CrearPedidoRequest.Item(2L,1);
        assertThrows(IllegalArgumentException.class,() -> service.crear(new CrearPedidoRequest(1L,3L,List.of(item,item)),ops));verifyNoInteractions(pedidos);
    }
    @Test void productoFueraDeOfertaNoSeAcepta() {
        assertThrows(ReglaNegocioException.class,() -> service.crear(new CrearPedidoRequest(1L,3L,List.of(new CrearPedidoRequest.Item(90L,1))),ops));verifyNoInteractions(pedidos);
    }
    @Test void ofertaVencidaNoSeAcepta() {
        ReflectionTestUtils.setField(oferta,"fechaFin",LocalDate.of(2026,10,26));
        assertThrows(ReglaNegocioException.class,() -> service.crear(request(1L,1),ops));verifyNoInteractions(pedidos);
    }
    @Test void productoInactivoNoSeAcepta() {
        producto.cambiarActivo(false);assertThrows(ReglaNegocioException.class,() -> service.crear(request(1L,1),ops));verifyNoInteractions(pedidos);
    }
    @Test void consultaClienteFiltraPorSuClientePersistido() {
        when(pedidos.findByClienteIdOrderByIdDesc(1L)).thenReturn(List.of());
        assertTrue(service.listar(actor).isEmpty());verify(pedidos).findByClienteIdOrderByIdDesc(1L);verify(pedidos,never()).findAllByOrderByIdDesc();
    }
    @Test void transicionInvalidaNoGuardaCambios() {
        var pedido=new Pedido(cliente,oferta,clock.instant());pedido.agregar(producto,1,new BigDecimal("19.90"));
        when(pedidos.buscarParaCambiar(4L)).thenReturn(Optional.of(pedido));
        assertThrows(TransicionInvalidaException.class,() -> service.cambiarEstado(4L,EstadoPedido.ENTREGADO));
        assertEquals(EstadoPedido.RECIBIDO,pedido.getEstado());verify(pedidos,never()).saveAndFlush(any());
    }
}
