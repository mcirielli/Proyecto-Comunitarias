package ar.edu.uade.comunitarias;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class InfrastructureTest {
    @LocalServerPort private int port;
    @Autowired private PasswordEncoder encoder;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    }

    @Test
    void healthIsPublicAndReturnsApiStatus() throws Exception {
        var response = client.send(request("/api/health").GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertTrue(response.body().contains("\"application\":\"Comunitarias\""));
        assertTrue(response.body().contains("\"status\":\"UP\""));
    }

    @Test
    void futureEndpointsRequireAuthentication() throws Exception {
        var response = client.send(request("/api/recurso-futuro").GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode());
    }

    @Test
    void localFrontendCanReadHealthWithCors() throws Exception {
        var response = client.send(request("/api/health").header("Origin", "http://localhost:5173")
                .GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("http://localhost:5173", response.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
    }

    @Test
    void localPreflightIsAccepted() throws Exception {
        var response = client.send(request("/api/health").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("http://localhost:5173", response.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
    }

    @Test
    void otherOriginsAreRejected() throws Exception {
        var response = client.send(request("/api/health").header("Origin", "https://example.invalid")
                .GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(403, response.statusCode());
        assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
    }

    @Test
    void passwordEncoderUsesBcrypt() {
        String hash = encoder.encode("test-only-password");
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(encoder.matches("test-only-password", hash));
        assertFalse(encoder.matches("wrong-password", hash));
    }
}
