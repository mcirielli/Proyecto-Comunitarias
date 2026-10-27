package ar.edu.uade.comunitarias.config;
import java.time.Clock;
import org.springframework.context.annotation.*;
@Configuration
public class TiempoConfig { @Bean public Clock clock() { return Clock.systemUTC(); } }
