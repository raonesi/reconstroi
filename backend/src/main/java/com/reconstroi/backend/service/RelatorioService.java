package com.reconstroi.backend.service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reconstroi.backend.dto.ApiDtos.*;
import com.reconstroi.backend.model.*;
import com.reconstroi.backend.repository.*;

@Service
public class RelatorioService {
    private final MaterialRepository materiais;
    private final ReservaRepository reservas;
    private final DoacaoRepository doacoes;
    private final UsuarioRepository usuarios;

    public RelatorioService(MaterialRepository materiais, ReservaRepository reservas, DoacaoRepository doacoes,
            UsuarioRepository usuarios) {
        this.materiais = materiais;
        this.reservas = reservas;
        this.doacoes = doacoes;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<Material> allMaterials = materiais.findAll().stream().filter(m -> !m.isExcluido()).toList();
        List<Doacao> allDonations = doacoes.findAll().stream()
            .filter(d -> d.getMaterialRelacionado() != null && d.getQuantidadeReaproveitada() != null).toList();
        List<Reserva> allReservations = reservas.findAll().stream().filter(r -> r.getMaterialRelacionado() != null).toList();

        long reused = allDonations.stream().mapToLong(Doacao::getQuantidadeReaproveitada).sum();
        BigDecimal residuos = somar(allDonations.stream().map(Doacao::getResiduosEvitadosKg).toList());
        BigDecimal peso = somar(allDonations.stream().map(Doacao::getPesoEstimadoKg).toList());
        BigDecimal co2 = somar(allDonations.stream().map(Doacao::getCo2EvitadoKg).toList());

        Map<String, Long> categories = allDonations.stream().filter(d -> d.getMaterialRelacionado().getCategoria() != null)
            .collect(Collectors.groupingBy(d -> d.getMaterialRelacionado().getCategoria().getNome(),
                TreeMap::new, Collectors.counting()));
        Map<String, Long> months = allDonations.stream().filter(d -> d.getConcluidaEm() != null)
            .collect(Collectors.groupingBy(d -> d.getConcluidaEm().atZone(ZoneId.systemDefault()).getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")), LinkedHashMap::new, Collectors.counting()));

        List<MaterialRanking> maisDoados = rankingQuantidade(allDonations);
        List<MaterialRanking> maisProcurados = allReservations.stream()
            .collect(Collectors.groupingBy(r -> r.getMaterialRelacionado().getNome(), Collectors.counting()))
            .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(5)
            .map(e -> new MaterialRanking(e.getKey(), e.getValue())).toList();

        return new DashboardResponse(allMaterials.size(),
            allMaterials.stream().filter(m -> m.getStatus() == StatusMaterial.DISPONIVEL).count(), reused,
            allReservations.size(), allDonations.size(), usuarios.countByAtivoTrue(),
            usuarios.countByTipo(TipoUsuario.BENEFICIARIO_ONG), usuarios.countByTipo(TipoUsuario.DOADOR_CONSTRUTORA),
            usuarios.countByTipo(TipoUsuario.BENEFICIARIO_FAMILIA), reused, residuos, peso, co2,
            categories, months, maisDoados, maisProcurados);
    }

    private BigDecimal somar(List<BigDecimal> valores) {
        return valores.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<MaterialRanking> rankingQuantidade(List<Doacao> doacoes) {
        return doacoes.stream().collect(Collectors.groupingBy(d -> d.getMaterialRelacionado().getNome(),
                Collectors.summingLong(d -> d.getQuantidadeReaproveitada())))
            .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(5)
            .map(e -> new MaterialRanking(e.getKey(), e.getValue())).toList();
    }
}
