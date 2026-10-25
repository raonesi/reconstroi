package VIEW;

import DAO.RelatorioDAO;
import DTO.DashboardDTO;
import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RelatoriosVIEW extends JPanel implements Atualizavel {
    private final JTextArea relatorio = new JTextArea();
    public RelatoriosVIEW() {
        setLayout(new BorderLayout(0, 12));
        add(Estilo.pagina("Relatórios", "Indicadores sociais, operacionais e ambientais do ReConstrói."), BorderLayout.NORTH);
        relatorio.setEditable(false); relatorio.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        relatorio.setBorder(new EmptyBorder(16, 16, 16, 16));
        add(new JScrollPane(relatorio), BorderLayout.CENTER);
        JButton atualizar = new JButton("Atualizar relatório"); atualizar.addActionListener(e -> atualizarDados());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT)); rodape.add(atualizar); add(rodape, BorderLayout.SOUTH);
    }
    @Override public void atualizarDados() { Estilo.executar(this, () -> new RelatorioDAO().consultarDashboard(), this::preencher); }
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
        relatorio.setText(t.toString()); relatorio.setCaretPosition(0);
    }
}
