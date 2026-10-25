package VIEW;

import DAO.UsuarioDAO;
import DTO.UsuarioDTO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FrmLoginVIEW extends JFrame {
    private final JTextField email = new JTextField();
    private final JPasswordField senha = new JPasswordField();

    public FrmLoginVIEW() {
        super("ReConstrói — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new GridLayout(1, 2));
        raiz.add(criarPainelDaMarca());
        raiz.add(criarFormularioDeLogin());
        setContentPane(raiz);
    }

    private JPanel criarPainelDaMarca() {
        JPanel marca = new JPanel(new GridBagLayout());
        marca.setBackground(Estilo.VERDE_ESCURO);
        JLabel textoMarca = new JLabel("<html><div style='text-align:center;color:white'><h1>ReConstrói</h1>"
            + "<p>Materiais que ganham uma nova história.</p><br><p>Aplicativo Desktop</p></div></html>");
        textoMarca.setForeground(Color.WHITE);
        marca.add(textoMarca);
        return marca;
    }

    private JPanel criarFormularioDeLogin() {
        JPanel entrada = new JPanel(new GridBagLayout());
        entrada.setBackground(Color.WHITE);
        JPanel formulario = new JPanel();
        formulario.setOpaque(false);
        formulario.setBorder(new EmptyBorder(30, 50, 30, 50));
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Acessar plataforma");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 24f));
        formulario.add(titulo);
        formulario.add(Box.createVerticalStrut(24));
        formulario.add(new JLabel("E-mail"));
        email.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        formulario.add(email);
        formulario.add(Box.createVerticalStrut(12));
        formulario.add(new JLabel("Senha"));
        senha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        formulario.add(senha);
        formulario.add(Box.createVerticalStrut(20));
        JButton entrar = Estilo.botao("ENTRAR");
        entrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        entrar.addActionListener(evento -> autenticar());
        getRootPane().setDefaultButton(entrar);
        formulario.add(entrar);
        formulario.add(Box.createVerticalStrut(10));

        JButton cadastrar = new JButton("Criar conta");
        cadastrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        cadastrar.addActionListener(evento -> new FrmCadastroVIEW(this, this::abrirSistema).setVisible(true));
        formulario.add(cadastrar);
        entrada.add(formulario);

        return entrada;
    }

    private void autenticar() {
        Estilo.executar(this,
            () -> new UsuarioDAO().autenticar(email.getText(), new String(senha.getPassword())),
            this::abrirSistema);
    }

    private void abrirSistema(UsuarioDTO usuario) {
        new FrmPrincipalVIEW(usuario).setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        Estilo.instalar();
        if (!InstanciaUnica.iniciarOuAtivar()) return;
        SwingUtilities.invokeLater(() -> new FrmLoginVIEW().setVisible(true));
    }
}
