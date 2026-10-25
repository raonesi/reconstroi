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
        abas.addTab("Relatórios", new RelatoriosVIEW());
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
        painel.setBorder(new EmptyBorder(10, 18, 10, 18));
        JLabel marca = new JLabel("ReConstrói");
        marca.setForeground(Estilo.VERDE_ESCURO);
        marca.setFont(marca.getFont().deriveFont(Font.BOLD, 22f));
        painel.add(marca, BorderLayout.WEST);
        JPanel conta = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        conta.setOpaque(false);
        conta.add(new JLabel(usuario.getNome() + "  •  " + rotuloTipo(usuario.getTipo())));
        JButton sair = new JButton("Sair");
        sair.addActionListener(evento -> {
            new UsuarioDAO().logout();
            dispose();
            new FrmLoginVIEW().setVisible(true);
        });
        conta.add(sair);
        painel.add(conta, BorderLayout.EAST);
        return painel;
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
