package VIEW;

import DAO.CategoriaDAO;
import DAO.MaterialDAO;
import DAO.ReservaDAO;
import DTO.CategoriaDTO;
import DTO.MaterialDTO;
import DTO.UsuarioDTO;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MateriaisVIEW extends JPanel implements Atualizavel {
    private final UsuarioDTO usuario;
    private final JTextField busca = new JTextField(16);
    private final JTextField cidade = new JTextField(10);
    private final JComboBox<String> estado = new JComboBox<>(new String[] {"", "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"});
    private final JComboBox<Object> categoria = new JComboBox<>(new Object[] {"Todas as categorias"});
    private final JComboBox<String> conservacao = new JComboBox<>(new String[] {"", "NOVO", "EXCELENTE", "BOM", "REGULAR"});
    private final JCheckBox somenteMeus = new JCheckBox("Meus materiais");
    private final DefaultTableModel modelo = new DefaultTableModel(new String[] {"ID", "Material", "Categoria", "Quantidade", "Unidade", "Cidade/UF", "Status", "Responsável"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabela = new JTable(modelo);
    private List<MaterialDTO> materiais = new ArrayList<>();

    public MateriaisVIEW(UsuarioDTO usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(0, 12));
        JPanel pagina = Estilo.pagina("Materiais", "Pesquise, cadastre, edite ou solicite materiais para reaproveitamento.");
        pagina.add(filtros(), BorderLayout.CENTER);
        add(pagina, BorderLayout.NORTH);
        add(Estilo.tabela(tabela), BorderLayout.CENTER);
        add(acoes(), BorderLayout.SOUTH);
        somenteMeus.setVisible(usuario.isDoador());
        somenteMeus.addActionListener(evento -> atualizarDados());
        Estilo.executar(this, () -> new CategoriaDAO().listar(), lista -> lista.forEach(categoria::addItem));
    }

    private JPanel filtros() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        painel.setOpaque(false);
        painel.add(new JLabel("Busca")); painel.add(busca);
        painel.add(new JLabel("Categoria")); painel.add(categoria);
        painel.add(new JLabel("Cidade")); painel.add(cidade);
        painel.add(new JLabel("UF")); painel.add(estado);
        painel.add(new JLabel("Conservação")); painel.add(conservacao);
        painel.add(somenteMeus);
        JButton pesquisar = Estilo.botao("Pesquisar");
        pesquisar.addActionListener(evento -> atualizarDados());
        painel.add(pesquisar);
        return painel;
    }

    private JPanel acoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton atualizar = new JButton("Atualizar"); atualizar.addActionListener(e -> atualizarDados()); painel.add(atualizar);
        JButton detalhes = new JButton("Detalhes"); detalhes.addActionListener(e -> detalhes()); painel.add(detalhes);
        JButton denunciar = new JButton("Denunciar"); denunciar.addActionListener(e -> denunciar()); painel.add(denunciar);
        if (usuario.possuiPermissao("RESERVA_SOLICITAR")) {
            JButton reservar = Estilo.botao("Solicitar reserva"); reservar.addActionListener(e -> reservar()); painel.add(reservar);
        }
        if (usuario.possuiPermissao("MATERIAL_CADASTRAR")) {
            JButton novo = Estilo.botao("Cadastrar material"); novo.addActionListener(e -> novo()); painel.add(novo);
            JButton editar = new JButton("Editar"); editar.addActionListener(e -> editar()); painel.add(editar);
            JButton excluir = new JButton("Excluir"); excluir.addActionListener(e -> excluir()); painel.add(excluir);
        }
        return painel;
    }

    @Override public void atualizarDados() {
        Object cat = categoria.getSelectedItem();
        String categoriaId = cat instanceof CategoriaDTO dto ? String.valueOf(dto.getId()) : "";
        Estilo.executar(this,
            () -> somenteMeus.isSelected() ? new MaterialDAO().listarMeus()
                : new MaterialDAO().pesquisar(busca.getText(), categoriaId, cidade.getText(),
                    String.valueOf(estado.getSelectedItem()), String.valueOf(conservacao.getSelectedItem())),
            this::preencher);
    }

    private void preencher(List<MaterialDTO> lista) {
        materiais = lista;
        modelo.setRowCount(0);
        for (MaterialDTO m : lista) modelo.addRow(new Object[] {m.getId(), m.getNome(),
            m.getCategoria() == null ? "—" : m.getCategoria().getNome(), m.getQuantidade(), m.getUnidade(),
            Estilo.texto(m.getCidade()) + "/" + Estilo.texto(m.getEstado()), m.getStatus(),
            m.getProprietario() == null ? "—" : m.getProprietario().getNome()});
    }

    private MaterialDTO selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { Estilo.erro(this, new IllegalArgumentException("Selecione um material.")); return null; }
        return materiais.get(tabela.convertRowIndexToModel(linha));
    }

    private void detalhes() {
        MaterialDTO m = selecionado(); if (m == null) return;
        Estilo.executar(this, () -> new MaterialDAO().consultar(m.getId()), completo -> {
            JTextArea texto = new JTextArea(14, 48);
            texto.setEditable(false); texto.setLineWrap(true); texto.setWrapStyleWord(true);
            texto.setText("Material: " + completo.getNome() + "\nCategoria: " + (completo.getCategoria() == null ? "—" : completo.getCategoria().getNome())
                + "\nQuantidade: " + completo.getQuantidade() + " " + completo.getUnidade()
                + "\nConservação: " + completo.getEstadoConservacao() + "\nStatus: " + completo.getStatus()
                + "\nLocal: " + completo.getEnderecoRetirada() + " — " + completo.getCidade() + "/" + completo.getEstado()
                + "\nResponsável: " + (completo.getProprietario() == null ? "—" : completo.getProprietario().getNome())
                + "\nFotos: " + completo.getFotos().size() + "\n\n" + completo.getDescricao());
            JOptionPane.showMessageDialog(this, new JScrollPane(texto), "Detalhes do material", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void reservar() {
        MaterialDTO m = selecionado(); if (m == null) return;
        JSpinner qtd = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, m.getQuantidade()), 1));
        JTextArea msg = new JTextArea(4, 28);
        JPanel p = new JPanel(new BorderLayout(5, 5)); p.add(qtd, BorderLayout.NORTH); p.add(new JScrollPane(msg), BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, p, "Quantidade e mensagem", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        Estilo.executar(this, () -> new ReservaDAO().solicitar(m.getId(), (Integer) qtd.getValue(), msg.getText()),
            r -> { Estilo.sucesso(this, "Reserva solicitada. O dono do material foi notificado."); atualizarDados(); });
    }

    private void novo() { new FrmMaterialVIEW((Frame) SwingUtilities.getWindowAncestor(this), null, this::atualizarDados).setVisible(true); }
    private void editar() {
        MaterialDTO m = selecionado(); if (m == null) return;
        Estilo.executar(this, () -> new MaterialDAO().consultar(m.getId()), completo ->
            new FrmMaterialVIEW((Frame) SwingUtilities.getWindowAncestor(this), completo, this::atualizarDados).setVisible(true));
    }
    private void excluir() {
        MaterialDTO m = selecionado(); if (m == null) return;
        if (JOptionPane.showConfirmDialog(this, "Excluir “" + m.getNome() + "”?", "Confirmação", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Estilo.executar(this, () -> { new MaterialDAO().excluir(m.getId()); return null; }, v -> atualizarDados());
    }
    private void denunciar() {
        MaterialDTO m = selecionado(); if (m == null) return;
        String motivo = JOptionPane.showInputDialog(this, "Informe o motivo da denúncia:");
        if (motivo == null || motivo.isBlank()) return;
        Estilo.executar(this, () -> { new MaterialDAO().denunciar(m.getId(), motivo); return null; }, v -> Estilo.sucesso(this, "Denúncia registrada."));
    }
}
