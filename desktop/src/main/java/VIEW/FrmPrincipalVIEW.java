package VIEW;

import DAO.SessaoDAO;
import DAO.UsuarioDAO;
import DTO.UsuarioDTO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FrmPrincipalVIEW extends JFrame {
    private final UsuarioDTO usuario;
    private final JTabbedPane abas = new JTabbedPane(JTabbedPane.LEFT);

    public FrmPrincipalVIEW(UsuarioDTO usuario) {
        super("ReConstrói — Plataforma de reaproveitamento");
        this.usuario = usuario;
        setIconImages(Estilo.iconesAplicativo());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setExtendedState(MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FUNDO);
        raiz.add(cabecalho(), BorderLayout.NORTH);
        abas.setFont(abas.getFont().deriveFont(Font.BOLD, 14f));
        abas.addTab("Dashboard", new DashboardVIEW());
        abas.addTab("Materiais", new MateriaisVIEW(usuario));
        abas.addTab("Reservas", new ReservasVIEW(usuario));
        abas.addTab("Doações", new DoacoesVIEW());
        abas.addTab("Relatórios", new RelatoriosVIEW(usuario));
        abas.addTab("Notificações", new NotificacoesVIEW());
        abas.addTab("Perfil", new PerfilVIEW());
        if (usuario.isAdmin()) abas.addTab("Administração", new AdminVIEW());
        abas.addChangeListener(evento -> atualizarAba());
        raiz.add(abas, BorderLayout.CENTER);
        setContentPane(raiz);
        SwingUtilities.invokeLater(this::atualizarAba);
    }

    private JPanel cabecalho() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDA), new EmptyBorder(10, 20, 10, 20)));

        JLabel marca = new JLabel("ReConstrói");
        marca.setIcon(new ImageIcon(Estilo.iconesAplicativo().get(3).getScaledInstance(34, 34, Image.SCALE_SMOOTH)));
        marca.setIconTextGap(10);
        marca.setForeground(Estilo.TEXTO);
        marca.setFont(marca.getFont().deriveFont(Font.BOLD, 21f));
        painel.add(marca, BorderLayout.WEST);

        JPanel conta = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        conta.setOpaque(false);
        JLabel identificacao = new JLabel("<html><b>" + escapar(usuario.getNome()) + "</b><br><span style='color:#5e7063'>"
            + rotuloTipo(usuario.getTipo()) + "</span></html>");
        conta.add(new Avatar(usuario.getNome()));
        conta.add(identificacao);
        conta.add(Box.createHorizontalStrut(6));
        JButton sair = Estilo.botaoSecundario("Sair");
        sair.addActionListener(evento -> {
            if (JOptionPane.showConfirmDialog(this, "Deseja sair da sua conta?", "Sair",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            new UsuarioDAO().logout();
            dispose();
            new FrmLoginVIEW().setVisible(true);
        });
        conta.add(sair);
        painel.add(conta, BorderLayout.EAST);
        return painel;
    }

    private static String escapar(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Círculo com as iniciais do usuário. */
    private static final class Avatar extends JComponent {
        private final String iniciais;

        Avatar(String nome) {
            StringBuilder sb = new StringBuilder();
            if (nome != null) for (String parte : nome.trim().split("\\s+")) {
                if (!parte.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(parte.charAt(0)));
            }
            iniciais = sb.length() == 0 ? "?" : sb.toString();
            setPreferredSize(new Dimension(38, 38));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Estilo.VERDE_CLARO);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Estilo.VERDE_ESCURO);
            g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
            FontMetrics m = g2.getFontMetrics();
            g2.drawString(iniciais, (getWidth() - m.stringWidth(iniciais)) / 2, (getHeight() - m.getHeight()) / 2 + m.getAscent());
            g2.dispose();
        }
    }

    private void atualizarAba() {
        Component selecionado = abas.getSelectedComponent();
        if (selecionado instanceof Atualizavel atualizavel) atualizavel.atualizarDados();
    }

    private static String rotuloTipo(String tipo) {
        if (tipo == null) return "Usuário";
        return switch (tipo) {
            case "DOADOR_PESSOA_FISICA" -> "Doador pessoa física";
            case "DOADOR_CONSTRUTORA" -> "Construtora";
            case "BENEFICIARIO_ONG" -> "ONG";
            case "BENEFICIARIO_FAMILIA" -> "Família";
            case "ADMIN" -> "Administrador";
            default -> tipo;
        };
    }
}
