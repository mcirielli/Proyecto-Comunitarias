package ar.edu.uade.comunitarias.repository;

import ar.edu.uade.comunitarias.domain.Producto;
import ar.edu.uade.comunitarias.dto.PrecioCompraItem;
import ar.edu.uade.comunitarias.dto.PrecioProveedorItem;
import ar.edu.uade.comunitarias.dto.PrecioVentaItem;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Consultas de solo lectura sobre precios ya persistidos; no modifica ofertas, órdenes ni vínculos. */
public interface HistorialPreciosRepository extends Repository<Producto, Long> {

    @Query("""
            select new ar.edu.uade.comunitarias.dto.PrecioVentaItem(o.id, o.titulo, o.fechaInicio, o.fechaFin, d.precioVenta)
            from DetalleOferta d join d.oferta o
            where d.producto.id = :id and o.publicada = true
            order by o.fechaInicio desc, o.id desc""")
    List<PrecioVentaItem> ventas(@Param("id") Long productoId);

    @Query("""
            select new ar.edu.uade.comunitarias.dto.PrecioCompraItem(oc.id, oc.fecha, pr.razonSocial, oc.estado, oc.tipo, d.cantidad, d.precioUnitario)
            from DetalleOrdenCompra d join d.orden oc join oc.proveedor pr
            where d.producto.id = :id
            order by oc.fecha desc, oc.id desc""")
    List<PrecioCompraItem> compras(@Param("id") Long productoId);

    @Query("""
            select new ar.edu.uade.comunitarias.dto.PrecioProveedorItem(pr.id, pr.razonSocial, pp.precioCompra)
            from ProductoProveedor pp join pp.proveedor pr
            where pp.producto.id = :id
            order by pr.razonSocial""")
    List<PrecioProveedorItem> proveedores(@Param("id") Long productoId);
}
