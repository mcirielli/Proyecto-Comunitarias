package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra,Long> {
    List<OrdenCompra> findAllByOrderByIdDesc();
    List<OrdenCompra> findByEstadoIn(Collection<EstadoCompra> estados);
    boolean existsByClaveSolicitud(UUID clave);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrdenCompra o where o.id=:id")
    Optional<OrdenCompra> bloquear(Long id);
}
