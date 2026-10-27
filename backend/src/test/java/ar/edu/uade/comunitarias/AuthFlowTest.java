package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.domain.Usuario;
import ar.edu.uade.comunitarias.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.net.http.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"})
@ActiveProfiles("supabase")
class AuthFlowTest {
    @MockBean UsuarioRepository usuarios;
    @MockBean ProductoRepository productos;
    @MockBean ProveedorRepository proveedores;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper json;
    @LocalServerPort int port;
    HttpClient client;
    CookieManager cookies;
    @BeforeEach void setup() {
        cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        client=HttpClient.newBuilder().cookieHandler(cookies).build();
    }
    HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    String csrf() throws Exception { return json.readTree(get("/api/auth/csrf").body()).get("token").asText(); }
    HttpResponse<String> post(String path, String body, String token) throws Exception {
        var request=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path))
            .header("Content-Type","application/x-www-form-urlencoded");
        if(token!=null) request.header("X-CSRF-TOKEN",token);
        return client.send(request.POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    void account(String rol, boolean activo, boolean hash) {
        var user=mock(Usuario.class);
        when(user.getId()).thenReturn(1L); when(user.getNombre()).thenReturn("Demo");
        when(user.getEmail()).thenReturn("demo@example.org"); when(user.getRol()).thenReturn(rol);
        when(user.isActivo()).thenReturn(activo);
        when(user.getPasswordHash()).thenReturn(hash ? encoder.encode("test-only") : null);
        when(usuarios.findByEmail("demo@example.org")).thenReturn(Optional.of(user));
    }
    HttpResponse<String> login(String token) throws Exception {
        return post("/api/auth/login","email=demo%40example.org&password=test-only",token);
    }
    @Test void sessionLifecycleAndNoHashLeak() throws Exception {
        account("OPERACIONES",true,true);
        assertEquals(401,get("/api/auth/session").statusCode());
        assertEquals(403,login(null).statusCode());
        assertEquals(403,login("incorrecto").statusCode());
        String previousToken = csrf();
        String previousSession = cookies.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        var response=login(previousToken); assertEquals(200,response.statusCode());
        String authenticatedSession = cookies.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        assertNotEquals(previousSession, authenticatedSession);
        assertFalse(response.body().contains("password")); assertFalse(response.body().contains("$2"));
        assertEquals(200,get("/api/auth/session").statusCode());
        assertEquals(200,get("/api/productos").statusCode());
        assertEquals(403,post("/api/auth/logout","",previousToken).statusCode());
        String token=csrf();
        assertEquals(204,post("/api/auth/logout","",token).statusCode());
        assertEquals(401,get("/api/auth/session").statusCode());
    }
    @Test void wrongPasswordIsRejected() throws Exception {
        account("OPERACIONES",true,true);
        assertEquals(401,post("/api/auth/login","email=demo%40example.org&password=wrong",csrf()).statusCode());
    }
    @Test void inactiveIsRejected() throws Exception {
        account("OPERACIONES",false,true); assertEquals(401,login(csrf()).statusCode());
    }
    @Test void missingHashIsRejected() throws Exception {
        account("OPERACIONES",true,false); assertEquals(401,login(csrf()).statusCode());
    }
    @Test void unknownRoleIsRejected() throws Exception {
        account("ADMIN",true,true); assertEquals(401,login(csrf()).statusCode());
    }
    @Test void clientCannotReadOperationalMasters() throws Exception {
        account("CLIENTE",true,true); assertEquals(200,login(csrf()).statusCode());
        assertEquals(403,get("/api/productos").statusCode()); verifyNoInteractions(productos);
    }
    @Test void managerDoesNotInheritOperations() throws Exception {
        account("GERENCIA",true,true); assertEquals(200,login(csrf()).statusCode());
        assertEquals(403,get("/api/proveedores").statusCode()); verifyNoInteractions(proveedores);
    }
}
