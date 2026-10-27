package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Pedido;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface PedidoRepository extends JpaRepository<Pedido,Long> {
    List<Pedido> findByClienteIdOrderByIdDesc(Long clienteId);
    List<Pedido> findAllByOrderByIdDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id=:id")
    Optional<Pedido> buscarParaCambiar(Long id);
}
