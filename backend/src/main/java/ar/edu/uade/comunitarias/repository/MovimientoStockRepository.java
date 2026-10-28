package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.MovimientoStock;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock,Long> {
    List<MovimientoStock> findByProductoIdOrderByFechaDescIdDesc(Long productoId,Pageable pagina);
}
