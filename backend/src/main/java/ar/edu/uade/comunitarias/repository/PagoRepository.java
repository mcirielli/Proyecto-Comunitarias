package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Pago;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface PagoRepository extends JpaRepository<Pago,Long> {
    Optional<Pago> findByClaveSolicitud(UUID clave);
    List<Pago> findByFacturaIdOrderByIdAsc(Long id);
    @Query("select coalesce(sum(p.importe),0) from Pago p where p.factura.id=:id")
    BigDecimal totalFactura(@Param("id") Long id);
}
