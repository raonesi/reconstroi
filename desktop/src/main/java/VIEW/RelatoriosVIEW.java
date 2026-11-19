package VIEW;

import DAO.DoacaoDAO;
import DAO.MaterialDAO;
import DAO.RelatorioDAO;
import DTO.DashboardDTO;
import DTO.UsuarioDTO;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class RelatoriosVIEW extends JPanel implements Atualizavel {
    private final UsuarioDTO usuario;
    private final JTextArea relatorio = new JTextArea();
    private static File ultimaPasta;

    public RelatoriosVIEW(UsuarioDTO usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(0, 12));
        setBackground(Estilo.FUNDO);
        add(Estilo.pagina("Relatórios", "Indicadores sociais, operacionais e ambientais do ReConstrói."), BorderLayout.NORTH);
        relatorio.setEditable(false);
        relatorio.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        relatorio.setBorder(new EmptyBorder(16, 16, 16, 16));
        JScrollPane scroll = new JScrollPane(relatorio);
        scroll.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 24, 0, 24), BorderFactory.createLineBorder(Estilo.BORDA)));
        scroll.setBackground(Estilo.FUNDO);
        add(scroll, BorderLayout.CENTER);

        JPanel rodape = Estilo.barraAcoes();
        JButton atualizar = Estilo.botaoSecundario("Atualizar");
        atualizar.addActionListener(e -> atualizarDados());
        JButton gerar = Estilo.botao("Gerar relatório…");
        gerar.addActionListener(e -> gerarRelatorio());
        rodape.add(atualizar);
        rodape.add(gerar);
        add(rodape, BorderLayout.SOUTH);
    }

    /** Compatibilidade com quem ainda cria a tela sem usuário. */
    public RelatoriosVIEW() { this(null); }

    @Override public void atualizarDados() {
        Estilo.executar(this, () -> new RelatorioDAO().consultarDashboard(), this::preencher);
    }

    private void preencher(DashboardDTO d) {
        StringBuilder t = new StringBuilder("RELATÓRIO GERAL — RECONSTRÓI\n========================================\n\n")
            .append("Materiais cadastrados........: ").append(d.getMateriaisCadastrados()).append('\n')
            .append("Materiais disponíveis........: ").append(d.getMateriaisDisponiveis()).append('\n')
            .append("Materiais reutilizados.......: ").append(d.getMateriaisReutilizados()).append('\n')
            .append("Reservas.....................: ").append(d.getReservas()).append('\n')
            .append("Doações concluídas...........: ").append(d.getDoacoesConcluidas()).append('\n')
            .append("Usuários.....................: ").append(d.getUsuarios()).append('\n')
            .append("ONGs........................: ").append(d.getOngs()).append('\n')
            .append("Construtoras.................: ").append(d.getConstrutoras()).append('\n')
            .append("Famílias.....................: ").append(d.getFamilias()).append('\n')
            .append("Quantidade reaproveitada.....: ").append(d.getQuantidadeReaproveitada()).append('\n')
            .append("Peso estimado................: ").append(d.getPesoEstimadoKg()).append(" kg\n")
            .append("Resíduos evitados............: ").append(d.getResiduosEvitadosKg()).append(" kg\n")
            .append("CO₂ evitado..................: ").append(d.getCo2EvitadoKg()).append(" kg\n\n")
            .append("CATEGORIAS MAIS UTILIZADAS\n----------------------------------------\n");
        for (Map.Entry<String, Long> e : d.getCategoriasMaisUtilizadas().entrySet()) t.append(String.format("%-30s %d%n", e.getKey(), e.getValue()));
        t.append("\nMATERIAIS MAIS DOADOS\n----------------------------------------\n");
        for (DashboardDTO.RankingDTO e : d.getMateriaisMaisDoados()) t.append(String.format("%-30s %d%n", e.getNome(), e.getQuantidade()));
        t.append("\nMATERIAIS MAIS PROCURADOS\n----------------------------------------\n");
        for (DashboardDTO.RankingDTO e : d.getMateriaisMaisProcurados()) t.append(String.format("%-30s %d%n", e.getNome(), e.getQuantidade()));
        relatorio.setText(t.toString());
        relatorio.setCaretPosition(0);
    }

    /** Pergunta formato e seções, escolhe onde salvar, gera o arquivo e oferece abri-lo. */
    private void gerarRelatorio() {
        JRadioButton pdf = new JRadioButton("PDF — para imprimir ou enviar", true);
        JRadioButton csv = new JRadioButton("Planilha CSV — para abrir no Excel");
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(pdf);
        grupo.add(csv);
        JCheckBox indicadores = new JCheckBox("Indicadores gerais e impacto ambiental", true);
        indicadores.setEnabled(false);
        JCheckBox materiais = new JCheckBox("Lista de materiais", true);
        JCheckBox doacoes = new JCheckBox("Lista de doações", true);

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.add(titulo("Formato"));
        painel.add(pdf);
        painel.add(csv);
        painel.add(Box.createVerticalStrut(12));
        painel.add(titulo("Conteúdo"));
        painel.add(indicadores);
        painel.add(materiais);
        painel.add(doacoes);
        if (JOptionPane.showConfirmDialog(this, painel, "Gerar relatório", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        boolean emPdf = pdf.isSelected();
        String extensao = emPdf ? "pdf" : "csv";
        JFileChooser seletor = new JFileChooser(ultimaPasta);
        seletor.setDialogTitle("Salvar relatório");
        seletor.setFileFilter(emPdf ? new FileNameExtensionFilter("Documento PDF (*.pdf)", "pdf")
            : new FileNameExtensionFilter("Planilha CSV (*.csv)", "csv"));
        seletor.setSelectedFile(new File(ultimaPasta, "relatorio-reconstroi-" + LocalDate.now() + "." + extensao));
        if (seletor.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File escolhido = seletor.getSelectedFile();
        if (!escolhido.getName().toLowerCase().endsWith("." + extensao)) escolhido = new File(escolhido.getPath() + "." + extensao);
        if (escolhido.exists() && JOptionPane.showConfirmDialog(this, "O arquivo “" + escolhido.getName() + "” já existe. Substituir?",
                "Arquivo existente", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        ultimaPasta = escolhido.getParentFile();

        final File arquivo = escolhido;
        final boolean comMateriais = materiais.isSelected();
        final boolean comDoacoes = doacoes.isSelected();
        String emitidoPor = usuario == null || usuario.getNome() == null ? "ReConstrói" : usuario.getNome();
        Estilo.executar(this, () -> {
            GeradorRelatorio.Dados dados = new GeradorRelatorio.Dados(
                new RelatorioDAO().consultarDashboard(),
                comMateriais ? new MaterialDAO().pesquisar("", "", "", "", "") : List.of(),
                comDoacoes ? new DoacaoDAO().listar() : List.of(),
                emitidoPor);
            if (emPdf) GeradorRelatorio.gerarPdf(arquivo, dados);
            else GeradorRelatorio.gerarCsv(arquivo, dados);
            return arquivo;
        }, gerado -> {
            int abrir = JOptionPane.showConfirmDialog(this, "Relatório salvo em:\n" + gerado.getAbsolutePath() + "\n\nDeseja abrir agora?",
                "Relatório gerado", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (abrir == JOptionPane.YES_OPTION) {
                try { Desktop.getDesktop().open(gerado); }
                catch (Exception erro) { Estilo.erro(this, new IllegalStateException("Não foi possível abrir o arquivo automaticamente.", erro)); }
            }
        });
    }

    private static JLabel titulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        return label;
    }
}
