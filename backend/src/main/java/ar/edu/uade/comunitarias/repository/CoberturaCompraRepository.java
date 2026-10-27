package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.CoberturaCompra;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface CoberturaCompraRepository extends JpaRepository<CoberturaCompra,Long> {
    boolean existsByClaveSolicitud(UUID clave);
    @Query("select count(c) from CoberturaCompra c join c.pedidos p where p in :ids")
    long contarPedidosCubiertos(Collection<Long> ids);
}
