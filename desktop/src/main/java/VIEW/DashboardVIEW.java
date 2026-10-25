package VIEW;

import DAO.RelatorioDAO;
import DTO.DashboardDTO;
import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardVIEW extends JPanel implements Atualizavel {
    private final JLabel materiais = valor();
    private final JLabel disponiveis = valor();
    private final JLabel reutilizados = valor();
    private final JLabel reservas = valor();
    private final JLabel doacoes = valor();
    private final JLabel usuarios = valor();
    private final JLabel impacto = valor();
    private final JTextArea categorias = new JTextArea();
    private boolean carregado;

    public DashboardVIEW() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Estilo.FUNDO);
        setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel titulo = new JLabel("Visão geral da plataforma");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 25f));
        add(titulo, BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(12, 12));
        centro.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(2, 4, 12, 12));
        cards.setOpaque(false);
        cards.add(card("Materiais cadastrados", materiais));
        cards.add(card("Materiais disponíveis", disponiveis));
        cards.add(card("Materiais reutilizados", reutilizados));
        cards.add(card("Reservas", reservas));
        cards.add(card("Doações concluídas", doacoes));
        cards.add(card("Usuários", usuarios));
        cards.add(card("CO₂ evitado", impacto));
        cards.add(card("Tecnologia", new JLabel("Desktop + API REST")));
        centro.add(cards, BorderLayout.NORTH);
        categorias.setEditable(false);
        categorias.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        categorias.setBorder(new EmptyBorder(12, 12, 12, 12));
        centro.add(new JScrollPane(categorias), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    private static JLabel valor() {
        JLabel label = new JLabel("—");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 25f));
        label.setForeground(Estilo.VERDE_ESCURO);
        return label;
    }

    private static JPanel card(String titulo, JComponent valor) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(225, 233, 226)), new EmptyBorder(14, 14, 14, 14)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel rotulo = new JLabel(titulo.toUpperCase());
        rotulo.setForeground(new Color(100, 115, 104));
        card.add(rotulo); card.add(Box.createVerticalStrut(10)); card.add(valor);
        return card;
    }

    @Override public void atualizarDados() {
        Estilo.executar(this, () -> new RelatorioDAO().consultarDashboard(), this::preencher);
    }

    private void preencher(DashboardDTO d) {
        materiais.setText(String.valueOf(d.getMateriaisCadastrados()));
        disponiveis.setText(String.valueOf(d.getMateriaisDisponiveis()));
        reutilizados.setText(String.valueOf(d.getMateriaisReutilizados()));
        reservas.setText(String.valueOf(d.getReservas()));
        doacoes.setText(String.valueOf(d.getDoacoesConcluidas()));
        usuarios.setText(String.valueOf(d.getUsuarios()));
        impacto.setText(d.getCo2EvitadoKg() + " kg");
        StringBuilder texto = new StringBuilder("CATEGORIAS MAIS UTILIZADAS\n\n");
        for (Map.Entry<String, Long> item : d.getCategoriasMaisUtilizadas().entrySet()) {
            texto.append(String.format("%-30s %d%n", item.getKey(), item.getValue()));
        }
        texto.append("\nUSUÁRIOS POR PERFIL\n\n")
            .append("ONGs: ").append(d.getOngs()).append("   Construtoras: ").append(d.getConstrutoras())
            .append("   Famílias: ").append(d.getFamilias());
        categorias.setText(texto.toString());
        carregado = true;
    }
}
