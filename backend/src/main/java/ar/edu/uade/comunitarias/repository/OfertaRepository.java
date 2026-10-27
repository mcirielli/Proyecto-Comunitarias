package ar.edu.uade.comunitarias.repository;
import ar.edu.uade.comunitarias.domain.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OfertaRepository extends JpaRepository<Oferta,Long> {
    List<Oferta> findByTitulo(String titulo);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select o from Oferta o where o.id = :id")
    java.util.Optional<Oferta> buscarParaPublicar(@org.springframework.data.repository.query.Param("id") Long id);
}
