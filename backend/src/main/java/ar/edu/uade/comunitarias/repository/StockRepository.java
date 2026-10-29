package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
/** Escrituras siempre bajo lock del Producto, incluso si falta su fila stock. */
public interface StockRepository extends JpaRepository<Stock,Long> {
    Optional<Stock> findByProductoId(Long productoId);
}
