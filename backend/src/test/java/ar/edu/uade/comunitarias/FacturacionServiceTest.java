package ar.edu.uade.comunitarias;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.*;
import java.util.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ar.edu.uade.comunitarias.FacturacionFixture.*;
class FacturacionServiceTest {
    final FacturaRepository facturas=mock(FacturaRepository.class);
    final PagoRepository pagos=mock(PagoRepository.class);
    final FacturacionPedidoRepository pedidos=mock(FacturacionPedidoRepository.class);
    final ClienteRepository clientes=mock(ClienteRepository.class);
    final FacturacionAcceso acceso=new FacturacionAcceso(clientes,true);
    final FacturacionService service=new FacturacionService(facturas,pagos,pedidos,acceso,CLOCK);
    @BeforeEach void setup(){
        when(facturas.saveAndFlush(any())).thenAnswer(c->{Factura f=c.getArgument(0);ReflectionTestUtils.setField(f,"id",4L);return f;});
        when(clientes.findByNombreComercio(PedidoService.CLIENTE_DEMO)).thenReturn(List.of(pedido("10",true).getCliente()));
    }
    @Test void emiteTotalServidorYRepiteFacturaSinSegundaEscritura(){
        when(pedidos.bloquear(3L)).thenReturn(Optional.of(pedido("10",true)));
        var nueva=service.emitir(new EmitirFacturaRequest(3L),OPS);assertEquals(new BigDecimal("20"),nueva.importeTotal());
        when(facturas.findByPedidoId(3L)).thenReturn(Optional.of(factura()));
        var segunda=service.emitir(new EmitirFacturaRequest(3L),OPS);assertEquals(nueva.id(),segunda.id());verify(facturas,times(1)).saveAndFlush(any());
    }
    @Test void noEntregadoNoPersiste(){when(pedidos.bloquear(3L)).thenReturn(Optional.of(pedido("10",false)));assertThrows(ReglaNegocioException.class,()->service.emitir(new EmitirFacturaRequest(3L),OPS));verify(facturas,never()).saveAndFlush(any());}
    @Test void inexistente404(){assertThrows(RecursoNoEncontradoException.class,()->service.emitir(new EmitirFacturaRequest(99L),OPS));}
    @Test void clienteYGerenciaNoEmiten(){for(var actor:List.of(CLI,GER))assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.emitir(new EmitirFacturaRequest(3L),actor));verifyNoInteractions(pedidos);}
    @Test void consultaClienteFiltradaSinEnumerarAjenas(){when(facturas.findByPedidoClienteIdOrderByIdDesc(1L)).thenReturn(List.of(factura()));assertEquals(1,service.listar(CLI).size());verify(facturas,never()).findAllByOrderByIdDesc();}
    @Test void clienteNoLeeFacturaOPagosDeOtro(){var f=factura();ReflectionTestUtils.setField(f.getPedido().getCliente(),"id",99L);when(facturas.findById(4L)).thenReturn(Optional.of(f));assertThrows(RecursoNoEncontradoException.class,()->service.detalle(4L,CLI));assertThrows(RecursoNoEncontradoException.class,()->service.pagos(4L,CLI));verifyNoInteractions(pagos);}
    @Test void demoAmbiguoNoSeleccionaClienteArbitrario(){when(clientes.findByNombreComercio(PedidoService.CLIENTE_DEMO)).thenReturn(List.of());assertThrows(ReglaNegocioException.class,()->service.listar(CLI));}
    @Test void cuentaRealUsaAsociacionUsuario(){var c=pedido("10",true).getCliente();when(clientes.findByUsuarioId(10L)).thenReturn(Optional.of(c));var actor=new SessionResponse(10L,"Real","real@example.invalid","CLIENTE");assertEquals(1L,new FacturacionAcceso(clientes,false).clientePropio(actor));}
}
