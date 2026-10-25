package VIEW;

import DAO.DoacaoDAO;
import DTO.DoacaoDTO;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DoacoesVIEW extends JPanel implements Atualizavel {
    private final DefaultTableModel modelo = new DefaultTableModel(new String[] {"ID", "Material", "Doador", "Beneficiário", "Quantidade", "Peso (kg)", "Resíduos (kg)", "CO₂ (kg)", "Conclusão"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    public DoacoesVIEW() {
        setLayout(new BorderLayout(0, 12));
        add(Estilo.pagina("Doações", "Histórico de materiais que foram reaproveitados."), BorderLayout.NORTH);
        add(Estilo.tabela(new JTable(modelo)), BorderLayout.CENTER);
        JButton atualizar = new JButton("Atualizar"); atualizar.addActionListener(e -> atualizarDados());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT)); rodape.add(atualizar); add(rodape, BorderLayout.SOUTH);
    }
    @Override public void atualizarDados() {
        Estilo.executar(this, () -> new DoacaoDAO().listar(), lista -> {
            modelo.setRowCount(0);
            for (DoacaoDTO d : lista) modelo.addRow(new Object[] {d.getId(),
                d.getMaterial() == null ? "—" : d.getMaterial().getNome(),
                d.getDoador() == null ? "—" : d.getDoador().getNome(),
                d.getBeneficiario() == null ? "—" : d.getBeneficiario().getNome(), d.getQuantidade(),
                d.getPesoEstimadoKg(), d.getResiduosEvitadosKg(), d.getCo2EvitadoKg(), Estilo.texto(d.getConcluidaEm())});
        });
    }
}
