package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Producto;
import ar.edu.uade.comunitarias.domain.Pedido;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.Repository;
/** Queries de compras sin modificar repositorios de otros módulos. */
public interface CompraAbastecimientoRepository extends Repository<Producto,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id=:id")
    Optional<Producto> bloquearProducto(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id=:id")
    Optional<Pedido> bloquearPedido(Long id);
    @Query("select p from Pedido p where p.estado=ar.edu.uade.comunitarias.state.EstadoPedido.CONFIRMADO order by p.id")
    List<Pedido> pedidosConfirmados();
}
