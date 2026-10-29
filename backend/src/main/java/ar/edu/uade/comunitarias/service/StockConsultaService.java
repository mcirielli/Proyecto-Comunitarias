package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.repository.*;
import java.time.Instant;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Profile("supabase") @Transactional(readOnly=true)
@ConditionalOnProperty(name="app.inventario.enabled",havingValue="true")
public class StockConsultaService {
    public record Saldo(Long productoId,String producto,int disponible,int reservado,int existencia){}
    public record Movimiento(Long id,Long productoId,Long ordenId,Long pedidoId,String tipo,int cantidad,Instant fecha){}
    private final ProductoRepository productos;
    private final StockRepository stocks;
    private final MovimientoStockRepository movimientos;
    public StockConsultaService(ProductoRepository productos,StockRepository stocks,MovimientoStockRepository movimientos){this.productos=productos;this.stocks=stocks;this.movimientos=movimientos;}
    public List<Saldo> listar(){
        Map<Long,Stock> porProducto=new HashMap<>();stocks.findAll().forEach(s->porProducto.put(s.getProductoId(),s));
        return productos.findAll().stream().sorted(Comparator.comparing(Producto::getId)).map(p->{
            var s=porProducto.get(p.getId());return new Saldo(p.getId(),p.getNombre(),s==null?0:s.getDisponible(),s==null?0:s.getReservado(),s==null?0:s.existencia());
        }).toList();
    }
    public List<Movimiento> movimientos(Long id,int pagina){
        if(pagina<0)throw new IllegalArgumentException("Página inválida");
        if(!productos.existsById(id))throw new RecursoNoEncontradoException("Producto inexistente");
        return movimientos.findByProductoIdOrderByFechaDescIdDesc(id,PageRequest.of(pagina,100)).stream()
            .map(m->new Movimiento(m.getId(),m.getProductoId(),m.getOrdenId(),m.getPedidoId(),m.getTipo(),m.getCantidad(),m.getFecha())).toList();
    }
}
