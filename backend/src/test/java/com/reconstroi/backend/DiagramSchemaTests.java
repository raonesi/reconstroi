package com.reconstroi.backend;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class DiagramSchemaTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;

    @Test
    void tabelasColunasTiposEChavesCorrespondemAoAstah() throws Exception {
        var expected = mapper.readTree(getClass().getResourceAsStream("/astah-schema.json"));
        Set<String> tables = new HashSet<>();
        expected.fieldNames().forEachRemaining(tables::add);
        assertThat(jdbc.queryForList("SELECT tablename FROM pg_tables WHERE schemaname='public'", String.class))
            .containsExactlyInAnyOrderElementsOf(tables);
        Map<String, String> types = Map.of("Long", "bigint", "int", "integer", "String", "character varying",
            "boolean", "boolean", "Decimal", "numeric");
        Map<String, String> targets = Map.ofEntries(Map.entry("usuario_id", "usuario"),
            Map.entry("proprietario_id", "usuario"), Map.entry("solicitante_id", "usuario"),
            Map.entry("doador_id", "usuario"), Map.entry("beneficiario_id", "usuario"),
            Map.entry("denunciante_id", "usuario"), Map.entry("material_id", "material"),
            Map.entry("categoria_id", "categoria"), Map.entry("reserva_id", "reserva"));
        for (String table : tables) {
            Map<String, String> actual = new HashMap<>();
            jdbc.query("SELECT column_name,data_type FROM information_schema.columns WHERE table_schema='public' AND table_name=?",
                rs -> { actual.put(rs.getString(1), rs.getString(2)); }, table);
            Set<String> columns = new HashSet<>();
            for (var column : expected.get(table)) {
                String label = column.get("name").asText();
                String name = label.split(" ")[0];
                String type = column.get("type").asText();
                columns.add(name);
                if (type.equals("DateTime")) assertThat(actual.get(name)).as(table + "." + name)
                    .isIn("timestamp with time zone", "timestamp without time zone");
                else assertThat(actual.get(name)).as(table + "." + name).isEqualTo(types.get(type));
                if (label.contains("FK")) {
                    assertThat(jdbc.queryForList("SELECT ccu.table_name FROM information_schema.table_constraints tc "
                        + "JOIN information_schema.key_column_usage kcu USING (constraint_catalog,constraint_schema,constraint_name) "
                        + "JOIN information_schema.constraint_column_usage ccu USING (constraint_catalog,constraint_schema,constraint_name) "
                        + "WHERE tc.constraint_type='FOREIGN KEY' AND tc.table_schema='public' AND tc.table_name=? AND kcu.column_name=?",
                        String.class, table, name)).contains(targets.get(name));
                }
                if (label.contains("PK") || label.contains("UNIQUE")) {
                    assertThat(jdbc.queryForObject("SELECT count(*) FROM pg_index i JOIN pg_class t ON t.oid=i.indrelid "
                        + "JOIN pg_attribute a ON a.attrelid=t.oid AND a.attnum=ANY(i.indkey) "
                        + "WHERE t.relnamespace='public'::regnamespace AND t.relname=? AND a.attname=? AND i.indisunique AND i.indnkeyatts=1 AND i.indpred IS NULL",
                        Integer.class, table, name)).isPositive();
                }
            }
            assertThat(actual.keySet()).as(table).containsExactlyInAnyOrderElementsOf(columns);
        }
    }
}
