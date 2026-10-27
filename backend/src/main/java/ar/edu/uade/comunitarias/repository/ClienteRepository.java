package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ClienteRepository extends JpaRepository<Cliente,Long> {
    Optional<Cliente> findByUsuarioId(Long usuarioId);
    List<Cliente> findByNombreComercio(String nombre);
}
