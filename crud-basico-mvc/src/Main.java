import controller.MaterialController;
import model.MaterialDAO;
import view.MaterialView;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        MaterialDAO dao = new MaterialDAO();
        MaterialController controller = new MaterialController(dao);
        SwingUtilities.invokeLater(() -> new MaterialView(controller).setVisible(true));
    }
}
