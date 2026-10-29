package ar.edu.uade.comunitarias;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.SessionResponse;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import java.math.BigDecimal;
import java.time.*;
import org.springframework.test.util.ReflectionTestUtils;
class FacturacionFixture {
    static final Clock CLOCK=Clock.fixed(Instant.parse("2026-10-27T15:00:00Z"),ZoneOffset.UTC);
    static final SessionResponse OPS=new SessionResponse(-2L,"Ops","operaciones@demo.local","OPERACIONES");
    static final SessionResponse CLI=new SessionResponse(-1L,"Cliente","cliente@demo.local","CLIENTE");
    static final SessionResponse GER=new SessionResponse(-3L,"Gerencia","gerencia@demo.local","GERENCIA");
    static Pedido pedido(String precio,boolean entregado){
        var c=new Cliente("Kiosco Demo UADE State");ReflectionTestUtils.setField(c,"id",1L);
        var p=new Producto("Arroz",null,null,"kg");ReflectionTestUtils.setField(p,"id",2L);
        var oferta=new Oferta("Fixture",LocalDate.of(2026,10,27),LocalDate.of(2026,10,28));
        var pedido=new Pedido(c,oferta,CLOCK.instant());ReflectionTestUtils.setField(pedido,"id",3L);
        pedido.agregar(p,2,new BigDecimal(precio));
        if(entregado) for(var estado:new EstadoPedido[]{EstadoPedido.CONFIRMADO,EstadoPedido.EN_PREPARACION,EstadoPedido.PREPARADO,EstadoPedido.DESPACHADO,EstadoPedido.ENTREGADO})pedido.cambiarEstado(estado,CLOCK.instant());
        return pedido;
    }
    static Factura factura(){var f=new Factura(pedido("10.00",true),CLOCK.instant());ReflectionTestUtils.setField(f,"id",4L);return f;}
}
