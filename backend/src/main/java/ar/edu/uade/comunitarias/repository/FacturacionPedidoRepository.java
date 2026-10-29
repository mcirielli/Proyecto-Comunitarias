package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Pedido;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
/** Lectura y lock de emisión; no altera Pedido/State ni repositorios de Andy. */
public interface FacturacionPedidoRepository extends Repository<Pedido,Long> {
    List<Pedido> findByEstadoOrderByIdDesc(EstadoPedido estado);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Pedido p where p.id=:id")
    Optional<Pedido> bloquear(@Param("id") Long id);
}
