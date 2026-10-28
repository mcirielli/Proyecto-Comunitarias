package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.ReservaPedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReservaPedidoRepository extends JpaRepository<ReservaPedido,Long> {
    List<ReservaPedido> findByPedidoId(Long pedidoId);
}
