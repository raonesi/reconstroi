package com.reconstroi.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import org.springframework.stereotype.Service;
import com.reconstroi.backend.model.Material;

@Service
public class ImpactoAmbientalService {
    private static final BigDecimal FATOR_CO2 = new BigDecimal("0.42");

    public Impacto calcular(Material material, int quantidade) {
        BigDecimal peso = BigDecimal.valueOf(quantidade).multiply(fatorPeso(material)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal residuos = peso;
        BigDecimal co2 = peso.multiply(FATOR_CO2).setScale(2, RoundingMode.HALF_UP);
        return new Impacto(peso, residuos, co2);
    }

    private BigDecimal fatorPeso(Material material) {
        String unidade = material.getUnidade().trim().toLowerCase(Locale.ROOT);
        if (unidade.equals("kg") || unidade.equals("quilo") || unidade.equals("quilos")) return BigDecimal.ONE;
        if (unidade.equals("t") || unidade.contains("tonelada")) return new BigDecimal("1000");
        if (unidade.contains("m³") || unidade.contains("m3")) return new BigDecimal("1000");
        if (unidade.contains("m²") || unidade.contains("m2")) return new BigDecimal("20");
        if (unidade.contains("litro")) return new BigDecimal("1.3");
        if (unidade.contains("saco")) return new BigDecimal("25");
        if (unidade.contains("caixa")) return new BigDecimal("15");
        String categoria = material.getCategoria() == null ? "" : material.getCategoria().getSlug();
        return switch (categoria) {
            case "tijolos", "telhas" -> new BigDecimal("2.5");
            case "portas", "janelas" -> new BigDecimal("20");
            case "pisos" -> new BigDecimal("3");
            case "tubos", "ferro", "madeira" -> new BigDecimal("5");
            default -> BigDecimal.ONE;
        };
    }

    public record Impacto(BigDecimal pesoEstimadoKg, BigDecimal residuosEvitadosKg, BigDecimal co2EvitadoKg) {}
}
