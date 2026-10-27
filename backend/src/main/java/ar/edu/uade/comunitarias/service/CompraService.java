package ar.edu.uade.comunitarias.service;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import ar.edu.uade.comunitarias.strategy.*;
import java.time.Clock;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Profile("supabase")
@ConditionalOnProperty(name="app.compras.enabled", havingValue="true")
@Transactional
public class CompraService {
    private final CompraAbastecimientoRepository abastecimiento;
    private final ProveedorRepository proveedores;
    private final ProductoProveedorRepository precios;
    private final StockAbastecimientoRepository stock;
    private final OrdenCompraRepository ordenes;
    private final CoberturaCompraRepository coberturas;
    private final Map<TipoCompra,CompraStrategy> estrategias;
    private final Clock clock;
    public CompraService(CompraAbastecimientoRepository abastecimiento, ProveedorRepository proveedores,
            ProductoProveedorRepository precios, StockAbastecimientoRepository stock,
            OrdenCompraRepository ordenes, CoberturaCompraRepository coberturas,
            CompraPorDemandaStrategy demanda, CompraQuinterosStrategy quinteros, Clock clock) {
        this.abastecimiento=abastecimiento; this.proveedores=proveedores; this.precios=precios;
        this.stock=stock; this.ordenes=ordenes; this.coberturas=coberturas; this.clock=clock;
        estrategias=Map.of(TipoCompra.DEMANDA_CLIENTES,demanda,TipoCompra.STOCK_QUINTEROS,quinteros);
    }

    public ResultadoCompraResponse crear(TipoCompra tipo, CrearCompraRequest request, SessionResponse actor) {
        exigirRol(actor,"OPERACIONES");
        validar(request,tipo);
        var proveedor=proveedores.findById(request.proveedorId()).orElseThrow(() -> new RecursoNoEncontradoException("Proveedor inexistente"));
        if (!proveedor.getActivo()) throw new ReglaNegocioException("Proveedor inactivo");
        List<Map<Long,Integer>> pedidos=new ArrayList<>();
        Map<Long,Integer> manual=new TreeMap<>();
        Map<Long,Integer> total=new TreeMap<>();
        // Orden estable de locks: primero pedidos y luego productos, también al emitir.
        for (Long id : new TreeSet<>(request.pedidos())) {
            var pedido=abastecimiento.bloquearPedido(id).orElseThrow(() -> new RecursoNoEncontradoException("Pedido inexistente"));
            if(pedido.getEstado()!=EstadoPedido.CONFIRMADO) throw new ReglaNegocioException("Solo se abastecen pedidos CONFIRMADO");
            Map<Long,Integer> lineas=new TreeMap<>();
            pedido.getDetalles().forEach(d -> lineas.put(d.getProducto().getId(),d.getCantidad()));
            pedidos.add(lineas);
            lineas.forEach((producto,cantidad) -> total.merge(producto,cantidad,Math::addExact));
        }
        request.productos().forEach(l -> manual.put(l.productoId(),l.cantidad()));
        var ids=new TreeSet<>(tipo==TipoCompra.DEMANDA_CLIENTES ? total.keySet() : manual.keySet());
        Map<Long,Producto> productos=new TreeMap<>();
        for(Long id:ids) productos.put(id,abastecimiento.bloquearProducto(id).orElseThrow(() -> new RecursoNoEncontradoException("Producto inexistente")));
        if(ordenes.existsByClaveSolicitud(request.claveSolicitud()) || coberturas.existsByClaveSolicitud(request.claveSolicitud()))
            throw new ReglaNegocioException("Solicitud ya procesada; consultá el listado");
        if(!request.pedidos().isEmpty() && coberturas.contarPedidosCubiertos(request.pedidos())>0)
            throw new ReglaNegocioException("Algún pedido ya tiene cobertura registrada");

        Map<Long,Integer> libre=new TreeMap<>(), abiertas=new TreeMap<>();
        stock.findByProductoIdIn(ids).forEach(s -> libre.put(s.getProductoId(),s.getCantidadDisponible()));
        ordenes.findByEstadoIn(List.of(EstadoCompra.EMITIDA,EstadoCompra.PENDIENTE)).forEach(o ->
            o.getDetalles().forEach(d -> abiertas.merge(d.getProducto().getId(),d.getCantidad(),Math::addExact)));
        // Ledger conservador aprobado para demo; recepción/liberación queda en inventario.
        coberturas.findAll().forEach(c -> c.getAsignaciones().forEach(a -> {
            libre.compute(a.getProductoId(),(id,n) -> Math.max(0,(n==null?0:n)-a.getStock()));
            abiertas.compute(a.getProductoId(),(id,n) -> Math.max(0,(n==null?0:n)-a.getAbierta()));
        }));
        CompraStrategy estrategia=estrategias.get(tipo);
        var cantidades=estrategia.calcular(new ContextoCompra(pedidos,manual,libre,abiertas));
        Map<Long,ProductoProveedor> vinculos=new HashMap<>();
        precios.findByProveedorId(proveedor.getId()).forEach(v -> vinculos.put(v.getProducto().getId(),v));
        for(Long id:ids) if(!productos.get(id).getActivo()) throw new ReglaNegocioException("Producto inactivo");
        for(Long id:cantidades.keySet()) {
            var vinculo=vinculos.get(id);
            if(vinculo==null || vinculo.getPrecioCompra()==null || vinculo.getPrecioCompra().signum()<0)
                throw new ReglaNegocioException("El proveedor no abastece todos los productos con un precio válido");
        }
        OrdenCompra orden=null;
        if(!cantidades.isEmpty()) {
            orden=new OrdenCompra(proveedor,tipo,request.claveSolicitud(),actor.email(),clock.instant());
            for(Long id:new TreeSet<>(cantidades.keySet())) orden.agregar(productos.get(id),cantidades.get(id),vinculos.get(id).getPrecioCompra());
            orden=ordenes.saveAndFlush(orden);
        }
        List<CoberturaCompra.Asignacion> asignaciones=new ArrayList<>();
        if(tipo==TipoCompra.DEMANDA_CLIENTES) {
            for(var linea:total.entrySet()) {
                int desdeStock=Math.min(linea.getValue(),libre.getOrDefault(linea.getKey(),0));
                // Lo que no cubre stock queda comprometido contra órdenes existentes + la nueva.
                asignaciones.add(new CoberturaCompra.Asignacion(linea.getKey(),desdeStock,linea.getValue()-desdeStock));
            }
        }
        // Un único registro de solicitud para ambos modos garantiza unicidad global del UUID.
        Long coberturaId=coberturas.saveAndFlush(new CoberturaCompra(request.claveSolicitud(),actor.email(),clock.instant(),new HashSet<>(request.pedidos()),orden,asignaciones)).getId();
        return new ResultadoCompraResponse(coberturaId,orden==null?null:CompraResponse.de(orden),orden==null?"Demanda cubierta sin compra nueva":"Compra registrada");
    }

