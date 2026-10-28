package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.StockAbastecimiento;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StockAbastecimientoRepository extends JpaRepository<StockAbastecimiento,Long> {
    List<StockAbastecimiento> findByProductoIdIn(Collection<Long> ids);
}
