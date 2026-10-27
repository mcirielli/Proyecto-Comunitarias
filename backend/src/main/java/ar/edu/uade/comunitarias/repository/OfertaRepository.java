package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OfertaRepository extends JpaRepository<Oferta,Long> { List<Oferta> findByTitulo(String titulo); }
