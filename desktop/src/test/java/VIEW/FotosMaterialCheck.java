package VIEW;

import DAO.ConexaoDAO;
import DAO.ImagemArquivo;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;

/** Executar manualmente com Java 17 e o classpath do desktop (sem banco de dados). */
public final class FotosMaterialCheck {
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }

    private static TransferHandler.TransferSupport transfer(FotosMaterialVIEW panel, List<File> files) {
        return new TransferHandler.TransferSupport(panel, new Transferable() {
            public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[] {DataFlavor.javaFileListFlavor}; }
            public boolean isDataFlavorSupported(DataFlavor flavor) { return DataFlavor.javaFileListFlavor.equals(flavor); }
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
                return files;
            }
        });
    }

    private static <T> T find(Container root, Class<T> type, String text) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c) && (text == null || c instanceof JButton b && b.getText().equals(text))) return type.cast(c);
            if (c instanceof Container child) { T result = find(child, type, text); if (result != null) return result; }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory(Path.of(args[0]), "fotos-check-");
        List<File> files = new ArrayList<>();
        for (String name : List.of("foto.WEBP", "foto.jpg", "foto.jpeg", "foto.png", "quinta.webp", "sexta.webp"))
            files.add(Files.write(root.resolve(name), new byte[] {1, 2, 3}).toFile());
        File invalid = Files.writeString(root.resolve("arquivo.txt"), "texto").toFile();
        File empty = Files.createFile(root.resolve("vazia.webp")).toFile();
        File big = root.resolve("grande.webp").toFile();
        try (RandomAccessFile f = new RandomAccessFile(big, "rw")) { f.setLength(ImagemArquivo.MAX_BYTES + 1); }
        File limit = root.resolve("limite.webp").toFile();
        try (RandomAccessFile f = new RandomAccessFile(limit, "rw")) { f.setLength(ImagemArquivo.MAX_BYTES); }
        ImagemArquivo.validar(limit);
        check(ImagemArquivo.contentType(files.get(0)).equals("image/webp"), "MIME WebP");
        check(ImagemArquivo.contentType(files.get(2)).equals("image/jpeg"), "MIME JPEG");

        SwingUtilities.invokeAndWait(() -> {
            List<String> warnings = new ArrayList<>();
            FotosMaterialVIEW panel = new FotosMaterialVIEW(warnings::add);
            TransferHandler handler = panel.getTransferHandler();
            check(!handler.canImport(new TransferHandler.TransferSupport(panel, new StringSelection("texto"))), "Texto deve ser rejeitado");
            check(handler.importData(transfer(panel, List.of(files.get(0), files.get(1)))), "Importar arquivos do explorador");
            check(panel.getArquivos().size() == 2, "Selecionar varios arquivos");
            handler.importData(transfer(panel, List.of(files.get(0))));
            check(panel.getArquivos().size() == 2, "Evitar repetidos");
            handler.importData(transfer(panel, List.of(invalid, empty, big, root.toFile())));
            check(panel.getArquivos().size() == 2 && !warnings.isEmpty(), "Rejeitar formato, vazio, tamanho e pasta");
            handler.importData(transfer(panel, files.subList(2, files.size())));
            check(panel.getArquivos().size() == 5, "Limitar quantidade");
            JList<?> list = find(panel, JList.class, null);
            list.setSelectedIndex(0);
            find(panel, JButton.class, "Remover selecionadas").doClick();
            check(panel.getArquivos().size() == 4 && files.get(0).exists(), "Remover apenas da selecao");
            check(panel.adicionarArquivos(List.of(files.get(0))), "Reinserir por seletor apos remocao");
        });

        AtomicReference<String> request = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/upload", exchange -> {
            request.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1));
            byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body); exchange.close();
        });
        server.start();
        try {
            System.setProperty("reconstroi.api.url", "http://127.0.0.1:" + server.getAddress().getPort());
            new ConexaoDAO().multipart("/upload", Map.of("nome", "Teste"), List.of(files.get(0)), JsonNode.class);
            check(request.get().contains("Content-Type: image/webp\r\n"), "MIME WebP no upload");
            check(request.get().contains("filename=\"foto.WEBP\""), "Preservar nome WebP");
        } finally { server.stop(0); }
        System.out.println("PASS transferencia de arquivos, selecao multipla, limites, remocao, duplicados e upload WebP");
    }
}
