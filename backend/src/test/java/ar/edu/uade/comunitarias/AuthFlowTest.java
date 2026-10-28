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
    "app.compras.enabled=true",
    "app.inventario.enabled=true",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"})
@ActiveProfiles("supabase")
class AuthFlowTest {
    @MockBean ar.edu.uade.comunitarias.service.CompraService compras;
    @MockBean ar.edu.uade.comunitarias.service.StockService inventario;
    @MockBean ar.edu.uade.comunitarias.service.CoberturaInventarioService coberturaInventario;
    @MockBean ar.edu.uade.comunitarias.service.StockConsultaService consultaInventario;
    @MockBean ar.edu.uade.comunitarias.service.RecepcionService recepcionInventario;
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
    @Test void inventarioConservaRolesYCsrf() throws Exception {
        assertEquals(401,get("/api/stock").statusCode());
        account("CLIENTE",true,true);assertEquals(200,login(csrf()).statusCode());
        assertEquals(403,get("/api/stock").statusCode());
        assertEquals(403,post("/api/compras/1/recepcion","",csrf()).statusCode());
        post("/api/auth/logout","",csrf());
        account("GERENCIA",true,true);assertEquals(200,login(csrf()).statusCode());
        assertEquals(200,get("/api/stock").statusCode());
        assertEquals(403,post("/api/compras/1/recepcion","",csrf()).statusCode());
        post("/api/auth/logout","",csrf());
        account("OPERACIONES",true,true);assertEquals(200,login(csrf()).statusCode());
        assertEquals(403,post("/api/compras/1/recepcion","",null).statusCode());
        assertEquals(400,post("/api/compras/1/recepcion","cantidad=99",csrf()).statusCode());
        verifyNoInteractions(recepcionInventario);
        assertEquals(200,post("/api/compras/1/recepcion","",csrf()).statusCode());
        verify(recepcionInventario).recibir(eq(1L),any());
    }
    @Test void conflictoRecepcionSeInformaSinExitoFalso() throws Exception {
        account("OPERACIONES",true,true);assertEquals(200,login(csrf()).statusCode());
        when(recepcionInventario.recibir(eq(1L),any())).thenThrow(new ar.edu.uade.comunitarias.domain.ReglaNegocioException("Orden ya recibida"));
        assertEquals(409,post("/api/compras/1/recepcion","",csrf()).statusCode());
    }
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
        assertEquals(403,get("/api/pedidos").statusCode());
    }
    @Test void clienteNoPuedeCambiarEstadoAunqueTengaCsrf() throws Exception {
        account("CLIENTE",true,true); assertEquals(200,login(csrf()).statusCode());
        var request=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/pedidos/1/estado"))
                .header("Content-Type","application/json").header("X-CSRF-TOKEN",csrf())
                .method("PATCH",HttpRequest.BodyPublishers.ofString("{\"estado\":\"ENTREGADO\"}")).build();
        assertEquals(403,client.send(request,HttpResponse.BodyHandlers.ofString()).statusCode());
        verifyNoInteractions(pedidos);
    }
    HttpResponse<String> compraPost(String path, String body, String token) throws Exception {
        var request=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).header("Content-Type","application/json");
        if(token!=null) request.header("X-CSRF-TOKEN",token);
        return client.send(request.POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    @Test void clienteNoVeCostosNiEjecutaCompras() throws Exception {
        account("CLIENTE",true,true); assertEquals(200,login(csrf()).statusCode());
        assertEquals(403,get("/api/compras").statusCode());
        assertEquals(403,get("/api/compras/disponibilidad").statusCode());
        assertEquals(403,compraPost("/api/compras/quinteros","{}",csrf()).statusCode());
        verifyNoInteractions(compras);
    }
    @Test void gerenciaAutorizaSinHeredarOperaciones() throws Exception {
        account("GERENCIA",true,true); assertEquals(200,login(csrf()).statusCode());
        assertEquals(200,get("/api/compras").statusCode());
        assertEquals(403,get("/api/compras/contexto").statusCode());
        assertEquals(403,compraPost("/api/compras/quinteros","{}",csrf()).statusCode());
        assertEquals(403,compraPost("/api/compras/1/emitir","{}",csrf()).statusCode());
        assertEquals(200,compraPost("/api/compras/1/autorizar","{}",csrf()).statusCode());
        verify(compras).autorizar(eq(1L),argThat(a -> a.rol().equals("GERENCIA")));
    }
    @Test void operacionesNoAutorizaYSigueNecesitandoCsrf() throws Exception {
        account("OPERACIONES",true,true); assertEquals(200,login(csrf()).statusCode());
        assertEquals(403,compraPost("/api/compras/1/autorizar","{}",csrf()).statusCode());
        assertEquals(403,compraPost("/api/compras/1/emitir","{}",null).statusCode());
        assertEquals(200,compraPost("/api/compras/1/emitir","{}",csrf()).statusCode());
        verify(compras).emitir(eq(1L),argThat(a -> a.rol().equals("OPERACIONES")));
    }
    @Test void cantidadesInvalidasSon400AntesDeEscritura() throws Exception {
        account("OPERACIONES",true,true); assertEquals(200,login(csrf()).statusCode());
        var body="{\"claveSolicitud\":\"00000000-0000-0000-0000-000000000001\",\"proveedorId\":1,\"pedidos\":[],\"productos\":[{\"productoId\":1,\"cantidad\":0}]}";
        assertEquals(400,compraPost("/api/compras/quinteros",body,csrf()).statusCode());
        verifyNoInteractions(compras);
    }

    @Test void ofertasRespetanRolesCsrfYContratoComercial() throws Exception {
        assertEquals(401,get("/api/ofertas").statusCode());
        account("CLIENTE",true,true); assertEquals(200,login(csrf()).statusCode());
        assertEquals(200,get("/api/ofertas").statusCode());
        assertEquals(403,post("/api/ofertas/1/publicacion","",csrf()).statusCode());
        assertEquals(403,post("/api/ofertas","",csrf()).statusCode());
        assertEquals(403,get("/api/ofertas/1").statusCode());
        post("/api/auth/logout","",csrf());
        account("GERENCIA",true,true); assertEquals(200,login(csrf()).statusCode());
        assertEquals(403,get("/api/ofertas").statusCode());
        assertEquals(403,post("/api/ofertas/1/publicacion","",csrf()).statusCode());
        post("/api/auth/logout","",csrf());
        account("OPERACIONES",true,true); assertEquals(200,login(csrf()).statusCode());
        assertEquals(200,get("/api/ofertas").statusCode());
        assertEquals(403,post("/api/ofertas/1/publicacion","",null).statusCode());
        assertEquals(403,post("/api/ofertas","",null).statusCode());
        verify(ofertas,never()).buscarParaPublicar(any());
    }
}
