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
class PagoServiceTest {
    final FacturaRepository facturas=mock(FacturaRepository.class);
    final PagoRepository pagos=mock(PagoRepository.class);
    final FacturacionAcceso acceso=new FacturacionAcceso(mock(ClienteRepository.class),false);
    final PagoService service=new PagoService(facturas,pagos,acceso,CLOCK);
    final UUID clave=UUID.randomUUID();Factura factura;
    @BeforeEach void setup(){factura=factura();when(facturas.bloquear(4L)).thenReturn(Optional.of(factura));when(pagos.totalFactura(4L)).thenReturn(BigDecimal.ZERO);when(pagos.saveAndFlush(any())).thenAnswer(c->{Pago p=c.getArgument(0);ReflectionTestUtils.setField(p,"id",5L);return p;});}
    RegistrarPagoRequest request(String importe){return new RegistrarPagoRequest(4L,clave,new BigDecimal(importe),MedioPago.EFECTIVO," demo ");}
    @Test void parcialYTotalActualizanEstado(){service.registrar(request("5"),OPS);assertEquals(EstadoFactura.PENDIENTE,factura.getEstado());when(pagos.totalFactura(4L)).thenReturn(new BigDecimal("5"));service.registrar(request("15"),OPS);assertEquals(EstadoFactura.PAGADA,factura.getEstado());}
    @Test void sobrepagoNoGuarda(){assertThrows(ReglaNegocioException.class,()->service.registrar(request("21"),OPS));verify(pagos,never()).saveAndFlush(any());}
    @Test void mismaClaveYMismoContenidoDevuelveOriginalSinCambiarSaldo(){var p=new Pago(factura,clave,new BigDecimal("20"),MedioPago.EFECTIVO,"demo",CLOCK.instant());ReflectionTestUtils.setField(p,"id",5L);when(pagos.findByClaveSolicitud(clave)).thenReturn(Optional.of(p));assertEquals(5L,service.registrar(request("20.00"),OPS).id());verify(pagos,never()).totalFactura(any());verify(pagos,never()).saveAndFlush(any());}
    @Test void mismaClaveContenidoDistinto409(){var p=new Pago(factura,clave,BigDecimal.ONE,MedioPago.EFECTIVO,"demo",CLOCK.instant());when(pagos.findByClaveSolicitud(clave)).thenReturn(Optional.of(p));assertThrows(ReglaNegocioException.class,()->service.registrar(request("2"),OPS));verify(pagos,never()).saveAndFlush(any());}
    @Test void mediosInvalidosEImportesInvalidos(){for(String v:new String[]{"0","-1","1.001","10000000000"})assertThrows(IllegalArgumentException.class,()->service.registrar(request(v),OPS));assertThrows(IllegalArgumentException.class,()->service.registrar(new RegistrarPagoRequest(4L,clave,BigDecimal.ONE,null,null),OPS));}
    @Test void anuladaNoAceptaPago(){ReflectionTestUtils.setField(factura,"estado",EstadoFactura.ANULADA);assertThrows(ReglaNegocioException.class,()->service.registrar(request("1"),OPS));}
    @Test void clienteYGerenciaNoRegistran(){for(var a:List.of(CLI,GER))assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.registrar(request("1"),a));verifyNoInteractions(facturas,pagos);}
    @Test void facturaCeroNoAceptaPagoPositivo(){factura=new Factura(pedido("0",true),CLOCK.instant());ReflectionTestUtils.setField(factura,"id",4L);when(facturas.bloquear(4L)).thenReturn(Optional.of(factura));assertThrows(ReglaNegocioException.class,()->service.registrar(request("1"),OPS));}
}
