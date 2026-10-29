package ar.edu.uade.comunitarias;
import ar.edu.uade.comunitarias.domain.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static ar.edu.uade.comunitarias.FacturacionFixture.*;
class FacturaTest {
    @Test void soloEntregadoYNumeracionDeterminista(){
        assertThrows(ReglaNegocioException.class,()->new Factura(pedido("10",false),CLOCK.instant()));
        var f=factura();assertEquals("INT-PED-3",f.getNumero());assertEquals(new BigDecimal("20.00"),f.getImporteTotal());
    }
    @Test void facturaCeroPagadaSinInventarPago(){var f=new Factura(pedido("0",true),CLOCK.instant());assertEquals(EstadoFactura.PAGADA,f.getEstado());assertEquals(0,f.getImporteTotal().signum());}
    @Test void parcialTotalYSobrepago(){
        var f=factura();f.registrarAcumulado(new BigDecimal("5"));assertEquals(EstadoFactura.PENDIENTE,f.getEstado());
        assertThrows(ReglaNegocioException.class,()->f.registrarAcumulado(new BigDecimal("21")));
        f.registrarAcumulado(new BigDecimal("20"));assertEquals(EstadoFactura.PAGADA,f.getEstado());
    }
    @Test void importesInvalidos(){for(String valor:new String[]{"-1","0","0.001","10000000000"})assertThrows(IllegalArgumentException.class,()->Factura.validarImporte(new BigDecimal(valor),true));}
    @Test void totalHistoricoDemasiadoGrandeNoSeRedondea(){assertThrows(IllegalArgumentException.class,()->new Factura(pedido("9999999999.99",true),CLOCK.instant()));}
}
