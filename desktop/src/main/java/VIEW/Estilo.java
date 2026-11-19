package VIEW;

import DAO.ApiException;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/** Identidade visual e utilitários compartilhados por todas as telas (camada VIEW). */
public final class Estilo {
    public static final Color VERDE = new Color(22, 128, 58);
    public static final Color VERDE_ESCURO = new Color(13, 95, 42);
    public static final Color VERDE_CLARO = new Color(234, 245, 237);
    public static final Color FUNDO = new Color(245, 247, 245);
    public static final Color TEXTO = new Color(19, 36, 25);
    public static final Color TEXTO_SUAVE = new Color(94, 112, 99);
    public static final Color BORDA = new Color(223, 231, 225);
    public static final Color ERRO = new Color(185, 28, 28);
    private static List<Image> icones;

    private Estilo() {}

    /** Instala o tema FlatLaf com as cores da marca; se falhar, usa o Nimbus como antes. */
    public static void instalar() {
        try {
            FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#16803a"));
            FlatLightLaf.setup();
            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Component.focusWidth", 2);
            UIManager.put("Button.minimumWidth", 90);
            UIManager.put("Button.margin", new Insets(7, 14, 7, 14));
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.width", 11);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Table.gridColor", new Color(236, 241, 237));
            UIManager.put("Table.selectionBackground", new Color(214, 236, 220));
            UIManager.put("Table.selectionForeground", TEXTO);
            UIManager.put("TableHeader.height", 36);
            UIManager.put("TableHeader.background", new Color(248, 250, 248));
            UIManager.put("TabbedPane.tabHeight", 42);
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
            UIManager.put("TabbedPane.underlineColor", VERDE);
            UIManager.put("TabbedPane.tabInsets", new Insets(4, 18, 4, 18));
            UIManager.put("OptionPane.buttonAreaBorder", new EmptyBorder(12, 0, 0, 0));
        } catch (Exception | LinkageError falha) {
            instalarNimbus();
        }
    }

    private static void instalarNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { }
        UIManager.put("control", Color.WHITE);
        UIManager.put("nimbusBase", VERDE_ESCURO);
        UIManager.put("nimbusFocus", VERDE);
    }

    /** Ícone do aplicativo desenhado em código (quadrado verde com a inicial), em vários tamanhos. */
    public static List<Image> iconesAplicativo() {
        if (icones != null) return icones;
        icones = new ArrayList<>();
        for (int tamanho : new int[] {16, 24, 32, 48, 64, 128}) {
            BufferedImage imagem = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = imagem.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(VERDE);
            int arco = Math.max(4, tamanho / 3);
            g.fillRoundRect(0, 0, tamanho, tamanho, arco, arco);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, Math.round(tamanho * 0.62f)));
            FontMetrics m = g.getFontMetrics();
            String letra = "R";
            g.drawString(letra, (tamanho - m.stringWidth(letra)) / 2, (tamanho - m.getHeight()) / 2 + m.getAscent());
            g.dispose();
            icones.add(imagem);
        }
        return icones;
    }

    /** Botão principal (ação mais importante da tela). */
    public static JButton botao(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(VERDE);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setFont(botao.getFont().deriveFont(Font.BOLD));
        botao.putClientProperty("JButton.buttonType", "roundRect");
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    /** Botão secundário (ações de apoio). */
    public static JButton botaoSecundario(String texto) {
        JButton botao = new JButton(texto);
        botao.setFocusPainted(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    /** Botão com aparência de link. */
    public static JButton link(String texto) {
        JButton botao = new JButton(texto);
        botao.putClientProperty("JButton.buttonType", "borderless");
        botao.setForeground(VERDE);
        botao.setFont(botao.getFont().deriveFont(Font.BOLD));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    public static void placeholder(JTextField campo, String texto) {
        campo.putClientProperty("JTextField.placeholderText", texto);
    }

    public static JPanel pagina(String titulo, String subtitulo) {
        JPanel painel = new JPanel(new BorderLayout(0, 14));
        painel.setBackground(FUNDO);
        painel.setBorder(new EmptyBorder(22, 24, 8, 24));
        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setFont(labelTitulo.getFont().deriveFont(Font.BOLD, 26f));
        labelTitulo.setForeground(TEXTO);
        JLabel labelSubtitulo = new JLabel(subtitulo);
        labelSubtitulo.setForeground(TEXTO_SUAVE);
        labelSubtitulo.setFont(labelSubtitulo.getFont().deriveFont(14f));
        cabecalho.add(labelTitulo);
        cabecalho.add(Box.createVerticalStrut(4));
        cabecalho.add(labelSubtitulo);
        painel.add(cabecalho, BorderLayout.NORTH);
        return painel;
    }

    public static JScrollPane tabela(JTable tabela) {
        return tabela(tabela, null);
    }

    /** Configura a tabela; se informado, executa {@code aoDuploClique} ao clicar duas vezes numa linha. */
    public static JScrollPane tabela(JTable tabela, Runnable aoDuploClique) {
        tabela.setRowHeight(34);
        // Nomes vêm de outros usuários: um nome começando com "<html>" seria interpretado pelo Swing
        // (podia carregar imagem de site externo e revelar o IP de quem abriu a lista). Mostra sempre como texto.
        tabela.setDefaultRenderer(Object.class, new TextoSeguroRenderer());
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setFont(tabela.getTableHeader().getFont().deriveFont(Font.BOLD));
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            String nome = tabela.getColumnName(i);
            if ("Status".equalsIgnoreCase(nome)) tabela.getColumnModel().getColumn(i).setCellRenderer(new StatusRenderer());
            if ("ID".equalsIgnoreCase(nome)) {
                tabela.getColumnModel().getColumn(i).setMaxWidth(70);
                tabela.getColumnModel().getColumn(i).setPreferredWidth(56);
            }
        }
        if (aoDuploClique != null) {
            tabela.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent evento) {
                    if (evento.getClickCount() == 2 && tabela.rowAtPoint(evento.getPoint()) >= 0) aoDuploClique.run();
                }
            });
        }
        JScrollPane scroll = new JScrollPane(tabela);
        // Margem lateral (cor de fundo da página) + linha de borda ao redor da tabela.
        scroll.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 24, 0, 24),
            BorderFactory.createLineBorder(BORDA)));
        scroll.setBackground(FUNDO);
        scroll.setOpaque(true);
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    /** Célula de tabela que nunca interpreta HTML. */
    static final class TextoSeguroRenderer extends DefaultTableCellRenderer {
        TextoSeguroRenderer() { putClientProperty("html.disable", Boolean.TRUE); }
    }

    /** Barra de botões alinhada à direita, com o espaçamento padrão das telas. */
    public static JPanel barraAcoes() {
        JPanel painel = new JPanel(new FluxoQuebravel(FlowLayout.RIGHT, 8, 6));
        painel.setBackground(FUNDO);
        painel.setBorder(new EmptyBorder(6, 18, 12, 18));
        return painel;
    }

    /**
     * Define o tamanho de uma janela sem passar da área útil da tela (descontando a barra de tarefas).
     * Em notebooks com escala de 125% ou 150% a área útil é bem menor que a resolução.
     */
    public static void tamanhoNaTela(Window janela, int largura, int altura) {
        if (janela instanceof RootPaneContainer rpc)
            rpc.getRootPane().putClientProperty("reconstroi.tamanhoDesejado", new Dimension(largura, altura));
        tamanhoNaTela(janela, largura, altura, areaUtil(janela));
    }

    /** Versão que recebe a área útil: usada também pelo teste de responsividade para simular telas menores. */
    public static void tamanhoNaTela(Window janela, int largura, int altura, Rectangle area) {
        int margem = 16;
        janela.setSize(Math.min(largura, area.width - margem), Math.min(altura, area.height - margem));
    }

    public static Rectangle areaUtil(Component referencia) {
        GraphicsConfiguration gc = referencia != null && referencia.getGraphicsConfiguration() != null
            ? referencia.getGraphicsConfiguration()
            : GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle tela = gc.getBounds();
        Insets barra = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        return new Rectangle(tela.x + barra.left, tela.y + barra.top,
            tela.width - barra.left - barra.right, tela.height - barra.top - barra.bottom);
    }

    /** Converte códigos como "DOADOR_PESSOA_FISICA" ou "DISPONIVEL" em texto legível. */
    public static String rotuloStatus(Object valor) {
        if (valor == null) return "—";
        String bruto = String.valueOf(valor).trim();
        if (bruto.isEmpty()) return "—";
        String chave = chave(bruto);
        return switch (chave) {
            case "DISPONIVEL" -> "Disponível";
            case "RESERVADO" -> "Reservado";
            case "SOLICITADA" -> "Solicitada";
            case "APROVADA" -> "Aprovada";
            case "RECUSADA" -> "Recusada";
            case "RETIRADA" -> "Retirada";
            case "FINALIZADA" -> "Finalizada";
            case "CANCELADA" -> "Cancelada";
            case "REMOVIDO", "REMOVIDA" -> "Removido";
            case "DOADO" -> "Doado";
            default -> {
                String texto = bruto.replace('_', ' ').toLowerCase(Locale.ROOT);
                yield Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
            }
        };
    }

    private static String chave(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
    }

    /** Cores (fundo, texto) da etiqueta de cada status. */
    static Color[] coresStatus(Object valor) {
        String c = valor == null ? "" : chave(String.valueOf(valor));
        if (c.contains("DISPON") || c.contains("APROVAD") || c.contains("FINALIZ") || c.contains("DOAD"))
            return new Color[] {new Color(220, 243, 227), new Color(17, 101, 45)};
        if (c.contains("SOLICIT") || c.contains("RESERV") || c.contains("PENDENT"))
            return new Color[] {new Color(255, 239, 213), new Color(146, 72, 6)};
        if (c.contains("RETIR"))
            return new Color[] {new Color(219, 234, 254), new Color(29, 78, 216)};
        if (c.contains("RECUS") || c.contains("CANCEL") || c.contains("REMOV") || c.contains("BLOQ"))
            return new Color[] {new Color(254, 226, 226), new Color(153, 27, 27)};
        return new Color[] {new Color(238, 241, 239), new Color(70, 84, 74)};
    }

    /** Desenha o status como uma etiqueta arredondada colorida. */
    static final class StatusRenderer extends DefaultTableCellRenderer {
        private Color fundoEtiqueta = Color.LIGHT_GRAY;
        private boolean selecionada;

        StatusRenderer() { setOpaque(false); }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, rotuloStatus(valor), selecionada, false, linha, coluna);
            Color[] cores = coresStatus(valor);
            this.fundoEtiqueta = cores[0];
            this.selecionada = selecionada;
            setForeground(cores[1]);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
            setBorder(new EmptyBorder(0, 18, 0, 8));
            setOpaque(false);
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(selecionada ? UIManager.getColor("Table.selectionBackground") : Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int largura = getFontMetrics(getFont()).stringWidth(getText()) + 20;
            int altura = 22;
            g2.setColor(fundoEtiqueta);
            g2.fillRoundRect(8, (getHeight() - altura) / 2, largura, altura, altura, altura);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void erro(Component pai, Throwable erro) {
        JOptionPane.showMessageDialog(pai, mensagemErro(erro), "ReConstrói", JOptionPane.ERROR_MESSAGE);
    }

    /** Extrai a mensagem mais útil de uma exceção (prioriza a mensagem vinda da API). */
    public static String mensagemErro(Throwable erro) {
        Throwable causa = erro;
        while (causa.getCause() != null && !(causa instanceof ApiException)) causa = causa.getCause();
        String mensagem = causa.getMessage();
        if (causa instanceof java.net.ConnectException || (mensagem != null && mensagem.contains("Connection refused")))
            return "Não foi possível conectar ao servidor do ReConstrói. Verifique se o sistema está em execução.";
        if (mensagem == null || mensagem.isBlank()) mensagem = "Não foi possível concluir a operação.";
        return mensagem;
    }

    public static void sucesso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "ReConstrói", JOptionPane.INFORMATION_MESSAGE);
    }

    public static <T> void executar(Component componente, Callable<T> tarefa, Consumer<T> sucesso) {
        Window janela = SwingUtilities.getWindowAncestor(componente);
        if (janela != null) janela.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return tarefa.call(); }
            @Override protected void done() {
                if (janela != null) janela.setCursor(Cursor.getDefaultCursor());
                try { sucesso.accept(get()); }
                catch (Exception erro) { Estilo.erro(componente, erro); }
            }
        }.execute();
    }

    public static String texto(Object valor) { return valor == null ? "—" : String.valueOf(valor); }
}
