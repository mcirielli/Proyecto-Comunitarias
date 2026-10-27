package ar.edu.uade.comunitarias.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.databind.ObjectMapper;
import ar.edu.uade.comunitarias.service.UsuarioPrincipal;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, Environment environment, ObjectMapper json) throws Exception {
        boolean loginEnabled = environment.matchesProfiles("supabase");
        http
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ofertas").hasAnyRole("CLIENTE", "OPERACIONES")
                        .requestMatchers("/api/ofertas", "/api/ofertas/**").hasRole("OPERACIONES")
                        .requestMatchers("/api/productos", "/api/productos/**", "/api/proveedores", "/api/proveedores/**").hasRole("OPERACIONES")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/estado").hasRole("OPERACIONES")
                        .requestMatchers("/api/demo/**").hasRole("OPERACIONES")
                        .requestMatchers("/api/pedidos", "/api/pedidos/**").hasAnyRole("CLIENTE", "OPERACIONES")
                        .anyRequest().authenticated())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Sesión requerida\"}");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Acceso denegado\"}");
                        }))
                .requestCache(cache -> cache.disable());
        if (loginEnabled) {
            http.formLogin(form -> form.loginProcessingUrl("/api/auth/login")
                    .usernameParameter("email")
                    .successHandler((request, response, authentication) -> {
                        response.setContentType("application/json");
                        response.setHeader("Cache-Control", "no-store");
                        json.writeValue(response.getWriter(), ((UsuarioPrincipal) authentication.getPrincipal()).identity());
                    })
                    .failureHandler((request, response, exception) -> {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Credenciales inválidas\"}");
                    }).permitAll());
        }
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String origins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origins.split(","))
                .map(String::trim).filter(origin -> !origin.isEmpty()).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN", "X-XSRF-TOKEN"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        return source;
    }
}
