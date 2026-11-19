package VIEW;

import DAO.SessaoDAO;
import DAO.UsuarioDAO;
import DTO.UsuarioDTO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PerfilVIEW extends JPanel implements Atualizavel {
    private final JTextField nome = new JTextField();
    private final JTextField email = new JTextField();
    private final JTextField telefone = new JTextField();
    private final JTextField cidade = new JTextField();
    private final JTextField estado = new JTextField();
    private final JTextField documento = new JTextField();
    private final JTextField tipo = new JTextField();
    public PerfilVIEW() {
        setLayout(new BorderLayout(0, 12));
        add(Estilo.pagina("Perfil", "Mantenha seus dados de contato e localização atualizados."), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10)); form.setBorder(new EmptyBorder(30, 80, 30, 80));
        campo(form, "Nome", nome); campo(form, "E-mail", email); campo(form, "Telefone", telefone);
        campo(form, "Cidade", cidade); campo(form, "Estado (UF)", estado); campo(form, "Documento", documento); campo(form, "Perfil de acesso", tipo);
        email.setEditable(false); tipo.setEditable(false);
        add(form, BorderLayout.CENTER);
        JButton salvar = Estilo.botao("Salvar alterações"); salvar.addActionListener(e -> salvar());
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT)); rodape.add(salvar); add(rodape, BorderLayout.SOUTH);
    }
    private static void campo(JPanel p, String r, JComponent c) { p.add(new JLabel(r)); p.add(c); }
    @Override public void atualizarDados() { Estilo.executar(this, () -> new UsuarioDAO().consultarPerfil(), this::preencher); }
    private void preencher(UsuarioDTO u) {
        nome.setText(u.getNome()); email.setText(u.getEmail()); telefone.setText(u.getTelefone());
        cidade.setText(u.getCidade()); estado.setText(u.getEstado()); documento.setText(u.getDocumento()); tipo.setText(u.getTipo() + " / " + u.getNivelAcesso());
    }
    private void salvar() {
        Estilo.executar(this, () -> new UsuarioDAO().atualizarPerfil(nome.getText(), telefone.getText(), cidade.getText(), estado.getText(), documento.getText()),
            u -> { preencher(u); Estilo.sucesso(this, "Perfil atualizado."); });
    }
}
