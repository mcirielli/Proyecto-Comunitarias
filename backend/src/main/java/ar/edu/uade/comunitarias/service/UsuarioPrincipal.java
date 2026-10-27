package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.dto.SessionResponse;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
public class UsuarioPrincipal extends User {
    private final SessionResponse identity;
    public UsuarioPrincipal(SessionResponse identity, String hash) {
        super(identity.email(), hash, List.of(new SimpleGrantedAuthority("ROLE_" + identity.rol())));
        this.identity = identity;
    }
    public SessionResponse identity() { return identity; }
}
