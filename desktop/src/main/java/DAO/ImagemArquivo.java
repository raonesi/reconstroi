package DAO;

import java.io.File;
import java.util.Locale;

/** Tipos aceitos pela API, sem depender das associacoes de arquivos do Windows. */
public final class ImagemArquivo {
    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private ImagemArquivo() {}

    public static String contentType(File arquivo) {
        String nome = arquivo.getName().toLowerCase(Locale.ROOT);
        if (nome.endsWith(".webp")) return "image/webp";
        if (nome.endsWith(".png")) return "image/png";
        if (nome.endsWith(".jpg") || nome.endsWith(".jpeg")) return "image/jpeg";
        throw new IllegalArgumentException("Use fotos JPG, PNG ou WebP.");
    }

    public static void validar(File arquivo) {
        if (!arquivo.isFile() || !arquivo.canRead())
            throw new IllegalArgumentException("Selecione um arquivo de imagem acessível.");
        contentType(arquivo);
        if (arquivo.length() == 0) throw new IllegalArgumentException("O arquivo está vazio.");
        if (arquivo.length() > MAX_BYTES) throw new IllegalArgumentException("Cada foto pode ter até 5 MB.");
    }
}
