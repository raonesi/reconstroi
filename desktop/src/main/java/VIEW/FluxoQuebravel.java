package VIEW;

import java.awt.*;
import javax.swing.*;

/**
 * FlowLayout que quebra linha DE VERDADE.
 *
 * O FlowLayout padrão calcula a altura como se tudo coubesse numa linha só. Quando a janela fica estreita,
 * os componentes passam para a linha de baixo, mas o painel não cresce e essa segunda linha some
 * (era o que acontecia com o botão "Pesquisar" na tela de Materiais). Aqui a altura preferida considera
 * a largura disponível, então o painel aumenta quando quebra linha.
 */
public class FluxoQuebravel extends FlowLayout {

    public FluxoQuebravel(int alinhamento, int espacoH, int espacoV) {
        super(alinhamento, espacoH, espacoV);
    }

    @Override public Dimension preferredLayoutSize(Container alvo) { return tamanho(alvo, true); }

    @Override public Dimension minimumLayoutSize(Container alvo) {
        Dimension minimo = tamanho(alvo, false);
        minimo.width -= getHgap() + 1;
        return minimo;
    }

    private Dimension tamanho(Container alvo, boolean preferido) {
        synchronized (alvo.getTreeLock()) {
            // Largura disponível: a do próprio painel ou, antes do primeiro layout, a do pai.
            int largura = alvo.getSize().width;
            Container pai = alvo;
            while (largura == 0 && pai.getParent() != null) {
                pai = pai.getParent();
                largura = pai.getSize().width;
            }
            if (largura == 0) largura = Integer.MAX_VALUE;

            Insets margens = alvo.getInsets();
            int espacoH = getHgap(), espacoV = getVgap();
            int larguraMaxima = largura - (margens.left + margens.right + espacoH * 2);

            Dimension total = new Dimension(0, 0);
            int larguraLinha = 0, alturaLinha = 0;
            for (Component c : alvo.getComponents()) {
                if (!c.isVisible()) continue;
                Dimension d = preferido ? c.getPreferredSize() : c.getMinimumSize();
                if (larguraLinha + d.width > larguraMaxima && larguraLinha > 0) {
                    adicionarLinha(total, larguraLinha, alturaLinha);
                    larguraLinha = 0;
                    alturaLinha = 0;
                }
                if (larguraLinha != 0) larguraLinha += espacoH;
                larguraLinha += d.width;
                alturaLinha = Math.max(alturaLinha, d.height);
            }
            adicionarLinha(total, larguraLinha, alturaLinha);

            total.width += margens.left + margens.right + espacoH * 2;
            total.height += margens.top + margens.bottom + espacoV * 2;

            // Dentro de um JScrollPane a largura do pai não limita; evita que o painel ocupe tudo sem precisar.
            Container rolagem = SwingUtilities.getAncestorOfClass(JScrollPane.class, alvo);
            if (rolagem != null && alvo.isValid()) total.width -= espacoH + 1;
            return total;
        }
    }

    private void adicionarLinha(Dimension total, int larguraLinha, int alturaLinha) {
        total.width = Math.max(total.width, larguraLinha);
        if (total.height > 0) total.height += getVgap();
        total.height += alturaLinha;
    }
}
