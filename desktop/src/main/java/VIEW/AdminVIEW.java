package VIEW;

import DAO.AdminDAO;
import DTO.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminVIEW extends JPanel implements Atualizavel {
    private final JTabbedPane abas = new JTabbedPane();
    private final DefaultTableModel usuariosModel = model("ID", "Nome", "E-mail", "Tipo", "Local", "Ativo");
    private final JTable usuariosTabela = new JTable(usuariosModel);
    private List<UsuarioDTO> usuarios = new ArrayList<>();
    private final DefaultTableModel materiaisModel = model("ID", "Material", "Categoria", "Responsável", "Local", "Status");
    private final JTable materiaisTabela = new JTable(materiaisModel);
    private List<MaterialDTO> materiais = new ArrayList<>();
    private final DefaultTableModel denunciasModel = model("ID", "Material", "Denunciante", "Motivo", "Status", "Data");
    private final JTable denunciasTabela = new JTable(denunciasModel);
    private List<DenunciaDTO> denuncias = new ArrayList<>();
    private final DefaultTableModel reservasModel = model("ID", "Material", "Doador", "Solicitante", "Quantidade", "Status");
    private final DefaultTableModel doacoesModel = model("ID", "Material", "Doador", "Beneficiário", "Quantidade", "CO₂ evitado");

    public AdminVIEW() {
        setLayout(new BorderLayout(0, 12));
        add(Estilo.pagina("Administração", "Gerencie usuários, anúncios, denúncias, reservas e doações."), BorderLayout.NORTH);
        abas.addTab("Usuários", usuariosPainel());
        abas.addTab("Materiais", materiaisPainel());
        abas.addTab("Denúncias", denunciasPainel());
        abas.addTab("Reservas", leituraPainel(new JTable(reservasModel)));
        abas.addTab("Doações", leituraPainel(new JTable(doacoesModel)));
        abas.addChangeListener(e -> atualizarDados());
        add(abas, BorderLayout.CENTER);
    }

    private JPanel usuariosPainel() {
        JPanel p = new JPanel(new BorderLayout()); p.add(Estilo.tabela(usuariosTabela), BorderLayout.CENTER);
        JButton ativo = Estilo.botao("Ativar/Desativar usuário"); ativo.addActionListener(e -> alterarAtivo());
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT)); r.add(ativo); p.add(r, BorderLayout.SOUTH); return p;
    }
    private JPanel materiaisPainel() {
        JPanel p = new JPanel(new BorderLayout()); p.add(Estilo.tabela(materiaisTabela), BorderLayout.CENTER);
        JButton remover = Estilo.botao("Remover anúncio"); remover.addActionListener(e -> removerMaterial());
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT)); r.add(remover); p.add(r, BorderLayout.SOUTH); return p;
    }
    private JPanel denunciasPainel() {
        JPanel p = new JPanel(new BorderLayout()); p.add(Estilo.tabela(denunciasTabela), BorderLayout.CENTER);
        JComboBox<String> status = new JComboBox<>(new String[] {"ABERTA", "EM_ANALISE", "RESOLVIDA", "ARQUIVADA"});
        JButton alterar = Estilo.botao("Alterar status"); alterar.addActionListener(e -> alterarDenuncia((String) status.getSelectedItem()));
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT)); r.add(status); r.add(alterar); p.add(r, BorderLayout.SOUTH); return p;
    }
    private JPanel leituraPainel(JTable tabela) {
        JPanel p = new JPanel(new BorderLayout()); p.add(Estilo.tabela(tabela), BorderLayout.CENTER);
        JButton atualizar = new JButton("Atualizar"); atualizar.addActionListener(e -> atualizarDados());
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT)); r.add(atualizar); p.add(r, BorderLayout.SOUTH); return p;
    }
    private static DefaultTableModel model(String... colunas) {
        return new DefaultTableModel(colunas, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    }

    @Override public void atualizarDados() {
        switch (abas.getSelectedIndex()) {
            case 0 -> Estilo.executar(this, () -> new AdminDAO().listarUsuarios(), this::preencherUsuarios);
            case 1 -> Estilo.executar(this, () -> new AdminDAO().listarMateriais(), this::preencherMateriais);
            case 2 -> Estilo.executar(this, () -> new AdminDAO().listarDenuncias(), this::preencherDenuncias);
            case 3 -> Estilo.executar(this, () -> new AdminDAO().listarReservas(), this::preencherReservas);
            case 4 -> Estilo.executar(this, () -> new AdminDAO().listarDoacoes(), this::preencherDoacoes);
            default -> { }
        }
    }

    private void preencherUsuarios(List<UsuarioDTO> lista) {
        usuarios = lista; usuariosModel.setRowCount(0);
        for (UsuarioDTO u : lista) usuariosModel.addRow(new Object[] {u.getId(), u.getNome(), u.getEmail(), u.getTipo(),
            Estilo.texto(u.getCidade()) + "/" + Estilo.texto(u.getEstado()), u.isAtivo() ? "Sim" : "Não"});
    }
    private void preencherMateriais(List<MaterialDTO> lista) {
        materiais = lista; materiaisModel.setRowCount(0);
        for (MaterialDTO m : lista) materiaisModel.addRow(new Object[] {m.getId(), m.getNome(),
            m.getCategoria() == null ? "—" : m.getCategoria().getNome(), m.getProprietario() == null ? "—" : m.getProprietario().getNome(),
            Estilo.texto(m.getCidade()) + "/" + Estilo.texto(m.getEstado()), m.getStatus()});
    }
    private void preencherDenuncias(List<DenunciaDTO> lista) {
        denuncias = lista; denunciasModel.setRowCount(0);
        for (DenunciaDTO d : lista) denunciasModel.addRow(new Object[] {d.getId(), Estilo.texto(d.getMaterialNome()),
            d.getDenunciante() == null ? "—" : d.getDenunciante().getNome(), d.getMotivo(), d.getStatus(), d.getCriadoEm()});
    }
    private void preencherReservas(List<ReservaDTO> lista) {
        reservasModel.setRowCount(0);
        for (ReservaDTO r : lista) reservasModel.addRow(new Object[] {r.getId(), r.getMaterial() == null ? "—" : r.getMaterial().getNome(),
            r.getMaterial() == null || r.getMaterial().getProprietario() == null ? "—" : r.getMaterial().getProprietario().getNome(),
            r.getSolicitante() == null ? "—" : r.getSolicitante().getNome(), r.getQuantidade(), r.getStatus()});
    }
    private void preencherDoacoes(List<DoacaoDTO> lista) {
        doacoesModel.setRowCount(0);
        for (DoacaoDTO d : lista) doacoesModel.addRow(new Object[] {d.getId(), d.getMaterial() == null ? "—" : d.getMaterial().getNome(),
            d.getDoador() == null ? "—" : d.getDoador().getNome(), d.getBeneficiario() == null ? "—" : d.getBeneficiario().getNome(),
            d.getQuantidade(), d.getCo2EvitadoKg()});
    }

    private int selecionada(JTable tabela, String tipo) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) Estilo.erro(this, new IllegalArgumentException("Selecione " + tipo + "."));
        return linha;
    }
    private void alterarAtivo() {
        int linha = selecionada(usuariosTabela, "um usuário"); if (linha < 0) return;
        UsuarioDTO u = usuarios.get(usuariosTabela.convertRowIndexToModel(linha));
        Estilo.executar(this, () -> new AdminDAO().alterarAtivo(u.getId(), !u.isAtivo()), v -> atualizarDados());
    }
    private void removerMaterial() {
        int linha = selecionada(materiaisTabela, "um material"); if (linha < 0) return;
        MaterialDTO m = materiais.get(materiaisTabela.convertRowIndexToModel(linha));
        String motivo = JOptionPane.showInputDialog(this, "Motivo da remoção:", "Anúncio irregular");
        if (motivo == null) return;
        Estilo.executar(this, () -> { new AdminDAO().removerMaterial(m.getId(), motivo); return null; }, v -> atualizarDados());
    }
    private void alterarDenuncia(String status) {
        int linha = selecionada(denunciasTabela, "uma denúncia"); if (linha < 0) return;
        DenunciaDTO d = denuncias.get(denunciasTabela.convertRowIndexToModel(linha));
        Estilo.executar(this, () -> new AdminDAO().atualizarDenuncia(d.getId(), status), v -> atualizarDados());
    }
}
