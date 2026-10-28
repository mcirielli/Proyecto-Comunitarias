package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.ProductoProveedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor,Long> {
    List<ProductoProveedor> findByProveedorId(Long proveedorId);
}
