package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Factura;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
public interface FacturaRepository extends JpaRepository<Factura,Long> {
    Optional<Factura> findByPedidoId(Long id);
    List<Factura> findAllByOrderByIdDesc();
    List<Factura> findByPedidoClienteIdOrderByIdDesc(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select f from Factura f where f.id=:id")
    Optional<Factura> bloquear(@Param("id") Long id);
}
