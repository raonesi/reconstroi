package VIEW;

import DAO.UsuarioDAO;
import DTO.UsuarioDTO;
import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FrmCadastroVIEW extends JDialog {
    private final JTextField nome = new JTextField();
    private final JTextField email = new JTextField();
    private final JTextField telefone = new JTextField();
    private final JPasswordField senha = new JPasswordField();
    private final JComboBox<TipoItem> tipo = new JComboBox<>(new TipoItem[] {
        new TipoItem("Pessoa física doadora", "DOADOR_PESSOA_FISICA"),
        new TipoItem("Construtora doadora", "DOADOR_CONSTRUTORA"),
        new TipoItem("ONG beneficiária", "BENEFICIARIO_ONG"),
        new TipoItem("Família beneficiária", "BENEFICIARIO_FAMILIA")
    });
    private final JTextField cidade = new JTextField();
    private final JTextField estado = new JTextField();
    private final JTextField documento = new JTextField();

    public FrmCadastroVIEW(Frame pai, Consumer<UsuarioDTO> aoCadastrar) {
        super(pai, "Cadastro — ReConstrói", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 610);
        setLocationRelativeTo(pai);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(new EmptyBorder(20, 24, 20, 24));
        JLabel titulo = new JLabel("Criar uma conta");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 23f));
        raiz.add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridLayout(0, 1, 5, 5));
        campo(formulario, "Nome", nome);
        campo(formulario, "E-mail", email);
        campo(formulario, "Telefone", telefone);
        campo(formulario, "Senha (mínimo 8 caracteres)", senha);
        campo(formulario, "Tipo de usuário", tipo);
        campo(formulario, "Cidade", cidade);
        campo(formulario, "Estado (UF)", estado);
        campo(formulario, "CPF/CNPJ/Documento", documento);
        raiz.add(formulario, BorderLayout.CENTER);

        JButton cadastrar = Estilo.botao("CADASTRAR");
        cadastrar.addActionListener(evento -> {
            TipoItem item = (TipoItem) tipo.getSelectedItem();
            Estilo.executar(this,
                () -> new UsuarioDAO().cadastrar(nome.getText(), email.getText(), telefone.getText(),
                    new String(senha.getPassword()), item.valor(), cidade.getText(), estado.getText(), documento.getText()),
                usuario -> { dispose(); aoCadastrar.accept(usuario); });
        });
        raiz.add(cadastrar, BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    private static void campo(JPanel painel, String rotulo, JComponent campo) {
        painel.add(new JLabel(rotulo));
        painel.add(campo);
    }

    private record TipoItem(String rotulo, String valor) {
        @Override public String toString() { return rotulo; }
    }
}
