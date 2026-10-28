package ar.edu.uade.comunitarias;
import ar.edu.uade.comunitarias.domain.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class InventarioDomainTest {
    final Instant ahora=Instant.parse("2026-10-27T15:00:00Z");
    @Test void reservaNoDescuentaDosVecesYEgresoConservaLibre(){
        var s=new Stock(1L);s.ingresar(10);s.reservar(7);
        assertEquals(3,s.getDisponible());assertEquals(7,s.getReservado());assertEquals(10,s.existencia());
        s.consumir(7);assertEquals(3,s.getDisponible());assertEquals(0,s.getReservado());assertEquals(3,s.existencia());
        assertThrows(ReglaNegocioException.class,()->s.consumir(7));
    }
    @Test void insuficienteNoMutaSaldo(){var s=new Stock(1L);s.ingresar(3);assertThrows(ReglaNegocioException.class,()->s.reservar(4));assertEquals(3,s.existencia());assertEquals(0,s.getReservado());}
    @Test void overflowDeExistenciaSeRechazaAntesDeMutar(){var s=new Stock(1L);s.ingresar(Integer.MAX_VALUE);s.reservar(1);assertThrows(ArithmeticException.class,()->s.ingresar(1));assertEquals(Integer.MAX_VALUE,s.existencia());}
    @Test void cantidadesNoPositivasNoSeOperan(){var s=new Stock(1L);for(int n:new int[]{0,-1}){assertThrows(IllegalArgumentException.class,()->s.ingresar(n));assertThrows(IllegalArgumentException.class,()->s.reservar(n));assertThrows(IllegalArgumentException.class,()->s.consumir(n));}}
    @Test void reservaTieneConsumoUnico(){var r=new ReservaPedido(1L,2L,3,ahora,"ops");r.consumir(ahora);assertTrue(r.isConsumida());assertThrows(ReglaNegocioException.class,()->r.consumir(ahora));}
    @Test void transferenciaConservaOrigenYCantidad(){var a=new AsignacionInventario(1L,2L,3L,4L,7,ahora);assertThrows(ReglaNegocioException.class,()->a.reservar(ahora));a.recibir(ahora);a.reservar(ahora);a.consumir(ahora);assertEquals(AsignacionInventario.Fase.CONSUMIDA,a.getFase());assertEquals(4L,a.getOrdenId());assertEquals(7,a.getCantidad());assertThrows(ReglaNegocioException.class,()->a.recibir(ahora));}
    @Test void soloEmitidaSeRecibeExactamenteUnaVez(){var p=new Proveedor("IT",null,null,null);var o=new OrdenCompra(p,TipoCompra.STOCK_QUINTEROS,UUID.randomUUID(),"ops",ahora);assertThrows(ReglaNegocioException.class,()->o.recibir("ops",ahora));o.autorizar("gerencia",ahora);assertThrows(ReglaNegocioException.class,()->o.recibir("ops",ahora));o.emitir("ops",ahora);o.recibir("ops",ahora);assertEquals(EstadoCompra.RECIBIDA,o.getEstado());assertThrows(ReglaNegocioException.class,()->o.recibir("ops",ahora));}
}
