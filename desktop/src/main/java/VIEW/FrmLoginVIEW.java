package VIEW;

import DAO.UsuarioDAO;
import DTO.UsuarioDTO;
import java.awt.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FrmLoginVIEW extends JFrame {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final JTextField email = new JTextField();
    private final JPasswordField senha = new JPasswordField();
    private final JLabel mensagem = new JLabel(" ");
    private final JButton entrar = Estilo.botao("Entrar");

    public FrmLoginVIEW() {
        super("ReConstrói — Login");
        setIconImages(Estilo.iconesAplicativo());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        Estilo.tamanhoNaTela(this, 980, 620);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new GridLayout(1, 2));
        raiz.add(painelMarca());
        raiz.add(painelEntrada());
        setContentPane(raiz);
        getRootPane().setDefaultButton(entrar);
    }

    /** Lado esquerdo: identidade visual e proposta do sistema. */
    private JPanel painelMarca() {
        JPanel marca = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Estilo.VERDE, getWidth(), getHeight(), Estilo.VERDE_ESCURO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        JPanel conteudo = new JPanel();
        conteudo.setOpaque(false);
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBorder(new EmptyBorder(0, 48, 0, 48));

        JLabel logo = new JLabel(new ImageIcon(logoClaro(72)));
        JLabel nome = branco("ReConstrói", Font.BOLD, 34f);
        JLabel slogan = branco("<html>Materiais que ganham uma nova história.</html>", Font.PLAIN, 17f);
        conteudo.add(logo);
        conteudo.add(Box.createVerticalStrut(18));
        conteudo.add(nome);
        conteudo.add(Box.createVerticalStrut(6));
        conteudo.add(slogan);
        conteudo.add(Box.createVerticalStrut(34));
        for (String item : new String[] {"Doe sobras de obra em poucos cliques",
                                         "Reserve materiais para ONGs e famílias",
                                         "Acompanhe o impacto ambiental das doações"}) {
            conteudo.add(branco("✓  " + item, Font.PLAIN, 14f));
            conteudo.add(Box.createVerticalStrut(10));
        }
        for (Component c : conteudo.getComponents()) ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        marca.add(conteudo);
        return marca;
    }

    /** Versão do logo para fundo escuro: quadrado branco com a inicial em verde. */
    private static Image logoClaro(int tamanho) {
        java.awt.image.BufferedImage imagem = new java.awt.image.BufferedImage(tamanho, tamanho, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, tamanho, tamanho, tamanho / 3, tamanho / 3);
        g.setColor(Estilo.VERDE);
        g.setFont(new Font("Segoe UI", Font.BOLD, Math.round(tamanho * 0.62f)));
        FontMetrics m = g.getFontMetrics();
        g.drawString("R", (tamanho - m.stringWidth("R")) / 2, (tamanho - m.getHeight()) / 2 + m.getAscent());
        g.dispose();
        return imagem;
    }

    private static JLabel branco(String texto, int estilo, float tamanho) {
        JLabel label = new JLabel(texto);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(estilo, tamanho));
        return label;
    }

    /** Lado direito: formulário de acesso. */
    private JPanel painelEntrada() {
        JPanel entrada = new JPanel(new GridBagLayout());
        entrada.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(360, 440));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        JLabel saudacao = new JLabel("BEM-VINDO DE VOLTA");
        saudacao.setForeground(Estilo.VERDE);
        saudacao.setFont(saudacao.getFont().deriveFont(Font.BOLD, 12f));
        JLabel titulo = new JLabel("Acesse sua conta");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 28f));
        titulo.setForeground(Estilo.TEXTO);
        JLabel subtitulo = new JLabel("Informe seus dados para continuar.");
        subtitulo.setForeground(Estilo.TEXTO_SUAVE);

        Estilo.placeholder(email, "voce@exemplo.com");
        Estilo.placeholder(senha, "Sua senha");
        senha.putClientProperty("JPasswordField.showRevealButton", true);
        for (JTextField campo : new JTextField[] {email, senha}) {
            campo.setPreferredSize(new Dimension(10, 40));
            campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { limparErro(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { limparErro(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { limparErro(); }
            });
        }

        mensagem.setForeground(Estilo.ERRO);
        entrar.setPreferredSize(new Dimension(10, 44));
        entrar.addActionListener(evento -> autenticar());

        JButton recuperar = Estilo.link("Esqueci minha senha");
        recuperar.addActionListener(evento -> recuperarSenha());
        JPanel linhaRecuperar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        linhaRecuperar.setOpaque(false);
        linhaRecuperar.add(recuperar);

        JButton cadastrar = Estilo.link("Cadastre-se grátis");
        cadastrar.addActionListener(evento -> new FrmCadastroVIEW(this, this::abrirSistema).setVisible(true));
        JPanel linhaCadastro = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linhaCadastro.setOpaque(false);
        JLabel semConta = new JLabel("Ainda não possui conta?");
        semConta.setForeground(Estilo.TEXTO_SUAVE);
        linhaCadastro.add(semConta);
        linhaCadastro.add(cadastrar);

        int y = 0;
        c.gridy = y++; form.add(saudacao, c);
        c.gridy = y++; c.insets = new Insets(6, 0, 0, 0); form.add(titulo, c);
        c.gridy = y++; c.insets = new Insets(4, 0, 26, 0); form.add(subtitulo, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0); form.add(rotulo("E-mail"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 16, 0); form.add(email, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 6, 0); form.add(rotulo("Senha"), c);
        c.gridy = y++; c.insets = new Insets(0, 0, 4, 0); form.add(senha, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 8, 0); form.add(linhaRecuperar, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 8, 0); form.add(mensagem, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 18, 0); form.add(entrar, c);
        c.gridy = y++; c.insets = new Insets(0, 0, 0, 0); form.add(new JSeparator(), c);
        c.gridy = y; c.insets = new Insets(14, 0, 0, 0); form.add(linhaCadastro, c);

        entrada.add(form);
        return entrada;
    }

    private static JLabel rotulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setForeground(Estilo.TEXTO);
        return label;
    }

    private void limparErro() {
        mensagem.setText(" ");
        email.putClientProperty("JComponent.outline", null);
        senha.putClientProperty("JComponent.outline", null);
    }

    private void mostrarErro(String texto, JComponent campo) {
        mensagem.setText(texto);
        if (campo != null) {
            campo.putClientProperty("JComponent.outline", "error");
            campo.requestFocusInWindow();
        }
    }

    /** Valida localmente antes de chamar a API e mostra o erro na própria tela. */
    private void autenticar() {
        String valorEmail = email.getText().trim();
        String valorSenha = new String(senha.getPassword());
        if (valorEmail.isEmpty()) { mostrarErro("Informe seu e-mail.", email); return; }
        if (!EMAIL.matcher(valorEmail).matches()) { mostrarErro("E-mail inválido. Ex.: voce@exemplo.com", email); return; }
        if (valorSenha.isEmpty()) { mostrarErro("Informe sua senha.", senha); return; }

        entrar.setEnabled(false);
        entrar.setText("Entrando…");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<UsuarioDTO, Void>() {
            @Override protected UsuarioDTO doInBackground() throws Exception {
                return new UsuarioDAO().autenticar(valorEmail, valorSenha);
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                entrar.setEnabled(true);
                entrar.setText("Entrar");
                try {
                    abrirSistema(get());
                } catch (Exception erro) {
                    mostrarErro(Estilo.mensagemErro(erro), null);
                }
            }
        }.execute();
    }

    private void abrirSistema(UsuarioDTO usuario) {
        new FrmPrincipalVIEW(usuario).setVisible(true);
        dispose();
    }

    private void recuperarSenha() {
        String valor = (String) JOptionPane.showInputDialog(this, "Informe o e-mail da sua conta:", "Recuperação de senha",
            JOptionPane.QUESTION_MESSAGE, null, null, email.getText().trim());
        if (valor == null || valor.isBlank()) return;
        Estilo.executar(this, () -> new UsuarioDAO().recuperarSenha(valor.trim()), msg -> Estilo.sucesso(this, msg));
    }

    public static void main(String[] args) {
        Estilo.instalar();
        if (!InstanciaUnica.iniciarOuAtivar()) return;
        SwingUtilities.invokeLater(() -> new FrmLoginVIEW().setVisible(true));
    }
}
