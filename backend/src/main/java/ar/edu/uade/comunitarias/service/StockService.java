package ar.edu.uade.comunitarias.service;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import java.time.Clock;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Profile("supabase") @Transactional
@ConditionalOnProperty(name="app.inventario.enabled",havingValue="true")
public class StockService {
    private final StockRepository stock;
    private final MovimientoStockRepository movimientos;
    private final ReservaPedidoRepository reservas;
    private final AsignacionInventarioRepository asignaciones;
    private final CompraAbastecimientoRepository locks;
    private final Clock clock;
    public StockService(StockRepository stock,MovimientoStockRepository movimientos,ReservaPedidoRepository reservas,
            AsignacionInventarioRepository asignaciones,CompraAbastecimientoRepository locks,Clock clock) {
        this.stock=stock;this.movimientos=movimientos;this.reservas=reservas;this.asignaciones=asignaciones;this.locks=locks;this.clock=clock;
    }
    public void bloquearProductos(Collection<Long> ids) {
        for(Long id:new TreeSet<>(ids)) locks.bloquearProducto(id).orElseThrow(()->new RecursoNoEncontradoException("Producto inexistente"));
    }
    Stock saldo(Long producto) { return stock.findByProductoId(producto).orElseGet(()->stock.save(new Stock(producto))); }
    public void ingresar(Long orden,Long producto,int cantidad,String actor) {
        // El caso de recepción ya tomó locks de todos los productos antes de la orden.
        saldo(producto).ingresar(cantidad);
        movimientos.save(new MovimientoStock(producto,orden,null,"INGRESO",cantidad,clock.instant(),actor));
    }
    public void transicion(Pedido pedido,EstadoPedido destino,String actor) {
        if(!Set.of(EstadoPedido.EN_PREPARACION,EstadoPedido.PREPARADO,EstadoPedido.DESPACHADO,EstadoPedido.ENTREGADO).contains(destino))return;
        var lineas=new TreeMap<Long,Integer>();
        pedido.getDetalles().forEach(d->lineas.put(d.getProducto().getId(),d.getCantidad()));
        bloquearProductos(lineas.keySet());
        var propias=reservas.findByPedidoId(pedido.getId());
        var compromisos=asignaciones.findByPedidoId(pedido.getId());
        if(destino==EstadoPedido.EN_PREPARACION) {
            if(!propias.isEmpty())throw new ReglaNegocioException("El pedido ya tiene reservas");
            for(var linea:lineas.entrySet()) {
                long producto=linea.getKey(); int cantidad=linea.getValue();
                int fisicaPropia=0;
                for(var a:compromisos)if(a.getProductoId().equals(producto)) {
                    if(a.getFase()!=AsignacionInventario.Fase.FISICA)throw new ReglaNegocioException("Falta recibir cobertura del pedido");
                    fisicaPropia=Math.addExact(fisicaPropia,a.getCantidad());
                }
                int comprometido=0;
                for(var a:asignaciones.findByProductoIdIn(List.of(producto)))if(a.getFase()==AsignacionInventario.Fase.FISICA)
                    comprometido=Math.addExact(comprometido,a.getCantidad());
                Stock saldo=saldo(producto);
                if(fisicaPropia>cantidad || comprometido>saldo.getDisponible())throw new ReglaNegocioException("Cobertura inconsistente; requiere conciliación");
                int utilizable=Math.addExact(saldo.getDisponible()-comprometido,fisicaPropia);
                if(utilizable<cantidad)throw new ReglaNegocioException("Stock no comprometido insuficiente para reservar el pedido completo");
                saldo.reservar(cantidad);
                reservas.save(new ReservaPedido(pedido.getId(),producto,cantidad,clock.instant(),actor));
                movimientos.save(new MovimientoStock(producto,null,pedido.getId(),"RESERVA",cantidad,clock.instant(),actor));
            }
            compromisos.forEach(a->a.reservar(clock.instant()));
            return;
        }
        if(propias.size()!=lineas.size())throw new ReglaNegocioException("Pedido histórico o sin reserva propia completa");
        for(var reserva:propias) {
            if(!Objects.equals(lineas.get(reserva.getProductoId()),reserva.getCantidad()) ||
                    reserva.isConsumida()!=(destino==EstadoPedido.ENTREGADO))
                throw new ReglaNegocioException("Reserva incompatible con la transición");
        }
        if(destino==EstadoPedido.DESPACHADO) {
            for(var reserva:propias) {
                saldo(reserva.getProductoId()).consumir(reserva.getCantidad());
                reserva.consumir(clock.instant());
                movimientos.save(new MovimientoStock(reserva.getProductoId(),null,pedido.getId(),"EGRESO",reserva.getCantidad(),clock.instant(),actor));
            }
            compromisos.forEach(a->a.consumir(clock.instant()));
        }
    }
    @Transactional(readOnly=true)
    public Map<Long,Integer> comprometidoFisico(Collection<Long> ids) {
        Map<Long,Integer> resultado=new TreeMap<>();
        asignaciones.findByProductoIdIn(ids).stream().filter(a->a.getFase()==AsignacionInventario.Fase.FISICA)
                .forEach(a->resultado.merge(a.getProductoId(),a.getCantidad(),Math::addExact));
        return resultado;
    }
}
