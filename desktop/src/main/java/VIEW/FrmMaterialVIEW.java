package VIEW;

import DAO.CategoriaDAO;
import DAO.MaterialDAO;
import DTO.CategoriaDTO;
import DTO.MaterialDTO;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FrmMaterialVIEW extends JDialog {
    private final JTextField nome = new JTextField();
    private final JTextArea descricao = new JTextArea(4, 30);
    private final JSpinner quantidade = new JSpinner(new SpinnerNumberModel(1, 1, 1_000_000, 1));
    private final JComboBox<String> unidade = new JComboBox<>(new String[] {"unidade", "kg", "m²", "litros", "metros", "sacos", "latas"});
    private final JComboBox<String> conservacao = new JComboBox<>(new String[] {"NOVO", "EXCELENTE", "BOM", "REGULAR"});
    private final JComboBox<CategoriaDTO> categoria = new JComboBox<>();
    private final JTextField endereco = new JTextField();
    private final JTextField cidade = new JTextField();
    private final JTextField estado = new JTextField();
    private final JCheckBox confirmarDuplicidade = new JCheckBox("Confirmo o cadastro mesmo se houver anúncio parecido");
    private final DefaultListModel<File> arquivosModel = new DefaultListModel<>();
    private final MaterialDTO material;
    private final Runnable aoSalvar;

    public FrmMaterialVIEW(Frame pai, MaterialDTO material, Runnable aoSalvar) {
        super(pai, material == null ? "Cadastrar material" : "Editar material", true);
        this.material = material;
        this.aoSalvar = aoSalvar;
        setSize(660, 760);
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel titulo = new JLabel(material == null ? "Novo material" : "Editar material #" + material.getId());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        raiz.add(titulo, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        int linha = 0;
        linha = campo(form, c, linha, "Nome", nome);
        descricao.setLineWrap(true); descricao.setWrapStyleWord(true);
        linha = campo(form, c, linha, "Descrição", new JScrollPane(descricao));
        JPanel qtd = new JPanel(new GridLayout(1, 2, 8, 0)); qtd.add(quantidade); qtd.add(unidade);
        linha = campo(form, c, linha, "Quantidade e unidade", qtd);
        linha = campo(form, c, linha, "Estado de conservação", conservacao);
        linha = campo(form, c, linha, "Categoria", categoria);
        linha = campo(form, c, linha, "Endereço de retirada", endereco);
        JPanel local = new JPanel(new GridLayout(1, 2, 8, 0)); local.add(cidade); local.add(estado);
        linha = campo(form, c, linha, "Cidade e UF", local);
        confirmarDuplicidade.setVisible(material == null);
        c.gridx = 0; c.gridy = linha++; c.gridwidth = 2; form.add(confirmarDuplicidade, c);

        if (material == null) {
            JList<File> lista = new JList<>(arquivosModel);
            JButton fotos = new JButton("Selecionar fotos");
            fotos.addActionListener(evento -> selecionarFotos());
            JPanel fotosPainel = new JPanel(new BorderLayout(4, 4));
            fotosPainel.add(fotos, BorderLayout.NORTH); fotosPainel.add(new JScrollPane(lista), BorderLayout.CENTER);
            c.gridx = 0; c.gridy = linha; c.gridwidth = 2; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
            form.add(fotosPainel, c);
        }
        raiz.add(new JScrollPane(form), BorderLayout.CENTER);

        JButton salvar = Estilo.botao("SALVAR");
        salvar.addActionListener(evento -> salvar());
        raiz.add(salvar, BorderLayout.SOUTH);
        setContentPane(raiz);

        Estilo.executar(this, () -> new CategoriaDAO().listar(), categorias -> {
            categorias.forEach(categoria::addItem);
            preencher();
        });
    }

    private static int campo(JPanel painel, GridBagConstraints c, int linha, String texto, JComponent componente) {
        c.weighty = 0; c.gridwidth = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = linha; c.weightx = 0; painel.add(new JLabel(texto), c);
        c.gridx = 1; c.weightx = 1; painel.add(componente, c);
        return linha + 1;
    }

    private void preencher() {
        if (material == null) return;
        nome.setText(material.getNome()); descricao.setText(material.getDescricao());
        quantidade.setValue(material.getQuantidade()); unidade.setSelectedItem(material.getUnidade());
        conservacao.setSelectedItem(material.getEstadoConservacao()); endereco.setText(material.getEnderecoRetirada());
        cidade.setText(material.getCidade()); estado.setText(material.getEstado());
        if (material.getCategoria() != null) {
            for (int i = 0; i < categoria.getItemCount(); i++) {
                if (categoria.getItemAt(i).getId().equals(material.getCategoria().getId())) categoria.setSelectedIndex(i);
            }
        }
    }

    private void selecionarFotos() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            for (File file : chooser.getSelectedFiles()) arquivosModel.addElement(file);
        }
    }

    private void salvar() {
        CategoriaDTO cat = (CategoriaDTO) categoria.getSelectedItem();
        if (cat == null) { Estilo.erro(this, new IllegalArgumentException("Selecione uma categoria.")); return; }
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("nome", nome.getText()); dados.put("descricao", descricao.getText());
        dados.put("quantidade", quantidade.getValue()); dados.put("unidade", unidade.getSelectedItem());
        dados.put("estadoConservacao", conservacao.getSelectedItem()); dados.put("categoriaId", cat.getId());
        dados.put("enderecoRetirada", endereco.getText()); dados.put("cidade", cidade.getText());
        dados.put("estado", estado.getText()); dados.put("confirmarDuplicidade", confirmarDuplicidade.isSelected());
        List<File> arquivos = new ArrayList<>();
        for (int i = 0; i < arquivosModel.size(); i++) arquivos.add(arquivosModel.get(i));
        Estilo.executar(this,
            () -> material == null ? new MaterialDAO().cadastrar(dados, arquivos) : new MaterialDAO().alterar(material.getId(), dados),
            salvo -> { Estilo.sucesso(this, "Material salvo com sucesso."); dispose(); aoSalvar.run(); });
    }
}
