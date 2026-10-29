package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import java.util.*;
import java.time.Clock;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
@Service @Profile("supabase") @ConditionalOnProperty(name="app.facturacion.enabled",havingValue="true") @Transactional(readOnly=true)
public class FacturacionService {
    private final FacturaRepository facturas;private final PagoRepository pagos;private final FacturacionPedidoRepository pedidos;private final FacturacionAcceso acceso;private final Clock clock;
    public FacturacionService(FacturaRepository f,PagoRepository p,FacturacionPedidoRepository pedidos,FacturacionAcceso acceso,Clock clock){facturas=f;pagos=p;this.pedidos=pedidos;this.acceso=acceso;this.clock=clock;}
    public record Elegible(Long id,String cliente,BigDecimal total) {}
    public List<Elegible> contexto(SessionResponse actor){
        acceso.operacion(actor);
        return pedidos.findByEstadoOrderByIdDesc(EstadoPedido.ENTREGADO).stream().filter(p->facturas.findByPedidoId(p.getId()).isEmpty()).map(p->new Elegible(p.getId(),p.getCliente().getNombreComercio(),p.total())).toList();
    }
    public List<FacturaResponse> listar(SessionResponse actor){
        Long id=acceso.clientePropio(actor);
        return (id==null ? facturas.findAllByOrderByIdDesc() : facturas.findByPedidoClienteIdOrderByIdDesc(id)).stream().map(this::respuesta).toList();
    }
    public FacturaResponse detalle(Long id,SessionResponse actor){return respuesta(buscar(id,actor));}
    public List<PagoResponse> pagos(Long id,SessionResponse actor){buscar(id,actor);return pagos.findByFacturaIdOrderByIdAsc(id).stream().map(PagoResponse::de).toList();}
    private Factura buscar(Long id,SessionResponse actor){acceso.consulta(actor);var f=facturas.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Factura inexistente"));acceso.comprobar(f,actor);return f;}
    private FacturaResponse respuesta(Factura f){return FacturaResponse.de(f,pagos.findByFacturaIdOrderByIdAsc(f.getId()));}
    @Transactional
    public FacturaResponse emitir(EmitirFacturaRequest request,SessionResponse actor){
        acceso.operacion(actor);
        if(request==null || request.pedidoId()==null || request.pedidoId()<=0)throw new IllegalArgumentException("Seleccione un pedido");
        var pedido=pedidos.bloquear(request.pedidoId()).orElseThrow(()->new RecursoNoEncontradoException("Pedido inexistente"));
        var existente=facturas.findByPedidoId(pedido.getId());
        if(existente.isPresent())return respuesta(existente.get());
        var factura=new Factura(pedido,clock.instant());
        return respuesta(facturas.saveAndFlush(factura));
    }
}
