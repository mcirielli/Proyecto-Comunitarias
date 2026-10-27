package ar.edu.uade.comunitarias;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.state.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
class PedidoStateTest {
    static Stream<Arguments> transiciones() {
        return Stream.of(EstadoPedido.values()).flatMap(origen -> Stream.of(EstadoPedido.values()).map(destino -> Arguments.of(origen,destino)));
    }
    @ParameterizedTest @MethodSource("transiciones")
    void todasLasCombinaciones(EstadoPedido origen,EstadoPedido destino) {
        var state=PedidoStates.reconstruir(origen);
        if(origen.ordinal()<5 && destino.ordinal()==origen.ordinal()+1) assertEquals(destino,state.cambiarA(destino).codigo());
        else assertThrows(TransicionInvalidaException.class,() -> state.cambiarA(destino));
    }
    @Test void pedidoDelegaYGuardaFechasSinAlterarseAnteSaltoInvalido() {
        var ahora=Instant.parse("2026-10-27T15:00:00Z");
        var p=new Pedido(new Cliente("Demo"),new Oferta("Oferta",LocalDate.now(),LocalDate.now()),ahora);
        p.agregar(new Producto("Arroz",null,null,"kg"),2,new BigDecimal("1250.50"));
        assertThrows(TransicionInvalidaException.class,() -> p.cambiarEstado(EstadoPedido.ENTREGADO,ahora));
        assertEquals(EstadoPedido.RECIBIDO,p.getEstado()); assertNull(p.getFechaEntrega());
        for(var destino:new EstadoPedido[]{EstadoPedido.CONFIRMADO,EstadoPedido.EN_PREPARACION,EstadoPedido.PREPARADO,EstadoPedido.DESPACHADO,EstadoPedido.ENTREGADO}) p.cambiarEstado(destino,ahora);
        assertEquals(ahora,p.getFechaDespacho());assertEquals(ahora,p.getFechaEntrega());
        assertEquals(new BigDecimal("2501.00"),p.total());
        assertThrows(TransicionInvalidaException.class,() -> p.cambiarEstado(EstadoPedido.RECIBIDO,ahora));
    }
}
