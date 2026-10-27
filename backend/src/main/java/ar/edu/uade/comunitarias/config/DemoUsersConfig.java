package ar.edu.uade.comunitarias.config;

import ar.edu.uade.comunitarias.dto.SessionResponse;
import ar.edu.uade.comunitarias.service.UsuarioPrincipal;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Cuentas ficticias solo para el MVP local; no se escriben en Supabase. */
@Configuration
@Profile("supabase")
@ConditionalOnProperty(name="app.auth.demo-enabled", havingValue="true")
public class DemoUsersConfig {
    @Bean
    public UserDetailsService demoUsers(PasswordEncoder encoder) {
        String hash = encoder.encode("Demo2026!");
        var users = Map.of(
                "cliente@demo.local", new SessionResponse(-1L, "Cliente demo", "cliente@demo.local", "CLIENTE"),
                "operaciones@demo.local", new SessionResponse(-2L, "Operaciones demo", "operaciones@demo.local", "OPERACIONES"),
                "gerencia@demo.local", new SessionResponse(-3L, "Gerencia demo", "gerencia@demo.local", "GERENCIA"));
        return email -> {
            var identity = users.get(email.trim());
            if (identity == null) throw new UsernameNotFoundException("Acceso inválido");
            return new UsuarioPrincipal(identity, hash);
        };
    }
}
