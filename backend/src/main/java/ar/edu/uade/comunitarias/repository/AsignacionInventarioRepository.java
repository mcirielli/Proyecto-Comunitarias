package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.AsignacionInventario;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AsignacionInventarioRepository extends JpaRepository<AsignacionInventario,Long> {
    List<AsignacionInventario> findByProductoIdIn(Collection<Long> productos);
    List<AsignacionInventario> findByPedidoId(Long pedidoId);
    List<AsignacionInventario> findByOrdenId(Long ordenId);
}
