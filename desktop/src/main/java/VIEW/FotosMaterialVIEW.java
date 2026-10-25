package VIEW;

import DAO.ImagemArquivo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Selecao por explorador ou arrastar/soltar, sempre copiando os arquivos. */
public final class FotosMaterialVIEW extends JPanel {
    private static final int MAX_FOTOS = 5;
    private final DefaultListModel<File> modelo = new DefaultListModel<>();
    private final JList<File> lista = new JList<>(modelo);
    private final JLabel contador = new JLabel("0 de 5 fotos");
    private final Consumer<String> mostrarAviso;

    public FotosMaterialVIEW() {
        this(null);
    }

    FotosMaterialVIEW(Consumer<String> mostrarAviso) {
        super(new BorderLayout(6, 6));
        this.mostrarAviso = mostrarAviso != null ? mostrarAviso : mensagem ->
            JOptionPane.showMessageDialog(this, mensagem, "Selecionar fotos", JOptionPane.WARNING_MESSAGE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Estilo.VERDE_ESCURO), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel instrucao = new JLabel("<html><b>Arraste as fotos do Explorador para cá</b><br>JPG, PNG ou WebP · até 5 fotos · até 5 MB por foto</html>");
        add(instrucao, BorderLayout.NORTH);
        lista.setVisibleRowCount(4);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof File arquivo) {
                    // Nomes vindos do explorador sao texto, nao HTML Swing.
                    label.putClientProperty("html.disable", Boolean.TRUE);
                    label.setText(arquivo.getName());
                    label.setToolTipText(arquivo.getAbsolutePath());
                }
                return label;
            }
        });
        JScrollPane rolagem = new JScrollPane(lista);
        rolagem.setPreferredSize(new Dimension(400, 90));
        add(rolagem, BorderLayout.CENTER);
        JButton selecionar = new JButton("Selecionar fotos");
        selecionar.addActionListener(e -> selecionarFotos());
        JButton remover = new JButton("Remover selecionadas");
        remover.setEnabled(false);
        lista.addListSelectionListener(e -> remover.setEnabled(!lista.isSelectionEmpty()));
        remover.addActionListener(e -> {
            int[] indices = lista.getSelectedIndices();
            for (int i = indices.length - 1; i >= 0; i--) modelo.remove(indices[i]);
            atualizarContador();
        });
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botoes.add(selecionar); botoes.add(remover); botoes.add(contador);
        add(botoes, BorderLayout.SOUTH);

        TransferHandler transferencia = new TransferHandler() {
            @Override public boolean canImport(TransferSupport support) {
                if (!isEnabled() || !support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return false;
                if (support.isDrop()) {
                    if ((support.getSourceDropActions() & COPY) == 0) return false;
                    support.setDropAction(COPY);
                }
                return true;
            }
            @Override public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    Object dados = support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!(dados instanceof List<?> itens)) return false;
                    List<File> arquivos = new ArrayList<>();
                    for (Object item : itens) if (item instanceof File arquivo) arquivos.add(arquivo);
                    return adicionarArquivos(arquivos);
                } catch (Exception erro) {
                    FotosMaterialVIEW.this.mostrarAviso.accept("Não foi possível receber as fotos. Tente o botão Selecionar fotos.");
                    return false;
                }
            }
        };
        for (JComponent alvo : new JComponent[] {this, instrucao, lista, rolagem, rolagem.getViewport(), botoes, selecionar, remover, contador})
            alvo.setTransferHandler(transferencia);
    }

    private void selecionarFotos() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar fotos do material");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Fotos JPG, PNG e WebP", "jpg", "jpeg", "png", "webp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            adicionarArquivos(List.of(chooser.getSelectedFiles()));
    }

    boolean adicionarArquivos(List<File> arquivos) {
        List<String> avisos = new ArrayList<>();
        int adicionados = 0;
        for (File arquivo : arquivos) {
            try {
                File canonico = arquivo.getCanonicalFile();
                ImagemArquivo.validar(canonico);
                if (modelo.contains(canonico)) continue;
                if (modelo.size() >= MAX_FOTOS) {
                    avisos.add("Limite de 5 fotos atingido. Remova uma foto para adicionar outra.");
                    break;
                }
                modelo.addElement(canonico);
                adicionados++;
            } catch (IOException | IllegalArgumentException erro) {
                avisos.add(arquivo.getName() + ": " + (erro instanceof IOException ? "Não foi possível acessar o arquivo." : erro.getMessage()));
            }
        }
        atualizarContador();
        if (!avisos.isEmpty()) mostrarAviso.accept(String.join("\n", avisos));
        return adicionados > 0;
    }

    private void atualizarContador() { contador.setText(modelo.size() + " de 5 fotos"); }

    public List<File> getArquivos() {
        List<File> arquivos = new ArrayList<>();
        for (int i = 0; i < modelo.size(); i++) arquivos.add(modelo.get(i));
        return List.copyOf(arquivos);
    }
}
