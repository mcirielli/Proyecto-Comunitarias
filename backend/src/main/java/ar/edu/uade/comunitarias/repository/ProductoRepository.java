package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductoRepository extends JpaRepository<Producto, Long> {}
