package VIEW;

import DAO.NotificacaoDAO;
import DTO.NotificacaoDTO;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class NotificacoesVIEW extends JPanel implements Atualizavel {
    private final DefaultTableModel modelo = new DefaultTableModel(new String[] {"ID", "Lida", "Título", "Mensagem", "Data"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    private List<NotificacaoDTO> notificacoes = new ArrayList<>();
    public NotificacoesVIEW() {
        setLayout(new BorderLayout(0, 12));
        add(Estilo.pagina("Notificações", "Avisos sobre solicitações e alterações nos seus materiais."), BorderLayout.NORTH);
        add(Estilo.tabela(tabela), BorderLayout.CENTER);
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton atualizar = new JButton("Atualizar"); atualizar.addActionListener(e -> atualizarDados()); rodape.add(atualizar);
        JButton ler = Estilo.botao("Marcar como lida"); ler.addActionListener(e -> marcarLida()); rodape.add(ler);
        add(rodape, BorderLayout.SOUTH);
    }
    @Override public void atualizarDados() {
        Estilo.executar(this, () -> new NotificacaoDAO().listar(), lista -> {
            notificacoes = lista; modelo.setRowCount(0);
            for (NotificacaoDTO n : lista) modelo.addRow(new Object[] {n.getId(), n.isLida() ? "Sim" : "Não", n.getTitulo(), n.getMensagem(), n.getCriadaEm()});
        });
    }
    private void marcarLida() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { Estilo.erro(this, new IllegalArgumentException("Selecione uma notificação.")); return; }
        NotificacaoDTO n = notificacoes.get(tabela.convertRowIndexToModel(linha));
        Estilo.executar(this, () -> new NotificacaoDAO().marcarLida(n.getId()), v -> atualizarDados());
    }
}
