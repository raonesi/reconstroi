package VIEW;

import DTO.DashboardDTO;
import DTO.DoacaoDTO;
import DTO.MaterialDTO;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Gera o relatório do ReConstrói em PDF ou CSV (compatível com o Excel em português). */
public final class GeradorRelatorio {
    /** Dados reunidos para o relatório. Listas vazias indicam seções não selecionadas. */
    public record Dados(DashboardDTO resumo, List<MaterialDTO> materiais, List<DoacaoDTO> doacoes, String emitidoPor) {}

    private static final Locale BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", BR);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", BR);
    private static final Color VERDE = new Color(22, 128, 58);
    private static final Color VERDE_CLARO = new Color(234, 245, 237);
    private static final Color CINZA = new Color(94, 112, 99);

    private GeradorRelatorio() {}

    // ------------------------------------------------------------------ PDF

    public static void gerarPdf(File arquivo, Dados dados) throws IOException {
        Document documento = new Document(PageSize.A4, 40, 40, 48, 48);
        try (OutputStream saida = new FileOutputStream(arquivo)) {
            PdfWriter escritor = PdfWriter.getInstance(documento, saida);
            escritor.setPageEvent(new Rodape(dados.emitidoPor()));
            documento.addTitle("Relatório ReConstrói");
            documento.addAuthor(dados.emitidoPor());
            documento.open();

            Paragraph titulo = new Paragraph("Relatório ReConstrói", fonte(20, Font.BOLD, VERDE));
            documento.add(titulo);
            documento.add(new Paragraph("Reaproveitamento de materiais de construção — emitido em "
                + LocalDateTime.now().format(DATA_HORA), fonte(10, Font.NORMAL, CINZA)));
            documento.add(espaco(10));

            DashboardDTO r = dados.resumo();
            secao(documento, "Indicadores gerais");
            PdfPTable indicadores = tabela(new float[] {3, 1.4f}, "Indicador", "Valor");
            linha(indicadores, "Materiais cadastrados", numero(r.getMateriaisCadastrados()));
            linha(indicadores, "Materiais disponíveis", numero(r.getMateriaisDisponiveis()));
            linha(indicadores, "Materiais reutilizados", numero(r.getMateriaisReutilizados()));
            linha(indicadores, "Reservas", numero(r.getReservas()));
            linha(indicadores, "Doações concluídas", numero(r.getDoacoesConcluidas()));
            linha(indicadores, "Quantidade reaproveitada", numero(r.getQuantidadeReaproveitada()));
            linha(indicadores, "Usuários cadastrados", numero(r.getUsuarios()));
            linha(indicadores, "ONGs / Construtoras / Famílias",
                r.getOngs() + " / " + r.getConstrutoras() + " / " + r.getFamilias());
            documento.add(indicadores);

            secao(documento, "Impacto ambiental");
            PdfPTable impacto = tabela(new float[] {3, 1.4f}, "Indicador", "Valor");
            linha(impacto, "Peso estimado reaproveitado", kg(r.getPesoEstimadoKg()));
            linha(impacto, "Resíduos evitados", kg(r.getResiduosEvitadosKg()));
            linha(impacto, "CO2 evitado", kg(r.getCo2EvitadoKg()));
            documento.add(impacto);

            rankingPdf(documento, "Categorias mais utilizadas", "Categoria", r.getCategoriasMaisUtilizadas());
            rankingPdf(documento, "Doações por mês", "Mês", r.getDoacoesPorMes());
            listaRankingPdf(documento, "Materiais mais doados", r.getMateriaisMaisDoados());
            listaRankingPdf(documento, "Materiais mais procurados", r.getMateriaisMaisProcurados());

            if (!dados.materiais().isEmpty()) {
                secao(documento, "Materiais (" + dados.materiais().size() + ")");
                PdfPTable t = tabela(new float[] {2.5f, 1.5f, 1.0f, 1.7f, 1.7f, 1.3f},
                    "Material", "Categoria", "Qtd.", "Cidade/UF", "Conservação", "Status");
                for (MaterialDTO m : dados.materiais()) {
                    linha(t, m.getNome(), m.getCategoria() == null ? "—" : m.getCategoria().getNome(),
                        m.getQuantidade() + " " + texto(m.getUnidade()),
                        texto(m.getCidade()) + "/" + texto(m.getEstado()),
                        Estilo.rotuloStatus(m.getEstadoConservacao()), Estilo.rotuloStatus(m.getStatus()));
                }
                documento.add(t);
            }

            if (!dados.doacoes().isEmpty()) {
                secao(documento, "Doações (" + dados.doacoes().size() + ")");
                PdfPTable t = tabela(new float[] {2.2f, 1.8f, 1.8f, 0.8f, 1.2f, 1.3f},
                    "Material", "Doador", "Beneficiário", "Qtd.", "CO2 evitado", "Conclusão");
                for (DoacaoDTO d : dados.doacoes()) {
                    linha(t, d.getMaterial() == null ? "—" : d.getMaterial().getNome(),
                        d.getDoador() == null ? "—" : d.getDoador().getNome(),
                        d.getBeneficiario() == null ? "—" : d.getBeneficiario().getNome(),
                        texto(d.getQuantidade()), kg(d.getCo2EvitadoKg()), data(d.getConcluidaEm()));
                }
                documento.add(t);
            }
            // Fecha o documento ANTES do try-with-resources fechar o arquivo (o close grava o final do PDF).
            documento.close();
        } finally {
            if (documento.isOpen()) {
                try { documento.close(); } catch (RuntimeException ignorado) { /* arquivo já fechado após erro */ }
            }
        }
    }

    private static void rankingPdf(Document doc, String titulo, String coluna, Map<String, Long> valores) {
        if (valores == null || valores.isEmpty()) return;
        secao(doc, titulo);
        PdfPTable t = tabela(new float[] {3, 1.4f}, coluna, "Quantidade");
        valores.forEach((nome, qtd) -> linha(t, nome, numero(qtd)));
        doc.add(t);
    }

    private static void listaRankingPdf(Document doc, String titulo, List<DashboardDTO.RankingDTO> itens) {
        if (itens == null || itens.isEmpty()) return;
        secao(doc, titulo);
        PdfPTable t = tabela(new float[] {3, 1.4f}, "Material", "Quantidade");
        for (DashboardDTO.RankingDTO i : itens) linha(t, i.getNome(), numero(i.getQuantidade()));
        doc.add(t);
    }

    private static void secao(Document doc, String titulo) {
        Paragraph p = new Paragraph(titulo, fonte(13, Font.BOLD, new Color(19, 36, 25)));
        p.setSpacingBefore(14);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private static PdfPTable tabela(float[] larguras, String... cabecalhos) {
        PdfPTable t = new PdfPTable(larguras);
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (String c : cabecalhos) {
            PdfPCell celula = new PdfPCell(new Phrase(c, fonte(9, Font.BOLD, Color.WHITE)));
            celula.setBackgroundColor(VERDE);
            celula.setPadding(6);
            celula.setBorderColor(VERDE);
            t.addCell(celula);
        }
        return t;
    }

    private static void linha(PdfPTable t, String... valores) {
        boolean par = (t.size() % 2) == 0;
        for (int i = 0; i < valores.length; i++) {
            PdfPCell celula = new PdfPCell(new Phrase(valores[i] == null ? "—" : valores[i], fonte(9, Font.NORMAL, Color.BLACK)));
            celula.setPadding(5);
            celula.setBorderColor(new Color(223, 231, 225));
            if (!par) celula.setBackgroundColor(VERDE_CLARO);
            if (i > 0 && valores.length == 2) celula.setHorizontalAlignment(Element.ALIGN_RIGHT);
            t.addCell(celula);
        }
    }

    private static Paragraph espaco(float altura) {
        Paragraph p = new Paragraph(new Chunk(" "));
        p.setLeading(altura);
        return p;
    }

    private static Font fonte(float tamanho, int estilo, Color cor) {
        return FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", true, tamanho, estilo, cor);
    }

    /** Rodapé com número da página e quem emitiu. */
    private static final class Rodape extends PdfPageEventHelper {
        private final String emitidoPor;
        Rodape(String emitidoPor) { this.emitidoPor = emitidoPor; }

        @Override public void onEndPage(PdfWriter escritor, Document documento) {
            var cb = escritor.getDirectContent();
            Phrase texto = new Phrase("ReConstrói — emitido por " + emitidoPor + "   ·   página " + escritor.getPageNumber(),
                fonte(8, Font.NORMAL, CINZA));
            com.lowagie.text.pdf.ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, texto,
                (documento.left() + documento.right()) / 2, documento.bottom() - 24, 0);
        }
    }

    // ------------------------------------------------------------------ CSV

    /** CSV em UTF-8 com BOM e ";" como separador, para abrir corretamente no Excel em português. */
    public static void gerarCsv(File arquivo, Dados dados) throws IOException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
            out.print('﻿');
            DashboardDTO r = dados.resumo();
            csv(out, "Relatório ReConstrói", "Emitido em " + LocalDateTime.now().format(DATA_HORA), "Por " + dados.emitidoPor());
            out.println();
            csv(out, "Indicador", "Valor");
            csv(out, "Materiais cadastrados", r.getMateriaisCadastrados());
            csv(out, "Materiais disponíveis", r.getMateriaisDisponiveis());
            csv(out, "Materiais reutilizados", r.getMateriaisReutilizados());
            csv(out, "Reservas", r.getReservas());
            csv(out, "Doações concluídas", r.getDoacoesConcluidas());
            csv(out, "Quantidade reaproveitada", r.getQuantidadeReaproveitada());
            csv(out, "Usuários", r.getUsuarios());
            csv(out, "ONGs", r.getOngs());
            csv(out, "Construtoras", r.getConstrutoras());
            csv(out, "Famílias", r.getFamilias());
            csv(out, "Peso estimado (kg)", decimal(r.getPesoEstimadoKg()));
            csv(out, "Resíduos evitados (kg)", decimal(r.getResiduosEvitadosKg()));
            csv(out, "CO₂ evitado (kg)", decimal(r.getCo2EvitadoKg()));
            out.println();
            csv(out, "Categoria", "Quantidade");
            if (r.getCategoriasMaisUtilizadas() != null) r.getCategoriasMaisUtilizadas().forEach((k, v) -> csv(out, k, v));

            if (!dados.materiais().isEmpty()) {
                out.println();
                csv(out, "ID", "Material", "Categoria", "Quantidade", "Unidade", "Cidade", "UF", "Conservação", "Status", "Responsável");
                for (MaterialDTO m : dados.materiais()) csv(out, m.getId(), m.getNome(),
                    m.getCategoria() == null ? "" : m.getCategoria().getNome(), m.getQuantidade(), m.getUnidade(),
                    m.getCidade(), m.getEstado(), Estilo.rotuloStatus(m.getEstadoConservacao()),
                    Estilo.rotuloStatus(m.getStatus()), m.getProprietario() == null ? "" : m.getProprietario().getNome());
            }
            if (!dados.doacoes().isEmpty()) {
                out.println();
                csv(out, "ID", "Material", "Doador", "Beneficiário", "Quantidade", "Peso (kg)", "Resíduos (kg)", "CO₂ (kg)", "Conclusão");
                for (DoacaoDTO d : dados.doacoes()) csv(out, d.getId(),
                    d.getMaterial() == null ? "" : d.getMaterial().getNome(),
                    d.getDoador() == null ? "" : d.getDoador().getNome(),
                    d.getBeneficiario() == null ? "" : d.getBeneficiario().getNome(), d.getQuantidade(),
                    decimal(d.getPesoEstimadoKg()), decimal(d.getResiduosEvitadosKg()), decimal(d.getCo2EvitadoKg()),
                    data(d.getConcluidaEm()));
            }
        }
    }

    private static void csv(PrintWriter out, Object... valores) {
        List<String> campos = new ArrayList<>();
        for (Object v : valores) {
            String s = v == null ? "" : String.valueOf(v);
            if (s.contains(";") || s.contains("\"") || s.contains("\n")) s = "\"" + s.replace("\"", "\"\"") + "\"";
            campos.add(s);
        }
        out.print(String.join(";", campos));
        out.print("\r\n");
    }

    // ------------------------------------------------------------------ formatação

    private static String numero(long valor) { return NumberFormat.getIntegerInstance(BR).format(valor); }

    private static String decimal(BigDecimal valor) {
        NumberFormat f = NumberFormat.getNumberInstance(BR);
        f.setMinimumFractionDigits(2);
        f.setMaximumFractionDigits(2);
        f.setGroupingUsed(false);
        return f.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private static String kg(BigDecimal valor) {
        NumberFormat f = NumberFormat.getNumberInstance(BR);
        f.setMaximumFractionDigits(2);
        return f.format(valor == null ? BigDecimal.ZERO : valor) + " kg";
    }

    private static String texto(Object valor) { return valor == null ? "—" : String.valueOf(valor); }

    private static String data(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        try { return OffsetDateTime.parse(iso).atZoneSameInstant(java.time.ZoneId.systemDefault()).format(DATA); }
        catch (Exception e) { return iso.length() >= 10 ? iso.substring(0, 10) : iso; }
    }
}
