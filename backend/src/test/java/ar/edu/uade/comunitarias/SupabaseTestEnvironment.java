package ar.edu.uade.comunitarias;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Arrays;

/** Falla antes de inicializar JDBC; nunca imprime valores de configuración. */
public class SupabaseTestEnvironment implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (!Arrays.equals(context.getEnvironment().getActiveProfiles(), new String[]{"supabase"})) {
            throw new IllegalStateException("Integración requiere exclusivamente perfil supabase");
        }
        for (String name : new String[]{"SUPABASE_JDBC_URL", "SUPABASE_DB_USER", "SUPABASE_DB_PASSWORD"}) {
            String value = context.getEnvironment().getProperty(name);
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Falta variable de integración: " + name);
            }
        }
    }
}
