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
    "app.facturacion.enabled=true",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"})
@ActiveProfiles("supabase")
class FacturacionHttpTest {
    @MockBean ar.edu.uade.comunitarias.service.FacturacionService facturas;
    @MockBean ar.edu.uade.comunitarias.service.PagoService pagos;
    @MockBean UsuarioRepository usuarios;
    @MockBean ClienteRepository clientes;
    @MockBean OfertaRepository ofertas;
    @MockBean PedidoRepository pedidos;
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

    HttpResponse<String> jsonPost(String path,String body,String token)throws Exception{
        var req=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).header("Content-Type","application/json");
        if(token!=null)req.header("X-CSRF-TOKEN",token);
        return client.send(req.POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    @Test void sinSesion401YEscrituraSinCsrf403()throws Exception{
        assertEquals(401,get("/api/facturas").statusCode());account("OPERACIONES",true,true);assertEquals(200,login(csrf()).statusCode());
        assertEquals(403,jsonPost("/api/facturas","{\"pedidoId\":1}",null).statusCode());assertEquals(403,jsonPost("/api/pagos","{}",null).statusCode());verifyNoInteractions(facturas,pagos);
    }
    @Test void clienteYGerenciaSoloConsultan()throws Exception{
        for(String rol:new String[]{"CLIENTE","GERENCIA"}){
            account(rol,true,true);assertEquals(200,login(csrf()).statusCode());assertEquals(200,get("/api/facturas").statusCode());assertEquals(403,get("/api/facturas/contexto").statusCode());
            assertEquals(403,jsonPost("/api/facturas","{\"pedidoId\":1}",csrf()).statusCode());assertEquals(403,jsonPost("/api/pagos","{}",csrf()).statusCode());post("/api/auth/logout","",csrf());
        }
        verify(facturas,never()).emitir(any(),any());verifyNoInteractions(pagos);
    }
    @Test void operacionesPuedeEmitirConCsrfYValidaBody()throws Exception{
        account("OPERACIONES",true,true);assertEquals(200,login(csrf()).statusCode());
        assertEquals(400,jsonPost("/api/facturas","{\"pedidoId\":0}",csrf()).statusCode());assertEquals(200,jsonPost("/api/facturas","{\"pedidoId\":1}",csrf()).statusCode());
        assertEquals(400,jsonPost("/api/pagos","{}",csrf()).statusCode());verify(facturas,times(1)).emitir(any(),any());
    }
    @Test void respuestaComercialNoContieneCostos()throws Exception{
        account("CLIENTE",true,true);assertEquals(200,login(csrf()).statusCode());
        when(facturas.listar(any())).thenReturn(java.util.List.of(ar.edu.uade.comunitarias.dto.FacturaResponse.de(FacturacionFixture.factura(),java.util.List.of())));
        var res=get("/api/facturas");assertEquals(200,res.statusCode());assertFalse(res.body().contains("precioCompra"));assertFalse(res.body().contains("password"));assertFalse(res.body().contains("proveedor"));assertTrue(res.body().contains("precioUnitario"));
    }
}
