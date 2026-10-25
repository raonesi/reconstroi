package VIEW;

import DAO.ApiException;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class Estilo {
    public static final Color VERDE = new Color(22, 163, 74);
    public static final Color VERDE_ESCURO = new Color(21, 128, 61);
    public static final Color FUNDO = new Color(244, 247, 245);
    public static final Color TEXTO = new Color(25, 45, 32);
    private Estilo() {}

    public static void instalar() {
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
        UIManager.put("Table.alternateRowColor", new Color(242, 248, 243));
    }

    public static JButton botao(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(VERDE);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setFont(botao.getFont().deriveFont(Font.BOLD));
        return botao;
    }

    public static JPanel pagina(String titulo, String subtitulo) {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(FUNDO);
        painel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setFont(labelTitulo.getFont().deriveFont(Font.BOLD, 24f));
        labelTitulo.setForeground(TEXTO);
        JLabel labelSubtitulo = new JLabel(subtitulo);
        labelSubtitulo.setForeground(new Color(90, 110, 96));
        cabecalho.add(labelTitulo);
        cabecalho.add(Box.createVerticalStrut(4));
        cabecalho.add(labelSubtitulo);
        painel.add(cabecalho, BorderLayout.NORTH);
        return painel;
    }

    public static JScrollPane tabela(JTable tabela) {
        tabela.setRowHeight(28);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 222)));
        return scroll;
    }

    public static void erro(Component pai, Throwable erro) {
        Throwable causa = erro;
        while (causa.getCause() != null && !(causa instanceof ApiException)) causa = causa.getCause();
        String mensagem = causa.getMessage();
        if (mensagem == null || mensagem.isBlank()) mensagem = "Não foi possível concluir a operação.";
        JOptionPane.showMessageDialog(pai, mensagem, "ReConstrói", JOptionPane.ERROR_MESSAGE);
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
