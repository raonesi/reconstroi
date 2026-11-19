package VIEW;

import DAO.ReservaDAO;
import DTO.ReservaDTO;
import DTO.UsuarioDTO;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReservasVIEW extends JPanel implements Atualizavel {
    private final UsuarioDTO usuario;
    private final JComboBox<String> modo;
    private final DefaultTableModel modelo = new DefaultTableModel(new String[] {"ID", "Material", "Solicitante", "Quantidade", "Unidade", "Local", "Status", "Mensagem"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    private List<ReservaDTO> reservas = new ArrayList<>();

    public ReservasVIEW(UsuarioDTO usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(0, 12));
        JPanel topo = Estilo.pagina("Reservas", "Acompanhe o fluxo da solicitação até a conclusão da doação.");
        modo = usuario.possuiPermissao("RESERVA_GERENCIAR_RECEBIDA")
            ? new JComboBox<>(new String[] {"Solicitações recebidas", "Minhas solicitações"})
            : new JComboBox<>(new String[] {"Minhas solicitações"});
        modo.addActionListener(e -> atualizarDados());
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT)); filtro.setOpaque(false);
        filtro.add(new JLabel("Visualizar:")); filtro.add(modo);
        topo.add(filtro, BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);
        add(Estilo.tabela(tabela), BorderLayout.CENTER);
        setBackground(Estilo.FUNDO);

        JPanel botoes = Estilo.barraAcoes();
        JButton atualizar = Estilo.botaoSecundario("Atualizar"); atualizar.addActionListener(e -> atualizarDados()); botoes.add(atualizar);
        if (usuario.possuiPermissao("RESERVA_GERENCIAR_RECEBIDA")) {
            botao(botoes, "Aprovar", "aprovar"); botao(botoes, "Recusar", "recusar");
            botao(botoes, "Confirmar retirada", "confirmar-retirada"); botao(botoes, "Finalizar", "finalizar");
        }
        JButton cancelar = Estilo.botaoSecundario("Cancelar reserva"); cancelar.addActionListener(e -> acao("cancelar")); botoes.add(cancelar);
        add(botoes, BorderLayout.SOUTH);
    }

    private void botao(JPanel painel, String texto, String acao) {
        JButton botao = Estilo.botao(texto); botao.addActionListener(e -> acao(acao)); painel.add(botao);
    }

    @Override public void atualizarDados() {
        boolean recebidas = "Solicitações recebidas".equals(modo.getSelectedItem());
        Estilo.executar(this, () -> recebidas ? new ReservaDAO().listarRecebidas() : new ReservaDAO().listarMinhas(), this::preencher);
    }

    private void preencher(List<ReservaDTO> lista) {
        reservas = lista; modelo.setRowCount(0);
        for (ReservaDTO r : lista) modelo.addRow(new Object[] {r.getId(),
            r.getMaterial() == null ? "—" : r.getMaterial().getNome(),
            r.getSolicitante() == null ? "—" : r.getSolicitante().getNome(), r.getQuantidade(),
            r.getMaterial() == null ? "—" : r.getMaterial().getUnidade(),
            r.getMaterial() == null ? "—" : Estilo.texto(r.getMaterial().getCidade()) + "/" + Estilo.texto(r.getMaterial().getEstado()),
            r.getStatus(), Estilo.texto(r.getMensagem())});
    }

    private ReservaDTO selecionada() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { Estilo.erro(this, new IllegalArgumentException("Selecione uma reserva.")); return null; }
        return reservas.get(tabela.convertRowIndexToModel(linha));
    }

    private void acao(String acao) {
        ReservaDTO reserva = selecionada(); if (reserva == null) return;
        String alvo = "a reserva #" + reserva.getId()
            + (reserva.getMaterial() == null ? "" : " (" + reserva.getMaterial().getNome() + ")");
        String pergunta = switch (acao) {
            case "aprovar" -> "Aprovar " + alvo + "?";
            case "recusar" -> "Recusar " + alvo + "?";
            case "confirmar-retirada" -> "Confirmar que o material d" + alvo + " foi retirado?";
            case "finalizar" -> "Finalizar " + alvo + " e registrar a doação?";
            case "cancelar" -> "Cancelar " + alvo + "?";
            default -> "Executar “" + acao + "” n" + alvo + "?";
        };
        if (JOptionPane.showConfirmDialog(this, pergunta, "Confirmação", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Estilo.executar(this, () -> new ReservaDAO().executarAcao(reserva.getId(), acao), r -> {
            Estilo.sucesso(this, "Reserva atualizada: " + Estilo.rotuloStatus(r.getStatus()) + "."); atualizarDados();
        });
    }
}
