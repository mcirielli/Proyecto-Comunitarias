package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.dto.SessionResponse;
import ar.edu.uade.comunitarias.repository.UsuarioRepository;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@Service
@Profile("supabase")
@ConditionalOnProperty(name="app.auth.demo-enabled", havingValue="false", matchIfMissing=true)

public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository repository;
    public UsuarioDetailsService(UsuarioRepository repository) { this.repository = repository; }
    @Override
    public UserDetails loadUserByUsername(String email) {
        var user = repository.findByEmail(email.trim()).orElseThrow(() -> new UsernameNotFoundException("Acceso inválido"));
        if (!user.isActivo() || user.getPasswordHash() == null || user.getPasswordHash().isBlank()
                || !Set.of("CLIENTE", "OPERACIONES", "GERENCIA").contains(user.getRol() == null ? "" : user.getRol())) {
            throw new UsernameNotFoundException("Acceso inválido");
        }
        return new UsuarioPrincipal(new SessionResponse(user.getId(), user.getNombre(), user.getEmail(), user.getRol()), user.getPasswordHash());
    }
}
