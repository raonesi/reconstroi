package VIEW;

import java.awt.Frame;
import java.awt.Window;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.SwingUtilities;

public final class InstanciaUnica {
    private static final int PORTA = 45873;
    private static ServerSocket servidor;
    private InstanciaUnica() {}

    public static boolean iniciarOuAtivar() {
        try {
            servidor = new ServerSocket(PORTA, 5, InetAddress.getLoopbackAddress());
            Thread thread = new Thread(InstanciaUnica::aguardar, "reconstroi-desktop-single-instance");
            thread.setDaemon(true);
            thread.start();
            return true;
        } catch (Exception jaExecutando) {
            try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), PORTA)) {
                OutputStream out = socket.getOutputStream();
                out.write(1);
                out.flush();
            } catch (Exception ignored) { }
            return false;
        }
    }

    private static void aguardar() {
        while (!servidor.isClosed()) {
            try (Socket socket = servidor.accept()) {
                socket.getInputStream().read();
                SwingUtilities.invokeLater(InstanciaUnica::mostrarJanela);
            } catch (Exception ignored) { }
        }
    }

    private static void mostrarJanela() {
        Window[] janelas = Window.getWindows();
        for (int i = janelas.length - 1; i >= 0; i--) {
            Window janela = janelas[i];
            if (!janela.isDisplayable()) continue;
            janela.setVisible(true);
            if (janela instanceof Frame frame) frame.setState(Frame.NORMAL);
            janela.toFront();
            janela.requestFocus();
            return;
        }
    }
}
