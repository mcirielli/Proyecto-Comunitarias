package ar.edu.uade.comunitarias;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/** Integración explícita: mvn -Dtest=SupabaseJdbcIT test. Requiere entorno Supabase. */
@SpringBootTest
@ActiveProfiles("supabase")
@ContextConfiguration(initializers = SupabaseTestEnvironment.class)
class SupabaseJdbcIT {
    @Autowired private DataSource dataSource;

    @Test
    void inspectResolvedSchema() throws Exception {
        try (var connection = dataSource.getConnection()) {
            for (String table : new String[]{"productos", "proveedores"}) {
                try (var statement = connection.prepareStatement("""
                        SELECT n.nspname, a.attname, format_type(a.atttypid, a.atttypmod),
                               a.attnotnull, a.attidentity
                        FROM pg_attribute a JOIN pg_class c ON c.oid=a.attrelid
                        JOIN pg_namespace n ON n.oid=c.relnamespace
                        WHERE c.oid=to_regclass(?) AND a.attnum>0 AND NOT a.attisdropped
                        ORDER BY a.attnum
                        """)) {
                    statement.setString(1, table);
                    statement.setQueryTimeout(10);
                    try (var rows = statement.executeQuery()) {
                        int count = 0;
                        while (rows.next()) {
                            count++;
                            System.out.printf("SCHEMA %s.%s column=%s type=%s required=%s identity=%s%n",
                                    rows.getString(1), table, rows.getString(2), rows.getString(3),
                                    rows.getBoolean(4), rows.getString(5));
                        }
                        assertEquals(6, count,
                                "Revisar columnas reales contra mappings antes de integrar");
                    }
                }
                try (var statement = connection.prepareStatement("""
                        SELECT contype, pg_get_constraintdef(oid)
                        FROM pg_constraint WHERE conrelid=to_regclass(?) ORDER BY contype
                        """)) {
                    statement.setString(1, table);
                    statement.setQueryTimeout(10);
                    try (var rows = statement.executeQuery()) {
                        boolean primary = false;
                        boolean uniqueCuit = false;
                        while (rows.next()) {
                            String type = rows.getString(1), definition = rows.getString(2);
                            System.out.printf("CONSTRAINT %s type=%s definition=%s%n", table, type, definition);
                            primary |= type.equals("p") && definition.equals("PRIMARY KEY (id)");
                            uniqueCuit |= type.equals("u") && definition.equals("UNIQUE (cuit)");
                        }
                        assertTrue(primary, "Se requiere PK id");
                        if (table.equals("proveedores")) assertTrue(uniqueCuit, "Se requiere UNIQUE cuit");
                    }
                }
            }
        }
    }

    @Test
    void springDataSourceExecutesSelectOne() throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("SELECT 1")) {
            statement.setQueryTimeout(10);
            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
                assertFalse(result.next());
            }
        }
    }
}
