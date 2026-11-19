package VIEW;

import DAO.RelatorioDAO;
import DTO.DashboardDTO;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardVIEW extends JPanel implements Atualizavel {
    private static final NumberFormat NUMERO = NumberFormat.getIntegerInstance(new Locale("pt", "BR"));
    private static final NumberFormat DECIMAL = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
    static { DECIMAL.setMaximumFractionDigits(1); }

    private final JLabel materiais = valor();
    private final JLabel disponiveis = valor();
    private final JLabel reutilizados = valor();
    private final JLabel reservas = valor();
    private final JLabel doacoes = valor();
    private final JLabel usuarios = valor();
    private final JLabel impacto = valor();
    private final JLabel residuos = valor();
    private final GraficoBarras grafico = new GraficoBarras();
    private final JLabel perfis = new JLabel(" ");

    public DashboardVIEW() {
        setLayout(new BorderLayout());
        setBackground(Estilo.FUNDO);
        JPanel pagina = Estilo.pagina("Visão geral", "Indicadores da plataforma e impacto ambiental das doações.");

        JPanel corpo = new JPanel(new BorderLayout(0, 16));
        corpo.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(2, 4, 14, 14));
        cards.setOpaque(false);
        cards.add(card("Materiais cadastrados", materiais, Estilo.VERDE));
        cards.add(card("Disponíveis agora", disponiveis, new Color(37, 99, 235)));
        cards.add(card("Materiais reutilizados", reutilizados, new Color(13, 148, 136)));
        cards.add(card("Reservas", reservas, new Color(217, 119, 6)));
        cards.add(card("Doações concluídas", doacoes, Estilo.VERDE_ESCURO));
        cards.add(card("Usuários", usuarios, new Color(124, 58, 237)));
        cards.add(card("CO₂ evitado", impacto, new Color(22, 163, 74)));
        cards.add(card("Resíduos evitados", residuos, new Color(100, 116, 139)));
        corpo.add(cards, BorderLayout.NORTH);

        JPanel blocoGrafico = new JPanel(new BorderLayout(0, 10));
        blocoGrafico.setBackground(Color.WHITE);
        blocoGrafico.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.BORDA), new EmptyBorder(16, 18, 16, 18)));
        JLabel tituloGrafico = new JLabel("Categorias mais utilizadas");
        tituloGrafico.setFont(tituloGrafico.getFont().deriveFont(Font.BOLD, 16f));
        tituloGrafico.setForeground(Estilo.TEXTO);
        perfis.setForeground(Estilo.TEXTO_SUAVE);
        blocoGrafico.add(tituloGrafico, BorderLayout.NORTH);
        blocoGrafico.add(grafico, BorderLayout.CENTER);
        blocoGrafico.add(perfis, BorderLayout.SOUTH);
        corpo.add(blocoGrafico, BorderLayout.CENTER);

        pagina.add(corpo, BorderLayout.CENTER);
        pagina.setBorder(new EmptyBorder(22, 24, 24, 24));
        add(pagina, BorderLayout.CENTER);
    }

    private static JLabel valor() {
        JLabel label = new JLabel("—");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 26f));
        label.setForeground(Estilo.TEXTO);
        return label;
    }

    /** Card de indicador com uma faixa colorida à esquerda. */
    private static JPanel card(String titulo, JComponent valor, Color destaque) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.BORDA),
                BorderFactory.createMatteBorder(0, 4, 0, 0, destaque)),
            new EmptyBorder(14, 16, 14, 16)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel rotulo = new JLabel(titulo);
        rotulo.setForeground(Estilo.TEXTO_SUAVE);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 12f));
        card.add(rotulo);
        card.add(Box.createVerticalStrut(8));
        card.add(valor);
        return card;
    }

    @Override public void atualizarDados() {
        Estilo.executar(this, () -> new RelatorioDAO().consultarDashboard(), this::preencher);
    }

    private void preencher(DashboardDTO d) {
        materiais.setText(NUMERO.format(d.getMateriaisCadastrados()));
        disponiveis.setText(NUMERO.format(d.getMateriaisDisponiveis()));
        reutilizados.setText(NUMERO.format(d.getMateriaisReutilizados()));
        reservas.setText(NUMERO.format(d.getReservas()));
        doacoes.setText(NUMERO.format(d.getDoacoesConcluidas()));
        usuarios.setText(NUMERO.format(d.getUsuarios()));
        impacto.setText(kg(d.getCo2EvitadoKg()));
        residuos.setText(kg(d.getResiduosEvitadosKg()));
        grafico.definir(d.getCategoriasMaisUtilizadas());
        perfis.setText("Perfis cadastrados — ONGs: " + d.getOngs() + "   ·   Construtoras: " + d.getConstrutoras()
            + "   ·   Famílias: " + d.getFamilias());
    }

    private static String kg(BigDecimal valor) {
        return DECIMAL.format(valor == null ? BigDecimal.ZERO : valor) + " kg";
    }

    /** Gráfico de barras horizontais simples, desenhado com Java 2D. */
    private static final class GraficoBarras extends JComponent {
        private final List<Map.Entry<String, Long>> dados = new ArrayList<>();

        GraficoBarras() { setPreferredSize(new Dimension(400, 220)); }

        void definir(Map<String, Long> valores) {
            dados.clear();
            if (valores != null) valores.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(8).forEach(dados::add);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont().deriveFont(13f));
            FontMetrics m = g2.getFontMetrics();
            if (dados.isEmpty()) {
                String vazio = "Ainda não há doações registradas para montar o gráfico.";
                g2.setColor(Estilo.TEXTO_SUAVE);
                g2.drawString(vazio, (getWidth() - m.stringWidth(vazio)) / 2, getHeight() / 2);
                g2.dispose();
                return;
            }
            int larguraRotulo = 0;
            for (Map.Entry<String, Long> e : dados) larguraRotulo = Math.max(larguraRotulo, m.stringWidth(e.getKey()));
            larguraRotulo = Math.min(larguraRotulo + 12, getWidth() / 3);
            long maximo = Math.max(1, dados.get(0).getValue());
            int linha = Math.max(22, Math.min(34, getHeight() / dados.size()));
            int areaBarra = getWidth() - larguraRotulo - 60;
            for (int i = 0; i < dados.size(); i++) {
                Map.Entry<String, Long> e = dados.get(i);
                int y = i * linha;
                int base = y + (linha + m.getAscent()) / 2 - 2;
                g2.setColor(Estilo.TEXTO);
                g2.drawString(e.getKey(), 0, base);
                int largura = (int) Math.max(4, areaBarra * (e.getValue() / (double) maximo));
                g2.setColor(i == 0 ? Estilo.VERDE : new Color(140, 200, 156));
                g2.fillRoundRect(larguraRotulo, y + 5, largura, linha - 10, 8, 8);
                g2.setColor(Estilo.TEXTO_SUAVE);
                g2.drawString(NUMERO.format(e.getValue()), larguraRotulo + largura + 8, base);
            }
            g2.dispose();
        }
    }
}
