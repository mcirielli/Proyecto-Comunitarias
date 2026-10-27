package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.config.DemoUsersConfig;
import ar.edu.uade.comunitarias.service.UsuarioPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class DemoUsersTest {
    @Test
    void tresCuentasLocalesConRolYLoginRepetible() {
        var encoder = new BCryptPasswordEncoder();
        var users = new DemoUsersConfig().demoUsers(encoder);
        for (var rol : new String[]{"CLIENTE", "OPERACIONES", "GERENCIA"}) {
            String email = (rol.equals("CLIENTE") ? "cliente" : rol.equals("GERENCIA") ? "gerencia" : "operaciones") + "@demo.local";
            var first = (UsuarioPrincipal) users.loadUserByUsername(email);
            assertEquals(rol, first.identity().rol());
            assertTrue(encoder.matches("Demo2026!", first.getPassword()));
            first.eraseCredentials();
            assertTrue(encoder.matches("Demo2026!", users.loadUserByUsername(email).getPassword()));
        }
    }
}