    private void validar(CrearCompraRequest r, TipoCompra tipo) {
        if(r==null || r.claveSolicitud()==null || r.proveedorId()==null || r.proveedorId()<=0 || r.pedidos()==null || r.productos()==null
            || r.pedidos().size()>100 || r.productos().size()>100) throw new IllegalArgumentException("Solicitud inválida");
        Set<Long> ids=new HashSet<>();
        for(Long id:r.pedidos()) if(id==null || id<=0 || !ids.add(id)) throw new IllegalArgumentException("Pedido repetido o inválido");
        ids.clear();
        for(var l:r.productos()) if(l==null || l.productoId()==null || l.productoId()<=0 || l.cantidad()<=0 || !ids.add(l.productoId())) throw new IllegalArgumentException("Producto repetido o inválido");
        if(tipo==TipoCompra.DEMANDA_CLIENTES ? r.pedidos().isEmpty() || !r.productos().isEmpty() : !r.pedidos().isEmpty() || r.productos().isEmpty())
            throw new IllegalArgumentException("Contenido incompatible con el tipo de compra");
    }

    @Transactional(readOnly=true)
    public List<CompraResponse> listar(SessionResponse actor) {
        exigirConsulta(actor);
        return ordenes.findAllByOrderByIdDesc().stream().map(CompraResponse::de).toList();
    }
    @Transactional(readOnly=true)
    public Map<String,Object> contexto(SessionResponse actor) {
        exigirRol(actor,"OPERACIONES");
        return Map.of("proveedores",proveedores.findAll().stream().filter(Proveedor::getActivo).map(p ->
                Map.of("id",p.getId(),"nombre",p.getRazonSocial(),"productos",precios.findByProveedorId(p.getId()).stream()
                    .filter(v -> v.getProducto().getActivo()).map(v -> Map.of("id",v.getProducto().getId(),"nombre",v.getProducto().getNombre(),"precio",v.getPrecioCompra())).toList())).toList(),
            "pedidos",abastecimiento.pedidosConfirmados().stream().filter(p -> coberturas.contarPedidosCubiertos(List.of(p.getId()))==0)
                .map(p -> Map.of("id",p.getId(),"cliente",p.getCliente().getNombreComercio())).toList());
    }
    public CompraResponse autorizar(Long id, SessionResponse actor) {
        exigirRol(actor,"GERENCIA");
        var orden=buscar(id); orden.autorizar(actor.email(),clock.instant()); return CompraResponse.de(orden);
    }
    public CompraResponse emitir(Long id, SessionResponse actor) {
        exigirRol(actor,"OPERACIONES");
        var orden=buscar(id);
        for(Long productoId:orden.getDetalles().stream().map(d -> d.getProducto().getId()).sorted().toList())
            abastecimiento.bloquearProducto(productoId).orElseThrow(() -> new RecursoNoEncontradoException("Producto inexistente"));
        orden.emitir(actor.email(),clock.instant()); return CompraResponse.de(orden);
    }
    private OrdenCompra buscar(Long id) { return ordenes.bloquear(id).orElseThrow(() -> new RecursoNoEncontradoException("Compra inexistente")); }
    private void exigirConsulta(SessionResponse a) {
        if(a==null || !Set.of("OPERACIONES","GERENCIA").contains(a.rol())) throw new org.springframework.security.access.AccessDeniedException("Acceso denegado");
    }
    private void exigirRol(SessionResponse a,String rol) {
        if(a==null || !rol.equals(a.rol())) throw new org.springframework.security.access.AccessDeniedException("Acceso denegado");
    }
}
