package ar.edu.uade.comunitarias.controller;
import ar.edu.uade.comunitarias.dto.SessionResponse;
import ar.edu.uade.comunitarias.service.UsuarioPrincipal;
import jakarta.servlet.http.*;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.*;
import org.springframework.web.bind.annotation.*;
@RestController
@Profile("supabase")
@RequestMapping("/api/auth")
public class AuthController {
    @GetMapping("/csrf")
    public ResponseEntity<Map<String,String>> csrf(CsrfToken token) {
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .body(Map.of("token", token.getToken(), "headerName", token.getHeaderName()));
    }
    @GetMapping("/session")
    public ResponseEntity<SessionResponse> session(Authentication authentication) {
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .body(((UsuarioPrincipal) authentication.getPrincipal()).identity());
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        new HttpSessionCsrfTokenRepository().saveToken(null, request, response);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        var cookie = new Cookie("JSESSIONID", "");
        cookie.setPath("/"); cookie.setHttpOnly(true); cookie.setSecure(request.isSecure()); cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.noContent().header("Cache-Control", "no-store").build();
    }
}
