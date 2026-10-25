package VIEW;

import DAO.CategoriaDAO;
import DAO.MaterialDAO;
import DTO.CategoriaDTO;
import DTO.MaterialDTO;
import java.awt.*;
import java.io.File;
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
    private final FotosMaterialVIEW fotos = new FotosMaterialVIEW();
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

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints posicao = new GridBagConstraints();
        posicao.insets = new Insets(4, 4, 4, 4);
        posicao.fill = GridBagConstraints.HORIZONTAL;
        posicao.weightx = 1;
        int linha = 0;
        linha = campo(formulario, posicao, linha, "Nome", nome);
        descricao.setLineWrap(true);
        descricao.setWrapStyleWord(true);
        linha = campo(formulario, posicao, linha, "Descrição", new JScrollPane(descricao));
        JPanel painelQuantidade = new JPanel(new GridLayout(1, 2, 8, 0));
        painelQuantidade.add(quantidade);
        painelQuantidade.add(unidade);
        linha = campo(formulario, posicao, linha, "Quantidade e unidade", painelQuantidade);
        linha = campo(formulario, posicao, linha, "Estado de conservação", conservacao);
        linha = campo(formulario, posicao, linha, "Categoria", categoria);
        linha = campo(formulario, posicao, linha, "Endereço de retirada", endereco);
        JPanel local = new JPanel(new GridLayout(1, 2, 8, 0));
        local.add(cidade);
        local.add(estado);
        linha = campo(formulario, posicao, linha, "Cidade e UF", local);
        confirmarDuplicidade.setVisible(material == null);
        posicao.gridx = 0;
        posicao.gridy = linha++;
        posicao.gridwidth = 2;
        formulario.add(confirmarDuplicidade, posicao);

        if (material == null) {
            posicao.gridx = 0;
            posicao.gridy = linha;
            posicao.gridwidth = 2;
            posicao.weighty = 1;
            posicao.fill = GridBagConstraints.BOTH;
            formulario.add(fotos, posicao);
        }
        raiz.add(new JScrollPane(formulario), BorderLayout.CENTER);

        JButton salvar = Estilo.botao("SALVAR");
        salvar.addActionListener(evento -> salvar());
        raiz.add(salvar, BorderLayout.SOUTH);
        setContentPane(raiz);

        Estilo.executar(this, () -> new CategoriaDAO().listar(), categorias -> {
            for (CategoriaDTO item : categorias) {
                categoria.addItem(item);
            }
            preencher();
        });
    }

    private static int campo(JPanel painel, GridBagConstraints posicao, int linha, String texto, JComponent componente) {
        posicao.weighty = 0;
        posicao.gridwidth = 1;
        posicao.fill = GridBagConstraints.HORIZONTAL;
        posicao.gridx = 0;
        posicao.gridy = linha;
        posicao.weightx = 0;
        painel.add(new JLabel(texto), posicao);
        posicao.gridx = 1;
        posicao.weightx = 1;
        painel.add(componente, posicao);
        return linha + 1;
    }

    private void preencher() {
        // No cadastro os campos ficam vazios; na edicao carregamos o material.
        if (material == null) {
            return;
        }
        nome.setText(material.getNome());
        descricao.setText(material.getDescricao());
        quantidade.setValue(material.getQuantidade());
        unidade.setSelectedItem(material.getUnidade());
        conservacao.setSelectedItem(material.getEstadoConservacao());
        endereco.setText(material.getEnderecoRetirada());
        cidade.setText(material.getCidade());
        estado.setText(material.getEstado());
        if (material.getCategoria() != null) {
            for (int i = 0; i < categoria.getItemCount(); i++) {
                if (categoria.getItemAt(i).getId().equals(material.getCategoria().getId())) categoria.setSelectedIndex(i);
            }
        }
    }

    private void salvar() {
        CategoriaDTO categoriaSelecionada = (CategoriaDTO) categoria.getSelectedItem();
        if (categoriaSelecionada == null) {
            Estilo.erro(this, new IllegalArgumentException("Selecione uma categoria."));
            return;
        }
        Map<String, Object> dados = lerFormulario(categoriaSelecionada);
        List<File> arquivos = fotos.getArquivos();

        // O acesso ao servidor acontece em segundo plano para nao travar a tela.
        Estilo.executar(this,
            () -> gravarMaterial(dados, arquivos),
            salvo -> concluirSalvamento());
    }

    private Map<String, Object> lerFormulario(CategoriaDTO categoriaSelecionada) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("nome", nome.getText());
        dados.put("descricao", descricao.getText());
        dados.put("quantidade", quantidade.getValue());
        dados.put("unidade", unidade.getSelectedItem());
        dados.put("estadoConservacao", conservacao.getSelectedItem());
        dados.put("categoriaId", categoriaSelecionada.getId());
        dados.put("enderecoRetirada", endereco.getText());
        dados.put("cidade", cidade.getText());
        dados.put("estado", estado.getText());
        dados.put("confirmarDuplicidade", confirmarDuplicidade.isSelected());
        return dados;
    }

    private MaterialDTO gravarMaterial(Map<String, Object> dados, List<File> arquivos) {
        MaterialDAO materialDAO = new MaterialDAO();
        if (material == null) {
            return materialDAO.cadastrar(dados, arquivos);
        }
        return materialDAO.alterar(material.getId(), dados);
    }

    private void concluirSalvamento() {
        Estilo.sucesso(this, "Material salvo com sucesso.");
        dispose();
        aoSalvar.run();
    }
}
