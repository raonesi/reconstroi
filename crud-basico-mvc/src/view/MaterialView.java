package view;

import controller.MaterialController;
import model.Material;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// A tela mostra os dados e encaminha os cliques para o controller.
public class MaterialView extends JFrame {
    private final MaterialController controller;
    private final JTextField nome = new JTextField();
    private final JTextField quantidade = new JTextField();
    private final JTextField descricao = new JTextField();
    private final DefaultTableModel linhas = new DefaultTableModel(
            new String[] {"ID", "Nome", "Quantidade", "Descricao"}, 0) {
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(linhas);
    private int idSelecionado = 0;

    public MaterialView(MaterialController controller) {
        super("CRUD basico MVC - Materiais");
        this.controller = controller;
        setSize(800, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel formulario = new JPanel(new GridLayout(3, 2, 8, 8));
        formulario.add(new JLabel("Nome:"));
        formulario.add(nome);
        formulario.add(new JLabel("Quantidade:"));
        formulario.add(quantidade);
        formulario.add(new JLabel("Descricao:"));
        formulario.add(descricao);

        JButton cadastrar = new JButton("Cadastrar");
        JButton editar = new JButton("Editar selecionado");
        JButton excluir = new JButton("Excluir selecionado");
        JButton limpar = new JButton("Limpar campos");
        JButton listar = new JButton("Atualizar lista");
        JPanel botoes = new JPanel();
        botoes.add(cadastrar);
        botoes.add(editar);
        botoes.add(excluir);
        botoes.add(limpar);
        botoes.add(listar);

        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        painel.add(formulario, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        painel.add(botoes, BorderLayout.SOUTH);
        setContentPane(painel);

        cadastrar.addActionListener(evento -> cadastrar());
        editar.addActionListener(evento -> editar());
        excluir.addActionListener(evento -> excluir());
        limpar.addActionListener(evento -> limpar());
        listar.addActionListener(evento -> atualizarLista());
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(evento -> selecionar());
        atualizarLista();
    }

    private void cadastrar() {
        try {
            controller.cadastrar(nome.getText(), quantidade.getText(), descricao.getText());
            atualizarLista();
        } catch (SQLException | IllegalArgumentException e) {
            mostrarErro(e);
        }
    }

    private void editar() {
        if (!temSelecao()) return;
        try {
            controller.editar(idSelecionado, nome.getText(), quantidade.getText(), descricao.getText());
            atualizarLista();
        } catch (SQLException | IllegalArgumentException e) {
            mostrarErro(e);
        }
    }

    private void excluir() {
        if (!temSelecao()) return;
        int resposta = JOptionPane.showConfirmDialog(this, "Excluir o material selecionado?",
                "Confirmar exclusao", JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) return;
        try {
            controller.excluir(idSelecionado);
            atualizarLista();
        } catch (SQLException | IllegalArgumentException e) {
            mostrarErro(e);
        }
    }

    private void atualizarLista() {
        try {
            java.util.List<Material> materiais = controller.listar();
            linhas.setRowCount(0);
            for (Material material : materiais) {
                linhas.addRow(new Object[] {material.getId(), material.getNome(),
                        material.getQuantidade(), material.getDescricao()});
            }
            limpar();
        } catch (SQLException | IllegalArgumentException e) {
            mostrarErro(e);
        }
    }

    private void selecionar() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            idSelecionado = 0;
            return;
        }
        idSelecionado = (Integer) linhas.getValueAt(linha, 0);
        nome.setText(linhas.getValueAt(linha, 1).toString());
        quantidade.setText(linhas.getValueAt(linha, 2).toString());
        descricao.setText(linhas.getValueAt(linha, 3).toString());
    }

    private boolean temSelecao() {
        if (idSelecionado == 0) {
            JOptionPane.showMessageDialog(this, "Selecione um material na tabela.");
            return false;
        }
        return true;
    }

    private void limpar() {
        tabela.clearSelection();
        idSelecionado = 0;
        nome.setText("");
        quantidade.setText("");
        descricao.setText("");
    }

    private void mostrarErro(Exception erro) {
        JOptionPane.showMessageDialog(this, erro.getMessage(), "Nao foi possivel concluir", JOptionPane.ERROR_MESSAGE);
    }
}
